/**
 * API 请求拦截器
 *
 * 使用 uni.addInterceptor 对 uni.request 进行全局拦截:
 * 1. 请求阶段: 自动注入 Bearer token + X-Category-Id header
 * 2. 响应阶段: 统一错误处理, 401 自动跳转登录
 *
 * 注意: 拦截器依赖 Pinia store, 需要在 App.vue onLaunch 中注册
 */

import { useUserStore } from '@/stores/user'
import { useExamStore } from '@/stores/exam'
import type { ApiResponse } from '@/types/api'

/** 拦截器是否已注册 */
let registered = false

/**
 * 注册全局请求拦截器
 * 应在 App.vue onLaunch 中调用一次
 */
export function registerInterceptors(): void {
  if (registered) return
  registered = true

  // --- 请求拦截 ---
  uni.addInterceptor('request', {
    invoke(args) {
      // 获取 token
      const userStore = useUserStore()
      const token = userStore.token

      if (token && args.url && !args.url.startsWith('https://')) {
        // 注入 Authorization header
        if (!args.header) {
          args.header = {}
        }
        ;(args.header as Record<string, string>)['Authorization'] = `Bearer ${token}`

        // 注入 X-Category-Id
        const examStore = useExamStore()
        ;(args.header as Record<string, string>)['X-Category-Id'] =
          String(examStore.currentCategoryId)
      }
    },
    fail(err) {
      console.error('[Interceptor] Request failed:', err)
    },
    complete() {
      // 请求完成时的清理工作(如有需要)
    },
  })

  // --- uploadFile 拦截 (同样注入 token) ---
  uni.addInterceptor('uploadFile', {
    invoke(args) {
      const userStore = useUserStore()
      const token = userStore.token

      if (token) {
        if (!args.header) {
          args.header = {}
        }
        ;(args.header as Record<string, string>)['Authorization'] = `Bearer ${token}`
      }
    },
  })

  // --- 响应拦截 (通过包装 uni.request 实现) ---
  wrapRequestWithResponseInterceptor()
}

/**
 * 包装 uni.request 添加响应拦截逻辑
 * 处理 401 跳转登录、业务错误码等
 */
function wrapRequestWithResponseInterceptor(): void {
  const originalRequest = uni.request.bind(uni)

  uni.request = function (options) {
    const originalSuccess = options.success
    const originalFail = options.fail

    options.success = function (res) {
      // HTTP 401 处理
      if (res.statusCode === 401) {
        handleUnauthorized()
        originalFail?.call(this, { errMsg: 'request:fail Unauthorized' } as any)
        return
      }

      // 业务错误码处理
      const body = res.data as ApiResponse
      if (body && typeof body === 'object' && 'code' in body) {
        if (body.code !== 0) {
          // 业务错误
          handleBusinessError(body)
          originalFail?.call(this, { errMsg: body.message } as any)
          return
        }
      }

      originalSuccess?.call(this, res)
    }

    options.fail = function (err) {
      // 网络错误已由 request.ts 重试逻辑处理
      // 这里只记录日志
      console.error('[Interceptor] Request network error:', err.errMsg)
      originalFail?.call(this, err)
    }

    return originalRequest(options)
  } as typeof uni.request
}

/**
 * 处理 401 未授权
 */
function handleUnauthorized(): void {
  const userStore = useUserStore()
  userStore.logout()

  uni.reLaunch({
    url: '/pages/login/index',
  })
}

/**
 * 处理业务错误码
 */
function handleBusinessError(body: ApiResponse): void {
  const errorMessages: Record<number, string> = {
    1001: '参数错误',
    1002: 'Token 已过期',
    1003: '账号已被禁用',
    2001: '题目不存在',
    2002: '会话已结束',
  }

  const msg = errorMessages[body.code] || body.message || '请求失败'
  uni.showToast({ title: msg, icon: 'none', duration: 2000 })
}
