import { get, post, del } from '../request'
import type { ExamCategory } from '@/types/category'

// ========== 分类树 ==========
export function getCategoryTree(): Promise<ExamCategory[]> {
  return get<ExamCategory[]>('/admin/categories/tree')
}

// ========== 统一保存 ==========
function saveCategoryNode(data: {
  id?: number
  parentId?: number
  type: string
  name: string
  icon?: string
  description?: string
  sortOrder?: number
}): Promise<void> {
  return post<void>('/admin/categories/save', data)
}

function deleteCategoryNode(type: string, id: number): Promise<void> {
  return del<void>(`/admin/categories/${type}/${id}`)
}

// ========== Category (大类) ==========
export function createCategory(data: { name: string; icon?: string; description?: string; sortOrder?: number; status?: number }) {
  return saveCategoryNode({ ...data, type: 'category' })
}
export function updateCategory(id: number, data: { name?: string; icon?: string; description?: string; sortOrder?: number; status?: number }) {
  return saveCategoryNode({ ...data, id, type: 'category' })
}
export function deleteCategory(id: number) {
  return deleteCategoryNode('category', id)
}

// ========== Subject (科目) ==========
export function createSubject(data: { categoryId: number; name: string; icon?: string; sortOrder?: number; status?: number }) {
  return saveCategoryNode({ ...data, parentId: data.categoryId, type: 'subject' })
}
export function updateSubject(id: number, data: { name?: string; icon?: string; sortOrder?: number; status?: number }) {
  return saveCategoryNode({ ...data, id, type: 'subject' })
}
export function deleteSubject(id: number) {
  return deleteCategoryNode('subject', id)
}

// ========== Chapter (章节) ==========
export function createChapter(data: { subjectId: number; name: string; sortOrder?: number; status?: number }) {
  return saveCategoryNode({ ...data, parentId: data.subjectId, type: 'chapter' })
}
export function updateChapter(id: number, data: { name?: string; sortOrder?: number; status?: number }) {
  return saveCategoryNode({ ...data, id, type: 'chapter' })
}
export function deleteChapter(id: number) {
  return deleteCategoryNode('chapter', id)
}

// ========== Tag (知识点) ==========
export function createTag(data: { chapterId: number; name: string; sortOrder?: number; status?: number }) {
  return saveCategoryNode({ ...data, parentId: data.chapterId, type: 'tag' })
}
export function updateTag(id: number, data: { name?: string; sortOrder?: number; status?: number }) {
  return saveCategoryNode({ ...data, id, type: 'tag' })
}
export function deleteTag(id: number) {
  return deleteCategoryNode('tag', id)
}

// ========== 兼容旧接口 ==========
export const categoryApi = {
  getTree: getCategoryTree,
  save: saveCategoryNode,
  deleteNode: deleteCategoryNode,
}
