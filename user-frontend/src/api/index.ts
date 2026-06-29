/**
 * API 客户端统一导出
 *
 * 使用方式:
 *   import { get, post } from '@/api'
 *   或
 *   import { authApi, examApi } from '@/api'
 */

export { get, post, put, del } from '@/utils/request'
export { registerInterceptors } from './interceptors'
