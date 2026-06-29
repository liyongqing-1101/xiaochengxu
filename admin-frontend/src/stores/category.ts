import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCategoryTree } from '@/api/modules/category'
import type { Category } from '@/types/category'

export interface FlatSubject {
  id: number
  name: string
  categoryId: number
}
export interface FlatChapter {
  id: number
  name: string
  subjectId: number
}
export interface FlatTag {
  id: number
  name: string
  chapterId: number
}

export const useCategoryStore = defineStore('category', () => {
  const tree = ref<Category[]>([])
  const loading = ref(false)

  // 扁平化的下级数据，用于筛选下拉框
  const subjects = ref<FlatSubject[]>([])
  const chapters = ref<FlatChapter[]>([])
  const tags = ref<FlatTag[]>([])

  async function fetchCategoryTree(forceRefresh = false) {
    if (!forceRefresh && tree.value.length > 0) return  // 已加载，跳过
    loading.value = true
    try {
      const res = await getCategoryTree()
      tree.value = res || []

      // 扁平化处理：从树形结构提取各级数据
      const subjList: FlatSubject[] = []
      const chapList: FlatChapter[] = []
      const tagList: FlatTag[] = []

      for (const cat of tree.value) {
        const examSubjects = (cat as any).examSubjects || (cat as any).subjects || []
        for (const subj of examSubjects) {
          subjList.push({ id: subj.id, name: subj.name, categoryId: cat.id })

          const examChapters = (subj as any).examChapters || (subj as any).chapters || []
          for (const chap of examChapters) {
            chapList.push({ id: chap.id, name: chap.name, subjectId: subj.id })

            const examTags = (chap as any).examTags || (chap as any).tags || []
            for (const tag of examTags) {
              tagList.push({ id: tag.id, name: tag.name, chapterId: chap.id })
            }
          }
        }
      }

      subjects.value = subjList
      chapters.value = chapList
      tags.value = tagList
    } finally {
      loading.value = false
    }
  }

  // 根据 subjectId 获取对应的 chapters
  function getChaptersBySubject(subjectId: number | undefined) {
    if (!subjectId) return chapters.value
    return chapters.value.filter(c => c.subjectId === subjectId)
  }

  // 根据 chapterId 获取对应的 tags
  function getTagsByChapter(chapterId: number | undefined) {
    if (!chapterId) return tags.value
    return tags.value.filter(t => t.chapterId === chapterId)
  }

  return {
    tree, loading, subjects, chapters, tags,
    fetchCategoryTree, getChaptersBySubject, getTagsByChapter,
  }
})