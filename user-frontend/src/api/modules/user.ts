/**
 * 用户 API 模块
 */
import { get, post, put } from '@/utils/request'
import type { UserInfo, UserStats, CheckInResult, PracticeHistory } from '@/types/user'
import type { PaginatedResult, PaginatedParams } from '@/types/api'

export const userApi = {
  /**
   * 获取用户信息
   * GET /user/info
   */
  getUserInfo(): Promise<UserInfo> {
    return get<UserInfo>('/user/info')
  },

  /**
   * 更新用户信息
   * PUT /user/info
   */
  updateUserInfo(data: Partial<Pick<UserInfo, 'nickname' | 'avatar'>>): Promise<void> {
    return put<void>('/user/info', data as any)
  },

  /**
   * 获取用户统计数据
   * GET /user/stats?categoryId=1
   */
  getStats(categoryId: number): Promise<UserStats> {
    return get<UserStats>('/user/stats', { categoryId })
  },

  /**
   * 每日打卡
   * POST /user/check-in
   */
  checkIn(): Promise<CheckInResult> {
    return post<CheckInResult>('/user/check-in')
  },

  /**
   * 获取练习历史
   * GET /user/history?categoryId=1&page=1&pageSize=20
   */
  getHistory(params: PaginatedParams): Promise<PaginatedResult<PracticeHistory>> {
    return get<PaginatedResult<PracticeHistory>>('/user/history', params as any)
  },

  /**
   * 意见反馈
   * POST /user/feedback
   */
  submitFeedback(content: string, contact?: string): Promise<void> {
    return post<void>('/user/feedback', { content, contact })
  },
}
