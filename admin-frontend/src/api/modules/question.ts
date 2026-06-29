import { get, post, del } from '../request'
import type { PageResult } from '@/types/api'
import type { Question, QuestionQuery } from '@/types/question'

export const questionApi = {
  getList(params: QuestionQuery): Promise<PageResult<Question>> {
    return get<PageResult<Question>>('/admin/questions/list', params)
  },
  save(data: Partial<Question>): Promise<void> {
    return post<void>('/admin/questions/save', data)
  },
  deleteById(id: number): Promise<void> {
    return del<void>(`/admin/questions/${id}`)
  },
  batchDelete(ids: number[]): Promise<void> {
    return post<void>('/admin/questions/batch-delete', { ids })
  },
  batchStatus(ids: number[], status: number): Promise<void> {
    return post<void>('/admin/questions/batch-status', { ids, status })
  },
}
