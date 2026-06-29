import { get, post, del } from '../request'
import type { ExamCategory } from '@/types/category'

export const categoryApi = {
  getTree(): Promise<ExamCategory[]> {
    return get<ExamCategory[]>('/admin/categories/tree')
  },
  save(data: {
    id?: number
    parentId?: number
    type: string
    name: string
    icon?: string
    description?: string
    sortOrder?: number
  }): Promise<void> {
    return post<void>('/admin/categories/save', data)
  },
  deleteNode(type: string, id: number): Promise<void> {
    return del<void>(`/admin/categories/${type}/${id}`)
  },
}
