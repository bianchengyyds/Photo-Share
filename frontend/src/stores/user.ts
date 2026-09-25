import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type { User } from '../types'
import { authService } from '../services/auth'

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(sessionStorage.getItem('token'))

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => user.value?.username || '')
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  async function login(username: string, password: string) {
    const response = await authService.login({ username, password })
    token.value = response.token
    user.value = response.user
    sessionStorage.setItem('token', response.token)
    sessionStorage.setItem('user', JSON.stringify(response.user))
  }

  async function register(username: string, password: string, nickname?: string) {
    const user = await authService.register({ username, password, nickname })
    return user
  }

  async function fetchUser() {
    if (!token.value) return
    try {
      user.value = await authService.getCurrentUser()
      sessionStorage.setItem('user', JSON.stringify(user.value))
    } catch {
      logout()
    }
  }

  function logout() {
    user.value = null
    token.value = null
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('user')
  }

  return {
    user,
    token,
    isLoggedIn,
    username,
    isAdmin,
    login,
    register,
    fetchUser,
    logout
  }
})
