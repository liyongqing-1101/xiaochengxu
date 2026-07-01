import { post, get, del } from '../request'
import type { ExamQuestion, QuestionQuery, QuestionSaveDTO, PageResult, ImportResult } from '@/types/question'

/**
 * 题目管理API（简化版）
 * 已移除难度、章节、知识点相关参数
 */
export const questionApi = {
  /** 获取题目列表 */
  getList(params: QuestionQuery): Promise<PageResult<ExamQuestion>> {
    return get<PageResult<ExamQuestion>>('/admin/questions/list', params)
  },

  /** 获取单题详情 */
  getById(id: number): Promise<ExamQuestion> {
    return get<ExamQuestion>(`/admin/questions/${id}`)
  },

  /** 保存题目（新增或更新） */
  save(data: QuestionSaveDTO): Promise<void> {
    return post<void>('/admin/questions/save', data)
  },

  /** 删除单题 */
  deleteById(id: number): Promise<void> {
    return del<void>(`/admin/questions/${id}`)
  },

  /** 批量删除 */
  batchDelete(ids: number[]): Promise<void> {
    return post<void>('/admin/questions/batch-delete', ids)
  },

  /** 批量更新状态 */
  batchStatus(ids: number[], status: number): Promise<void> {
    return post<void>(`/admin/questions/batch-status?status=${status}`, {})
  },
}

/**
 * 导入API（单独分组）
 */
export const importApi = {
  /** 上传Excel导入 */
  upload(formData: FormData): Promise<ImportResult> {
    return post<ImportResult>('/admin/import/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  /** 下载导入模板 */
  downloadTemplate(): void {
    window.open('/api/admin/import/template', '_blank')
  },
}
