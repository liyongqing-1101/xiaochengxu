/**
 * 考试分类状态管理 Store
 *
 * 管理:
 * - 当前考试大类 (categoryId)
 * - 全部分类/科目/章节缓存 (30分钟 TTL)
 * - 分类切换逻辑
 *
 * [EXTENSION-POINT] 新增考试分类无需修改此文件
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { ExamCategory, Subject, Chapter, KnowledgePoint } from '@/types/exam'
import { CategoryId } from '@/types/enums'
import { storage } from '@/utils/storage'
import { StorageKey, CACHE, APP } from '@/utils/constants'
import { getCategoryMeta } from '@/config/category.config'

export const useExamStore = defineStore('exam', () => {
  // ═══════════════════════════════════════
  // State
  // ═══════════════════════════════════════
  const currentCategoryId = ref<number>(
    storage.get<number>(StorageKey.CATEGORY_ID) ?? APP.DEFAULT_CATEGORY_ID,
  )
  const categories = ref<ExamCategory[]>([])
  const loading = ref(false)

  // ═══════════════════════════════════════
  // Getters
  // ═══════════════════════════════════════
  /** 当前分类元信息 */
  const currentCategoryMeta = computed(() => getCategoryMeta(currentCategoryId.value))

  /** 当前分类完整数据 */
  const currentCategory = computed(() =>
    categories.value.find(c => c.id === currentCategoryId.value),
  )

  /** 当前分类下的科目列表 */
  const subjects = computed(() => currentCategory.value?.subjects || [])

  /** 分类总数(控制分类切换按钮显隐) */
  const hasMultipleCategories = computed(() => categories.value.length > 1)

  // ═══════════════════════════════════════
  // Actions
  // ═══════════════════════════════════════

  /**
   * 获取所有考试分类及科目
   * 结果缓存 30 分钟
   */
  async function fetchCategories(): Promise<void> {
    // 检查缓存
    const cached = storage.getWithTTL<ExamCategory[]>('categories')
    if (cached) {
      categories.value = cached
      return
    }

    loading.value = true
    try {
      const { get } = await import('@/utils/request')
      const data = await get<ExamCategory[]>('/exam/categories')
      categories.value = data

      // 缓存 30 分钟
      storage.setWithTTL('categories', data, CACHE.CATEGORY_TTL)
    } catch (err) {
      console.error('[ExamStore] fetchCategories error:', err)
    } finally {
      loading.value = false
    }
  }

  /**
   * 切换考试分类
   * @param id 目标分类 ID
   */
  function switchCategory(id: number): void {
    if (id === currentCategoryId.value) return

    currentCategoryId.value = id
    storage.set(StorageKey.CATEGORY_ID, id)
  }

  /**
   * 获取科目列表(带缓存)
   */
  async function fetchSubjects(categoryId?: number): Promise<Subject[]> {
    const cid = categoryId ?? currentCategoryId.value

    // 从 categories 缓存中获取
    const cat = categories.value.find(c => c.id === cid)
    if (cat?.subjects?.length) return cat.subjects

    try {
      const { get } = await import('@/utils/request')
      return await get<Subject[]>('/exam/subjects', { categoryId: cid })
    } catch {
      return []
    }
  }

  /**
   * 获取章节列表
   */
  async function fetchChapters(subjectId: number): Promise<Chapter[]> {
    try {
      const { get } = await import('@/utils/request')
      return await get<Chapter[]>('/exam/chapters', {
        categoryId: currentCategoryId.value,
        subjectId,
      })
    } catch {
      return []
    }
  }

  /**
   * 获取知识点列表
   */
  async function fetchKnowledgePoints(
    subjectId: number,
    chapterId: number,
  ): Promise<KnowledgePoint[]> {
    try {
      const { get } = await import('@/utils/request')
      return await get<KnowledgePoint[]>('/exam/knowledge-points', {
        categoryId: currentCategoryId.value,
        subjectId,
        chapterId,
      })
    } catch {
      return []
    }
  }

  return {
    // state
    currentCategoryId,
    categories,
    loading,
    // getters
    currentCategoryMeta,
    currentCategory,
    subjects,
    hasMultipleCategories,
    // actions
    fetchCategories,
    switchCategory,
    fetchSubjects,
    fetchChapters,
    fetchKnowledgePoints,
  }
})
