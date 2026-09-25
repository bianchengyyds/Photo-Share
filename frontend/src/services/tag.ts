import api from './api'
import type { TagCount, TagView, TargetType } from '../types'

export async function addTag(targetType: TargetType, targetId: string, name: string): Promise<TagView> {
  const res = await api.post('/tags', { targetType, targetId, name })
  return res.data.data
}

export async function listPopularTags(limit = 30): Promise<TagCount[]> {
  const res = await api.get('/tags/popular', { params: { limit } })
  return res.data.data
}

export async function deleteTag(relationId: string): Promise<void> {
  await api.delete(`/tags/${relationId}`)
}
