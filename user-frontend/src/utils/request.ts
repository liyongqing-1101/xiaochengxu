/**
 * HTTP 请求封装
 *
 * 基于 uni.request 二次封装:
 * - 请求重试 (网络错误)
 * - 加载状态管理
 * - 统一错误转换
 */

import { API } from './constants'

/** 请求配置 */
export interface RequestConfig {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: Record<string, unknown>
  params?: Record<string, unknown>
  header?: Record<string, string>
  /** 是否显示加载提示 */
  showLoading?: boolean
  /** 加载提示文字 */
  loadingText?: string
  /** 重试次数 */
  retryCount?: number
  /** 跳过 token 注入 */
  skipAuth?: boolean
}

/** 请求计数器(控制 loading 显示) */
let requestCount = 0

/**
 * 显示加载
 */
function showLoading(text: string): void {
  if (requestCount === 0) {
    uni.showLoading({ title: text, mask: true })
  }
  requestCount++
}

/**
 * 隐藏加载
 */
function hideLoading(): void {
  requestCount--
  if (requestCount <= 0) {
    requestCount = 0
    uni.hideLoading()
  }
}

/**
 * 构建完整 URL
 */
function buildUrl(url: string, params?: Record<string, unknown>): string {
  const baseURL = API.BASE_URL
  const fullUrl = url.startsWith('http') ? url : baseURL + url

  if (!params || Object.keys(params).length === 0) return fullUrl

  const query = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== '')
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
    .join('&')

  return query ? `${fullUrl}?${query}` : fullUrl
}

/**
 * 执行请求 (带重试)
 */
function doRequest<T>(config: RequestConfig, retryLeft: number): Promise<T> {
  const {
    url,
    method = 'GET',
    data,
    params,
    header = {},
    showLoading: shouldLoad = false,
    loadingText = '加载中...',
  } = config

  const fullUrl = buildUrl(url, params)

  // 打印请求日志
  console.log(`[HTTP] ${method} ${fullUrl}`, {
    请求参数: params,
    请求体: data,
    请求头: header,
    剩余重试次数: retryLeft
  })

  if (shouldLoad) {
    showLoading(loadingText)
  }

  return new Promise<T>((resolve, reject) => {
    uni.request({
      url: fullUrl,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        ...header,
      },
      timeout: API.TIMEOUT,
      success: (res) => {
        if (shouldLoad) hideLoading()

        // 打印响应日志
        console.log(`[HTTP] ✅ ${method} ${fullUrl} 响应成功`, {
          状态码: res.statusCode,
          响应数据: res.data
        })

        // HTTP 状态码检查
        if (res.statusCode === 401) {
          // Token 过期由拦截器处理, 这里静默失败
          console.warn(`[HTTP] ❌ ${method} ${fullUrl} 401未授权`)
          reject(new Error('LOGIN_EXPIRED'))
          return
        }

        if (res.statusCode && res.statusCode >= 500) {
          // 服务端错误可重试
          console.warn(`[HTTP] ❌ ${method} ${fullUrl} 服务器错误 status=${res.statusCode}`)
          if (retryLeft > 0) {
            setTimeout(() => {
              doRequest<T>(config, retryLeft - 1).then(resolve).catch(reject)
            }, API.RETRY_DELAY)
          } else {
            uni.showToast({ title: '服务器繁忙,请稍后重试', icon: 'none' })
            reject(new Error('SERVER_ERROR'))
          }
          return
        }

        resolve(res.data as T)
      },
      fail: (err) => {
        if (shouldLoad) hideLoading()

        // 打印错误日志
        console.error(`[HTTP] ❌ ${method} ${fullUrl} 请求失败`, err)

        // 网络错误重试
        if (retryLeft > 0) {
          console.warn(`[HTTP] 🔄 ${method} ${fullUrl} 准备重试，剩余次数: ${retryLeft - 1}`)
          setTimeout(() => {
            doRequest<T>(config, retryLeft - 1).then(resolve).catch(reject)
          }, API.RETRY_DELAY)
        } else {
          uni.showToast({ title: '网络异常,请检查网络连接', icon: 'none' })
          reject(err)
        }
      },
    })
  })
}

/**
 * GET 请求
 */
export function get<T>(
  url: string,
  params?: Record<string, unknown>,
  config?: Partial<RequestConfig>,
): Promise<T> {
  return doRequest<T>(
    { url, method: 'GET', params, ...config },
    config?.retryCount ?? API.RETRY_COUNT,
  )
}

/**
 * POST 请求
 */
export function post<T>(
  url: string,
  data?: Record<string, unknown>,
  config?: Partial<RequestConfig>,
): Promise<T> {
  return doRequest<T>(
    { url, method: 'POST', data, ...config },
    config?.retryCount ?? API.RETRY_COUNT,
  )
}

/**
 * PUT 请求
 */
export function put<T>(
  url: string,
  data?: Record<string, unknown>,
  config?: Partial<RequestConfig>,
): Promise<T> {
  return doRequest<T>(
    { url, method: 'PUT', data, ...config },
    config?.retryCount ?? API.RETRY_COUNT,
  )
}

/**
 * DELETE 请求
 */
export function del<T>(
  url: string,
  params?: Record<string, unknown>,
  config?: Partial<RequestConfig>,
): Promise<T> {
  return doRequest<T>(
    { url, method: 'DELETE', params, ...config },
    config?.retryCount ?? API.RETRY_COUNT,
  )
}
