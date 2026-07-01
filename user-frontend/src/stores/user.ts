/**
 * 用户状态管理 Store
 *
 * 管理:
 * - JWT Token 及持久化
 * - 用户基本信息
 * - 刷题统计数据
 * - 登录/登出/会话恢复
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, UserStats, LoginResult } from '@/types/user'
import { storage } from '@/utils/storage'
import { StorageKey } from '@/utils/constants'
import { authApi } from '@/api/modules/auth'
import { userApi } from '@/api/modules/user'
import { get, post, put } from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  // ═══════════════════════════════════════
  // State
  // ═══════════════════════════════════════
  const token = ref<string | null>(null)
  const userInfo = ref<UserInfo | null>(null)
  const stats = ref<UserStats | null>(null)

  // ═══════════════════════════════════════
  // Getters
  // ═══════════════════════════════════════
  const isLoggedIn = computed(() => !!token.value)
  const avatar = computed(() => userInfo.value?.avatar || '/static/images/default-avatar.png')
  const nickname = computed(() => userInfo.value?.nickname || '微信用户')
  const isVip = computed(() => userInfo.value?.membership === 'vip')
  const todayCheckedIn = computed(() => stats.value?.todayCheckedIn ?? false)
  const streakDays = computed(() => stats.value?.streakDays ?? 0)
  const totalQuestions = computed(() => stats.value?.totalQuestions ?? 0)
  const accuracy = computed(() => stats.value?.accuracy ?? 0)

  // ═══════════════════════════════════════
  // Actions
  // ═══════════════════════════════════════

  /**
   * 从本地存储恢复登录会话
   */
  function restoreSession(): void {
    const savedToken = storage.get<string>(StorageKey.TOKEN)
    const savedInfo = storage.get<UserInfo>(StorageKey.USER_INFO)

    if (savedToken) {
      token.value = savedToken
    }
    if (savedInfo) {
      userInfo.value = savedInfo
    }
  }

  /**
   * 微信一键登录
   */
  async function login(): Promise<void> {
    return new Promise((resolve, reject) => {
      uni.login({
        provider: 'weixin',
        success: async (loginRes) => {
          try {
            const result = await post<LoginResult>('/auth/login', {
              code: loginRes.code,
            })

            token.value = result.token
            userInfo.value = result.userInfo

            // 持久化
            storage.set(StorageKey.TOKEN, result.token)
            storage.set(StorageKey.USER_INFO, result.userInfo)

            resolve()
          } catch (err) {
            uni.showToast({ title: '登录失败,请重试', icon: 'none' })
            reject(err)
          }
        },
        fail: (err) => {
          console.error('wx.login fail:', err)
          uni.showToast({ title: '微信授权失败', icon: 'none' })
          reject(err)
        },
      })
    })
  }

  /**
   * 用户名密码登录
   *
   * 流程: 调用 API → 校验响应数据 → 写入 state → 持久化 storage
   */
  async function loginByPassword(username: string, password: string): Promise<void> {
    // 参数校验
    if (!username || !password) {
      throw new Error('用户名和密码不能为空')
    }

    console.log('[Store] loginByPassword 请求:', { username, password: '***' })

    // 调用 API（拦截器已解包 {code,data} → 直接返回 data 对象）
    const result = await authApi.loginByUsername(username, password)

    console.log('[Store] loginByPassword 后端返回:', {
      hasToken: !!result?.token,
      tokenPrefix: result?.token ? result.token.substring(0, 20) + '...' : 'MISSING',
      hasUserInfo: !!result?.userInfo,
      hasRefreshToken: !!result?.refreshToken,
    })

    // 数据完整性校验
    if (!result || !result.token || !result.userInfo) {
      console.error('[Store] loginByPassword 响应数据不完整:', result)
      throw new Error('登录返回数据异常，请重试')
    }

    // 写入 Pinia state
    token.value = result.token
    userInfo.value = result.userInfo

    // 持久化到本地缓存
    storage.set(StorageKey.TOKEN, result.token)
    storage.set(StorageKey.USER_INFO, result.userInfo)
    if (result.refreshToken) {
      storage.set(StorageKey.REFRESH_TOKEN, result.refreshToken)
    }

    console.log('[Store] loginByPassword ✅ 登录态已保存')
  }

  /**
   * 用户注册
   */
  async function register(username: string, password: string, confirmPassword: string): Promise<void> {
    await authApi.register(username, password, confirmPassword)
  }

  /**
   * 获取用户信息
   */
  async function fetchUserInfo(): Promise<void> {
    try {
      const info = await get<UserInfo>('/user/info')
      userInfo.value = info
      storage.set(StorageKey.USER_INFO, info)
    } catch {
      // 静默失败, 使用缓存数据
    }
  }

  /**
   * 获取用户统计数据
   */
  async function fetchStats(): Promise<void> {
    try {
      const examStore = (await import('./exam')).useExamStore()
      const data = await get<UserStats>('/user/stats', {
        categoryId: examStore.currentCategoryId,
      })
      stats.value = data
    } catch {
      // 静默失败
    }
  }

  /**
   * 每日打卡
   */
  async function checkIn(): Promise<boolean> {
    try {
      await post('/user/check-in')
      // 刷新统计数据
      await fetchStats()
      uni.showToast({ title: '打卡成功! 🎉', icon: 'success' })
      return true
    } catch {
      return false
    }
  }

  /**
   * 登出
   */
  async function logout(): Promise<void> {
    console.log('[Store] 开始退出登录流程')
    try {
      // 调用后端logout接口，将token加入黑名单
      await userApi.logout()
      console.log('[Store] 后端登出接口调用成功')
    } catch (err) {
      console.warn('[Store] 后端登出接口调用失败，本地强制清除:', err)
    } finally {
      // 无论接口调用成功与否，都清除本地登录状态
      token.value = null
      userInfo.value = null
      stats.value = null

      // 清除本地存储中的认证信息
      storage.remove(StorageKey.TOKEN)
      storage.remove(StorageKey.USER_INFO)
      storage.remove(StorageKey.REFRESH_TOKEN)
      console.log('[Store] 本地登录态已清除')
    }
  }

  /**
   * 更新用户信息(本地 + 远程)
   */
  async function updateProfile(data: Partial<Pick<UserInfo, 'nickname' | 'avatar'>>): Promise<void> {
    try {
      await put('/user/info', data)
      if (userInfo.value) {
        Object.assign(userInfo.value, data)
        storage.set(StorageKey.USER_INFO, userInfo.value)
      }
      uni.showToast({ title: '更新成功', icon: 'success' })
    } catch {
      uni.showToast({ title: '更新失败', icon: 'none' })
    }
  }

  return {
    // state
    token,
    userInfo,
    stats,
    // getters
    isLoggedIn,
    avatar,
    nickname,
    isVip,
    todayCheckedIn,
    streakDays,
    totalQuestions,
    accuracy,
    // actions
    restoreSession,
    login,
    loginByPassword,
    register,
    fetchUserInfo,
    fetchStats,
    checkIn,
    logout,
    updateProfile,
  }
})
