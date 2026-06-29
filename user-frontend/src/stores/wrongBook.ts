/**
 * 错题本状态管理 Store
 *
 * 管理:
 * - 本地错题列表 (local-first, 离线可用)
 * - 按科目筛选
 * - 增删查操作
 * - $subscribe 自动持久化
 */

import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import type { Question, WrongQuestion } from '@/types/question'
import { WrongSource } from '@/types/enums'
import { storage } from '@/utils/storage'
import { StorageKey } from '@/utils/constants'

export const useWrongBookStore = defineStore('wrongBook', () => {
  // ═══════════════════════════════════════
  // State
  // ═══════════════════════════════════════
  const wrongList = ref<WrongQuestion[]>([])
  const filterSubjectId = ref<number | null>(null)

  // ═══════════════════════════════════════
  // Getters
  // ═══════════════════════════════════════

  /** 按科目筛选后的错题列表 */
  const filteredList = computed(() => {
    if (filterSubjectId.value === null) return wrongList.value
    return wrongList.value.filter(w => w.question.subjectId === filterSubjectId.value)
  })

  /** 错题总数 */
  const totalCount = computed(() => wrongList.value.length)

  /** 筛选后的错题数 */
  const filteredCount = computed(() => filteredList.value.length)

  /** 按科目分组的错题统计 */
  const subjectStats = computed(() => {
    const map: Record<number, { subjectId: number; subjectName: string; count: number }> = {}
    wrongList.value.forEach((w) => {
      const sid = w.question.subjectId
      if (!map[sid]) {
        map[sid] = {
          subjectId: sid,
          subjectName: w.question.subjectName || `科目${sid}`,
          count: 0,
        }
      }
      map[sid].count++
    })
    return Object.values(map)
  })

  // ═══════════════════════════════════════
  // Actions
  // ═══════════════════════════════════════

  /**
   * 从本地存储加载错题
   */
  function loadFromStorage(): void {
    const saved = storage.get<WrongQuestion[]>(StorageKey.WRONG_QUESTIONS)
    if (saved) {
      wrongList.value = saved
    }
  }

  /**
   * 持久化到本地存储
   */
  function saveToStorage(): void {
    storage.set(StorageKey.WRONG_QUESTIONS, wrongList.value)
  }

  /**
   * 添加错题
   */
  function addWrong(question: Question, userAnswer: string[]): void {
    const existing = wrongList.value.find(w => w.question.id === question.id)

    if (existing) {
      // 已存在: 增加错误次数
      existing.errorCount++
      existing.lastErrorTime = Date.now()
      existing.lastWrongAnswer = userAnswer
    } else {
      // 新增错题
      wrongList.value.unshift({
        question,
        errorCount: 1,
        lastErrorTime: Date.now(),
        lastWrongAnswer: userAnswer,
        source: WrongSource.PRACTICE,
      })
    }

    saveToStorage()
  }

  /**
   * 移除错题
   */
  function removeWrong(questionId: number): void {
    const idx = wrongList.value.findIndex(w => w.question.id === questionId)
    if (idx > -1) {
      wrongList.value.splice(idx, 1)
      saveToStorage()
    }
  }

  /**
   * 按科目清空错题
   */
  function clearBySubject(subjectId: number): void {
    wrongList.value = wrongList.value.filter(w => w.question.subjectId !== subjectId)
    saveToStorage()
  }

  /**
   * 清空所有错题
   */
  function clearAll(): void {
    wrongList.value = []
    saveToStorage()
  }

  /**
   * 检查题目是否在错题本中
   */
  function hasWrong(questionId: number): boolean {
    return wrongList.value.some(w => w.question.id === questionId)
  }

  /**
   * 设置科目筛选
   */
  function setFilter(subjectId: number | null): void {
    filterSubjectId.value = subjectId
  }

  return {
    // state
    wrongList,
    filterSubjectId,
    // getters
    filteredList,
    totalCount,
    filteredCount,
    subjectStats,
    // actions
    loadFromStorage,
    saveToStorage,
    addWrong,
    removeWrong,
    clearBySubject,
    clearAll,
    hasWrong,
    setFilter,
  }
})
