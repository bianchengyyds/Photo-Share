import api from './api'
import type { LoginRequest, RegisterRequest, LoginResponse, User } from '../types'

export const authService = {
  async login(data: LoginRequest): Promise<LoginResponse> {
    const res = await api.post('/auth/login', data)
    return res.data.data
  },

  async register(data: RegisterRequest): Promise<User> {
    const res = await api.post('/auth/register', data)
    return res.data.data
  },

  async getCurrentUser(): Promise<User> {
    const res = await api.get('/auth/me')
    return res.data.data
  },

  async logout(): Promise<void> {
    // 前端清除 token 即可，后端 JWT 是无状态的
  }
}
