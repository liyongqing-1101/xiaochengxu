import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/modules/auth'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const nickname = ref(localStorage.getItem('admin_nickname') || '')
  const role = ref(localStorage.getItem('admin_role') || '')

  const isLoggedIn = computed(() => !!token.value)

  async function login(username: string, password: string) {
    const result = await authApi.login(username, password)
    token.value = result.token
    nickname.value = result.nickname
    role.value = result.role
    localStorage.setItem('admin_token', result.token)
    localStorage.setItem('admin_nickname', result.nickname)
    localStorage.setItem('admin_role', result.role)
  }

  function logout() {
    token.value = ''
    nickname.value = ''
    role.value = ''
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_nickname')
    localStorage.removeItem('admin_role')
    router.push('/login')
  }

  return { token, nickname, role, isLoggedIn, login, logout }
})
