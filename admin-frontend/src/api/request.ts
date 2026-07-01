import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types/api'

// 管理后台使用 HttpSession 认证，不需要 JWT Token
// Cookie 会自动携带 JSESSIONID
const instance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,  // 必须开启，才能携带 Cookie (JSESSIONID)
})

// 请求拦截器
instance.interceptors.request.use((config) => {
  // Session 模式：不需要手动添加 Token，Cookie 自动携带
  return config
})

// 响应拦截器 - 统一处理错误
instance.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResponse<any>
    if (res.code !== 0) {
      ElMessage.error(res.message || '请求失败')
      // Session 过期，跳转到登录页
      if (res.code === 401) {
        localStorage.removeItem('admin_user')
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.message))
    }
    return res.data
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('admin_user')
      window.location.href = '/login'
    }
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  },
)

export function get<T>(url: string, paramsOrConfig?: any): Promise<T> {
  // Support both { params } and axios config with responseType etc.
  if (paramsOrConfig && (paramsOrConfig.responseType !== undefined || paramsOrConfig.headers !== undefined)) {
    return instance.get(url, paramsOrConfig)
  }
  return instance.get(url, { params: paramsOrConfig })
}

export function post<T>(url: string, data?: any, config?: any): Promise<T> {
  return instance.post(url, data, config)
}

export function put<T>(url: string, data?: any): Promise<T> {
  return instance.put(url, data)
}

export function del<T>(url: string, params?: any): Promise<T> {
  return instance.delete(url, { params })
}

export default instance
