/**
 * 考试分类切换逻辑
 *
 * [EXTENSION-POINT] 新增考试分类无需修改此文件
 */
import { useExamStore } from '@/stores/exam'

export function useCategory() {
  const examStore = useExamStore()

  function switchCategory(id: number): void {
    examStore.switchCategory(id)
    // 切换分类后刷新相关数据
    uni.showToast({ title: `已切换到${examStore.currentCategoryMeta.name}`, icon: 'success' })
  }

  return {
    currentCategoryId: examStore.currentCategoryId,
    currentCategoryMeta: examStore.currentCategoryMeta,
    subjects: examStore.subjects,
    hasMultipleCategories: examStore.hasMultipleCategories,
    switchCategory,
    fetchCategories: examStore.fetchCategories,
  }
}
