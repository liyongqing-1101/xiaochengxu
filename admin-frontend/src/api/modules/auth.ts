import { post } from '../request'
import type { ApiResponse } from '@/types/api'

export interface AdminLoginResult {
  token: string
  nickname: string
  role: string
}

export const authApi = {
  login(username: string, password: string): Promise<AdminLoginResult> {
    return post<AdminLoginResult>('/admin/auth/login', { username, password })
  },
}
