/**
 * 登录认证逻辑
 */
import { useUserStore } from '@/stores/user'

export function useAuth() {
  const userStore = useUserStore()

  /**
   * 微信一键登录
   */
  async function wxLogin(): Promise<boolean> {
    try {
      await userStore.login()
      return true
    } catch {
      return false
    }
  }

  /**
   * 检查登录状态, 未登录则跳转
   */
  function requireAuth(): boolean {
    if (!userStore.isLoggedIn) {
      uni.reLaunch({ url: '/pages/login/index' })
      return false
    }
    return true
  }

  return {
    isLoggedIn: userStore.isLoggedIn,
    wxLogin,
    requireAuth,
  }
}
