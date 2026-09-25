import api from './api'
import type { NotificationItem } from '../types'

export async function listNotifications(unreadOnly = false, page = 1, size = 20): Promise<NotificationItem[]> {
  const res = await api.get('/notifications', { params: { unreadOnly, page, size } })
  return res.data.data
}

export async function getUnreadCount(): Promise<number> {
  const res = await api.get('/notifications/unread-count')
  return res.data.data
}

export async function markRead(id: string): Promise<void> {
  await api.post(`/notifications/${id}/read`)
}

export async function markAllRead(): Promise<void> {
  await api.post('/notifications/read-all')
}

export async function deleteNotification(id: string): Promise<void> {
  await api.delete(`/notifications/${id}`)
}
