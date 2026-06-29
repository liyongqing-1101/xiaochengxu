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
   * 用户名密码登录
   * POST /auth/login/username
   */
  loginByUsername(username: string, password: string): Promise<LoginResult> {
    return post<LoginResult>('/auth/login/username', { username, password })
  },

  /**
   * 用户注册
   * POST /auth/register
   */
  register(username: string, password: string, confirmPassword: string): Promise<void> {
    return post<void>('/auth/register', { username, password, confirmPassword })
  },

  /**
   * 刷新 Token
   * POST /auth/refresh
   */
  refreshToken(): Promise<{ token: string }> {
    return post<{ token: string }>('/auth/refresh')
  },
}
