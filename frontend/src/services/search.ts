import api from './api'
import type { Album, Photo, SearchQuery } from '../types'

// 后端把留空的筛选字段视为"不加该条件"，前端只提交非空项
function toParams(query: SearchQuery): Record<string, string | number> {
  const params: Record<string, string | number> = {}
  const fields = ['keyword', 'tagId', 'albumId', 'ownerId', 'startDate', 'endDate'] as const
  for (const key of fields) {
    const value = query[key]
    if (value) params[key] = value
  }
  params.page = query.page ?? 1
  params.size = query.size ?? 20
  return params
}

export async function searchAlbums(query: SearchQuery): Promise<Album[]> {
  const res = await api.get('/search/albums', { params: toParams(query) })
  return res.data.data
}

export async function searchPhotos(query: SearchQuery): Promise<Photo[]> {
  const res = await api.get('/search/photos', { params: toParams(query) })
  return res.data.data
}

export async function listFollowedAlbums(page = 1, size = 20): Promise<Album[]> {
  const res = await api.get('/search/following-albums', { params: { page, size } })
  return res.data.data
}
