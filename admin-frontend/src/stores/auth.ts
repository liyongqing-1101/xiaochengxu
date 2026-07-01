import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/modules/auth'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
  // Session模式：不存储token，只存储用户信息用于显示
  const nickname = ref(localStorage.getItem('admin_nickname') || '')
  const role = ref(localStorage.getItem('admin_role') || '')

  // Session模式下，只要有用户信息就算登录状态
  // 每次访问API，拦截器会检查Session是否有效
  const isLoggedIn = computed(() => !!nickname.value)

  async function login(username: string, password: string) {
    const result = await authApi.login(username, password)
    nickname.value = result.nickname
    role.value = result.role
    // 只存储用户显示信息，SessionID由Cookie自动管理
    localStorage.setItem('admin_nickname', result.nickname)
    localStorage.setItem('admin_role', result.role)
  }

  async function logout() {
    try {
      await authApi.logout()
    } catch (e) {
      // 即使登出API失败，也要清除本地状态
    }
    nickname.value = ''
    role.value = ''
    localStorage.removeItem('admin_nickname')
    localStorage.removeItem('admin_role')
    router.push('/login')
  }

  return { nickname, role, isLoggedIn, login, logout }
})
