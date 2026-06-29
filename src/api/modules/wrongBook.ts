/**
 * 错题本 API 模块
 */
import { get, post, del } from '@/utils/request'
import type { WrongQuestion } from '@/types/question'
import type { PaginatedResult, PaginatedParams } from '@/types/api'

export const wrongBookApi = {
  /**
   * 同步错题到服务端
   * POST /wrong-book/sync
   */
  syncWrongQuestions(questions: WrongQuestion[]): Promise<{ synced: number }> {
    return post<{ synced: number }>('/wrong-book/sync', {
      questions: questions.map(w => ({
        questionId: w.question.id,
        errorCount: w.errorCount,
        lastWrongAnswer: w.lastWrongAnswer,
        lastErrorTime: w.lastErrorTime,
      })),
    })
  },

  /**
   * 获取服务端错题列表
   * GET /wrong-book/list?categoryId=1&subjectId=2&page=1&pageSize=20
   */
  getWrongList(params: PaginatedParams): Promise<PaginatedResult<WrongQuestion>> {
    return get<PaginatedResult<WrongQuestion>>('/wrong-book/list', params as any)
  },

  /**
   * 从服务端删除错题
   * DELETE /wrong-book/remove?questionId=123
   */
  removeWrong(questionId: number): Promise<void> {
    return del<void>('/wrong-book/remove', { questionId })
  },
}
