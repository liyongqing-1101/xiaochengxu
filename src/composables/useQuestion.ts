/**
 * 答题逻辑 composable
 */
import { useQuestionStore } from '@/stores/question'
import type { StartSessionParams } from '@/types/question'
import { CategoryId } from '@/types/enums'
import { useExamStore } from '@/stores/exam'

export function useQuestion() {
  const questionStore = useQuestionStore()
  const examStore = useExamStore()

  /**
   * 开始刷题
   */
  async function startPractice(subjectId: number, options?: {
    chapterId?: number
    knowledgePointId?: number
    questionCount?: number
    mode?: 'practice' | 'random' | 'exam'
  }): Promise<void> {
    const params: StartSessionParams = {
      categoryId: examStore.currentCategoryId as CategoryId,
      subjectId,
      ...options,
    }
    await questionStore.startSession(params)
  }

  return {
    // state
    session: questionStore.session,
    currentQuestion: questionStore.currentQuestion,
    currentNumber: questionStore.currentNumber,
    totalCount: questionStore.totalCount,
    progress: questionStore.progress,
    selectedOptions: questionStore.selectedOptions,
    isCurrentSubmitted: questionStore.isCurrentSubmitted,
    showExplanation: questionStore.showExplanation,
    answerStatusMap: questionStore.answerStatusMap,
    submitting: questionStore.submitting,
    // actions
    startPractice,
    selectOption: questionStore.selectOption,
    submitAnswer: questionStore.submitAnswer,
    prevQuestion: questionStore.prevQuestion,
    nextQuestion: questionStore.nextQuestion,
    jumpTo: questionStore.jumpTo,
    toggleCollect: questionStore.toggleCollect,
    endSession: questionStore.endSession,
    reset: questionStore.reset,
  }
}
