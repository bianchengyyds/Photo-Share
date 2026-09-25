import api from './api'
import type { UserBrief, UserProfile } from '../types'

export async function getUserProfile(id: string): Promise<UserProfile> {
  const res = await api.get(`/users/${id}`)
  return res.data.data
}

export async function listFollowers(id: string, page = 1, size = 20): Promise<UserBrief[]> {
  const res = await api.get(`/users/${id}/followers`, { params: { page, size } })
  return res.data.data
}

export async function listFollowing(id: string, page = 1, size = 20): Promise<UserBrief[]> {
  const res = await api.get(`/users/${id}/following`, { params: { page, size } })
  return res.data.data
}

export async function followUser(id: string): Promise<void> {
  await api.post(`/users/${id}/follow`)
}

export async function unfollowUser(id: string): Promise<void> {
  await api.delete(`/users/${id}/follow`)
}

// @ 选人面板与关注添加共用，后端限制返回条数
export async function suggestUsers(keyword: string): Promise<UserBrief[]> {
  const res = await api.get('/users/suggest', { params: { keyword } })
  return res.data.data
}
