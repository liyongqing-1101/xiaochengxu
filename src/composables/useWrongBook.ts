/**
 * 错题本逻辑
 */
import { useWrongBookStore } from '@/stores/wrongBook'
import type { Question } from '@/types/question'

export function useWrongBook() {
  const wrongBookStore = useWrongBookStore()

  /**
   * 添加错题
   */
  function addWrong(question: Question, userAnswer: string[]): void {
    wrongBookStore.addWrong(question, userAnswer)
  }

  /**
   * 移除错题
   */
  function removeWrong(questionId: number): void {
    wrongBookStore.removeWrong(questionId)
  }

  /**
   * 检查是否在错题本中
   */
  function isWrong(questionId: number): boolean {
    return wrongBookStore.hasWrong(questionId)
  }

  /**
   * 设置科目筛选
   */
  function filterBySubject(subjectId: number | null): void {
    wrongBookStore.setFilter(subjectId)
  }

  return {
    wrongList: wrongBookStore.wrongList,
    filteredList: wrongBookStore.filteredList,
    totalCount: wrongBookStore.totalCount,
    subjectStats: wrongBookStore.subjectStats,
    filterSubjectId: wrongBookStore.filterSubjectId,
    addWrong,
    removeWrong,
    isWrong,
    filterBySubject,
    clearAll: wrongBookStore.clearAll,
    loadFromStorage: wrongBookStore.loadFromStorage,
  }
}
