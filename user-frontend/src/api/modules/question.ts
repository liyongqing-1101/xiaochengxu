/**
 * 题目 API 模块
 */
import { get, post, del } from '@/utils/request'
import type { Question, QuestionSession, StartSessionParams, SubmitResult, SessionSummary, SubmitAnswerParams, DailyQuestion, SubjectStats } from '@/types/question'
import type { PaginatedResult, PaginatedParams } from '@/types/api'

export const questionApi = {
  /**
   * 开始答题会话
   * POST /question/session/start
   */
  startSession(params: StartSessionParams): Promise<QuestionSession> {
    return post<QuestionSession>('/question/session/start', params as any)
  },

  /**
   * 提交答案
   * POST /question/submit
   */
  submitAnswer(params: SubmitAnswerParams): Promise<SubmitResult> {
    return post<SubmitResult>('/question/submit', params as any)
  },

  /**
   * 结束答题会话
   * POST /question/session/end
   */
  endSession(sessionId: string): Promise<SessionSummary> {
    return post<SessionSummary>('/question/session/end', { sessionId })
  },

  /**
   * 分页获取题目列表
   * GET /question/list?categoryId=1&subjectId=2&page=1&pageSize=20
   */
  getQuestionList(params: PaginatedParams): Promise<PaginatedResult<Question>> {
    return get<PaginatedResult<Question>>('/question/list', params as any)
  },

  /**
   * 收藏题目
   * POST /question/collect
   */
  collectQuestion(questionId: number): Promise<void> {
    return post<void>('/question/collect', { questionId })
  },

  /**
   * 取消收藏
   * DELETE /question/collect?questionId=123
   */
  uncollectQuestion(questionId: number): Promise<void> {
    return del<void>('/question/collect', { questionId })
  },

  /**
   * 获取收藏列表
   * GET /question/collected?categoryId=1&page=1&pageSize=20
   */
  getCollectedQuestions(params: PaginatedParams): Promise<PaginatedResult<Question>> {
    return get<PaginatedResult<Question>>('/question/collected', params as any)
  },

  /**
   * 获取每日一题
   * GET /question/daily?categoryId=1
   */
  getDailyQuestion(categoryId: number): Promise<DailyQuestion> {
    return get<DailyQuestion>('/question/daily', { categoryId })
  },

  /**
   * 搜索题目
   * GET /question/search?keyword=xxx&categoryId=1&page=1&pageSize=20
   */
  searchQuestions(
    keyword: string,
    params: PaginatedParams,
  ): Promise<PaginatedResult<Question>> {
    return get<PaginatedResult<Question>>('/question/search', {
      keyword,
      ...params,
    } as any)
  },

  /**
   * 获取科目题目统计（按题型）
   * GET /question/subject/{subjectId}/stats
   */
  getSubjectStats(subjectId: number): Promise<SubjectStats> {
    return get<SubjectStats>(`/question/subject/${subjectId}/stats`)
  },
}
