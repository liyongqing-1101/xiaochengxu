import { post, get } from '../request'

export interface AdminLoginResult {
  nickname: string
  role: string
  sessionId: string
}

export interface AdminCheckResult {
  nickname: string
  role: string
  username: string
}

export const authApi = {
  login(username: string, password: string): Promise<AdminLoginResult> {
    return post<AdminLoginResult>('/admin/auth/login', { username, password })
  },

  checkLogin(): Promise<AdminCheckResult> {
    return get<AdminCheckResult>('/admin/auth/check')
  },

  logout(): Promise<void> {
    return post<void>('/admin/auth/logout')
  },
}
