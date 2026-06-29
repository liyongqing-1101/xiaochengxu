/**
 * 答题会话状态管理 Store
 *
 * 管理:
 * - 当前答题会话 (题目列表、进度、用户答案)
 * - 题目切换、提交、收藏
 * - 答题进度本地持久化 (断点续答)
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  Question,
  QuestionSession,
  UserAnswer,
  StartSessionParams,
  SubmitResult,
} from '@/types/question'
import { AnswerStatus } from '@/types/enums'
import { storage } from '@/utils/storage'
import { StorageKey } from '@/utils/constants'

export const useQuestionStore = defineStore('question', () => {
  // ═══════════════════════════════════════
  // State
  // ═══════════════════════════════════════
  const session = ref<QuestionSession | null>(null)
  const submitting = ref(false)
  const showExplanation = ref(false)

  // ═══════════════════════════════════════
  // Getters
  // ═══════════════════════════════════════
  /** 当前题目 */
  const currentQuestion = computed<Question | null>(() => {
    if (!session.value) return null
    return session.value.questions[session.value.currentIndex] ?? null
  })

  /** 当前题号 (从1开始) */
  const currentNumber = computed(() => (session.value?.currentIndex ?? 0) + 1)

  /** 总题数 */
  const totalCount = computed(() => session.value?.questions.length ?? 0)

  /** 是否第一题 */
  const isFirstQuestion = computed(() => session.value?.currentIndex === 0)

  /** 是否最后一题 */
  const isLastQuestion = computed(() =>
    session.value ? session.value.currentIndex >= session.value.questions.length - 1 : true,
  )

  /** 答题进度 */
  const progress = computed(() => {
    if (!session.value) return { total: 0, answered: 0, correct: 0 }
    const answers = Object.values(session.value.answers)
    return {
      total: session.value.questions.length,
      answered: answers.filter(a => a.isCorrect !== undefined).length,
      correct: answers.filter(a => a.isCorrect).length,
    }
  })

  /** 当前题目的作答状态 */
  const currentAnswerStatus = computed(() => {
    if (!session.value || !currentQuestion.value) return AnswerStatus.UNANSWERED
    const answer = session.value.answers[currentQuestion.value.id]
    if (!answer) return AnswerStatus.UNANSWERED
    if (answer.isCorrect === undefined) return AnswerStatus.ANSWERED
    return answer.isCorrect ? AnswerStatus.CORRECT : AnswerStatus.INCORRECT
  })

  /** 当前题目已选中的选项 */
  const selectedOptions = computed(() => {
    if (!session.value || !currentQuestion.value) return []
    return session.value.answers[currentQuestion.value.id]?.selectedOptions ?? []
  })

  /** 当前题目是否已提交 */
  const isCurrentSubmitted = computed(() => {
    if (!session.value || !currentQuestion.value) return false
    const answer = session.value.answers[currentQuestion.value.id]
    return answer ? answer.isCorrect !== undefined : false
  })

  /** 题目作答状态映射(用于答题卡) */
  const answerStatusMap = computed(() => {
    const map: Record<number, AnswerStatus> = {}
    if (!session.value) return map
    session.value.questions.forEach((q) => {
      const answer = session.value.answers[q.id]
      if (!answer) {
        map[q.id] = AnswerStatus.UNANSWERED
      } else if (answer.isCorrect === undefined) {
        map[q.id] = AnswerStatus.ANSWERED
      } else {
        map[q.id] = answer.isCorrect ? AnswerStatus.CORRECT : AnswerStatus.INCORRECT
      }
    })
    return map
  })

  // ═══════════════════════════════════════
  // Actions
  // ═══════════════════════════════════════

  /**
   * 开始答题会话
   */
  async function startSession(params: StartSessionParams): Promise<void> {
    try {
      const { post } = await import('@/utils/request')
      const data = await post<QuestionSession>('/question/session/start', params as any)
      session.value = data
      showExplanation.value = false
    } catch (err) {
      uni.showToast({ title: '加载题目失败', icon: 'none' })
      throw err
    }
  }

  /**
   * 选择/取消选项
   */
  function selectOption(optionId: string): void {
    if (!session.value || !currentQuestion.value) return
    if (isCurrentSubmitted.value) return // 已提交不允许修改

    const qid = currentQuestion.value.id
    const existing = session.value.answers[qid]

    if (!existing || !existing.selectedOptions) {
      // 首次选择
      session.value.answers[qid] = {
        questionId: qid,
        selectedOptions: [optionId],
        isCorrect: undefined as any,
        answeredAt: Date.now(),
        duration: 0,
      }
      return
    }

    const selected = existing.selectedOptions

    // 单选题/判断题: 直接替换
    if (
      currentQuestion.value.type === 1 /* SINGLE_CHOICE */ ||
      currentQuestion.value.type === 3 /* TRUE_FALSE */
    ) {
      existing.selectedOptions = [optionId]
      return
    }

    // 多选题: 切换选中
    const idx = selected.indexOf(optionId)
    if (idx > -1) {
      selected.splice(idx, 1)
    } else {
      selected.push(optionId)
    }
  }

  /**
   * 提交当前题目答案
   */
  async function submitAnswer(): Promise<SubmitResult | null> {
    if (!session.value || !currentQuestion.value) return null

    const answer = session.value.answers[currentQuestion.value.id]
    if (!answer || answer.selectedOptions.length === 0) {
      uni.showToast({ title: '请先选择答案', icon: 'none' })
      return null
    }

    submitting.value = true
    try {
      const { post } = await import('@/utils/request')
      const result = await post<SubmitResult>('/question/submit', {
        sessionId: session.value.sessionId,
        questionId: currentQuestion.value.id,
        selectedOptions: answer.selectedOptions,
        duration: Math.floor((Date.now() - answer.answeredAt) / 1000),
      })

      // 更新本地答案状态
      answer.isCorrect = result.isCorrect
      showExplanation.value = true

      // 错题自动加入错题本
      if (!result.isCorrect) {
        const { useWrongBookStore } = await import('./wrongBook')
        useWrongBookStore().addWrong(currentQuestion.value, answer.selectedOptions)
      }

      // 保存进度
      saveProgressToLocal()

      return result
    } catch (err) {
      console.error('[QuestionStore] submitAnswer error:', err)
      return null
    } finally {
      submitting.value = false
    }
  }

  /**
   * 上一题
   */
  function prevQuestion(): void {
    if (!session.value || isFirstQuestion.value) return
    session.value.currentIndex--
    showExplanation.value = false
  }

  /**
   * 下一题
   */
  function nextQuestion(): void {
    if (!session.value || isLastQuestion.value) return
    session.value.currentIndex++
    showExplanation.value = false
  }

  /**
   * 跳转到指定题号 (答题卡)
   */
  function jumpTo(index: number): void {
    if (!session.value) return
    if (index >= 0 && index < session.value.questions.length) {
      session.value.currentIndex = index
      showExplanation.value = false
    }
  }

  /**
   * 收藏/取消收藏当前题目
   */
  async function toggleCollect(): Promise<void> {
    if (!currentQuestion.value) return
    try {
      const { post, del } = await import('@/utils/request')
      if (currentQuestion.value.collected) {
        await del('/question/collect', { questionId: currentQuestion.value.id })
        currentQuestion.value.collected = false
        uni.showToast({ title: '已取消收藏', icon: 'none' })
      } else {
        await post('/question/collect', { questionId: currentQuestion.value.id })
        currentQuestion.value.collected = true
        uni.showToast({ title: '已收藏', icon: 'success' })
      }
    } catch {
      uni.showToast({ title: '操作失败', icon: 'none' })
    }
  }

  /**
   * 结束答题会话
   */
  async function endSession(): Promise<void> {
    if (!session.value) return
    try {
      const { post } = await import('@/utils/request')
      await post('/question/session/end', { sessionId: session.value.sessionId })
    } catch {
      // 静默失败
    } finally {
      storage.remove(StorageKey.ANSWER_PROGRESS)
    }
  }

  /**
   * 本地保存答题进度
   */
  function saveProgressToLocal(): void {
    if (!session.value) return
    storage.set(StorageKey.ANSWER_PROGRESS, {
      sessionId: session.value.sessionId,
      currentIndex: session.value.currentIndex,
      answers: session.value.answers,
    })
  }

  /**
   * 重置状态
   */
  function reset(): void {
    session.value = null
    showExplanation.value = false
    submitting.value = false
  }

  return {
    // state
    session,
    submitting,
    showExplanation,
    // getters
    currentQuestion,
    currentNumber,
    totalCount,
    isFirstQuestion,
    isLastQuestion,
    progress,
    currentAnswerStatus,
    selectedOptions,
    isCurrentSubmitted,
    answerStatusMap,
    // actions
    startSession,
    selectOption,
    submitAnswer,
    prevQuestion,
    nextQuestion,
    jumpTo,
    toggleCollect,
    endSession,
    saveProgressToLocal,
    reset,
  }
})
