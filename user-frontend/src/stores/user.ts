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
            // 调用后端 JWT 登录接口
            const { post } = await import('@/utils/request')
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
   */
  async function loginByPassword(username: string, password: string): Promise<void> {
    const { authApi } = await import('@/api/modules/auth')
    const result = await authApi.loginByUsername(username, password)

    token.value = result.token
    userInfo.value = result.userInfo

    // 持久化
    storage.set(StorageKey.TOKEN, result.token)
    storage.set(StorageKey.USER_INFO, result.userInfo)
  }

  /**
   * 用户注册
   */
  async function register(username: string, password: string, confirmPassword: string): Promise<void> {
    const { authApi } = await import('@/api/modules/auth')
    await authApi.register(username, password, confirmPassword)
  }

  /**
   * 获取用户信息
   */
  async function fetchUserInfo(): Promise<void> {
    try {
      const { get } = await import('@/utils/request')
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
      const { get } = await import('@/utils/request')
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
      const { post } = await import('@/utils/request')
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
  function logout(): void {
    token.value = null
    userInfo.value = null
    stats.value = null

    // 清除本地存储中的认证信息
    storage.remove(StorageKey.TOKEN)
    storage.remove(StorageKey.USER_INFO)
  }

  /**
   * 更新用户信息(本地 + 远程)
   */
  async function updateProfile(data: Partial<Pick<UserInfo, 'nickname' | 'avatar'>>): Promise<void> {
    try {
      const { put } = await import('@/utils/request')
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
