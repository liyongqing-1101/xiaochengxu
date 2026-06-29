/**
 * 认证 API 模块
 */
import { post } from '@/utils/request'
import type { LoginResult } from '@/types/user'

export const authApi = {
  /**
   * 微信一键登录
   * POST /auth/login
   * @param code 微信 login code
   */
  login(code: string): Promise<LoginResult> {
    return post<LoginResult>('/auth/login', { code })
  },

  /**
   * 刷新 Token
   * POST /auth/refresh
   */
  refreshToken(): Promise<{ token: string }> {
    return post<{ token: string }>('/auth/refresh')
  },
}
