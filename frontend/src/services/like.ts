import api from './api'
import type { Album, LikeSummary, Photo, TargetType } from '../types'

// 点赞只提交"目标类型 + 目标ID"这一最小信息，点赞人由后端从 token 取
export async function likeTarget(targetType: TargetType, targetId: string): Promise<void> {
  await api.post('/likes', { targetType, targetId })
}

export async function unlikeTarget(targetType: TargetType, targetId: string): Promise<void> {
  await api.delete('/likes', { params: { targetType, targetId } })
}

export async function getMyLikes(): Promise<LikeSummary> {
  const res = await api.get('/likes/mine')
  return res.data.data
}

export async function listLikedAlbums(): Promise<Album[]> {
  const res = await api.get('/likes/my-albums')
  return res.data.data
}

export async function listLikedPhotos(): Promise<Photo[]> {
  const res = await api.get('/likes/my-photos')
  return res.data.data
}
