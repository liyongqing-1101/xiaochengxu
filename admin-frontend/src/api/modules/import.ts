import { get, post } from '../request'
import type { PageResult } from '@/types/api'
import type { ImportTask, ImportProgress } from '@/types/import'

export const importApi = {
  downloadTemplate(): Promise<Blob> {
    return get<Blob>('/admin/import/template', { responseType: 'blob' })
  },
  uploadFile(file: File, categoryId: number): Promise<{ taskId: number }> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('categoryId', String(categoryId))
    return post<{ taskId: number }>('/admin/import/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  getProgress(taskId: number): Promise<ImportProgress> {
    return get<ImportProgress>(`/admin/import/progress/${taskId}`)
  },
  getTasks(page: number, pageSize: number): Promise<PageResult<ImportTask>> {
    return get<PageResult<ImportTask>>('/admin/import/tasks', { page, pageSize })
  },
  downloadErrorFile(taskId: number): Promise<Blob> {
    return get<Blob>(`/admin/import/error-file/${taskId}`, { responseType: 'blob' })
  },
}
