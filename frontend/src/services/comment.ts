import api from './api'
import type { AlbumComment } from '../types'

export async function listComments(albumId: string): Promise<AlbumComment[]> {
  const res = await api.get(`/albums/${albumId}/comments`)
  return res.data.data
}

// parentId 为空表示一级评论；mentionUserIds 来自 @ 选人面板，服务端逐个校验
export async function addComment(
  albumId: string,
  content: string,
  parentId?: string | null,
  mentionUserIds?: string[],
): Promise<AlbumComment> {
  const res = await api.post(`/albums/${albumId}/comments`, {
    content,
    parentId: parentId || undefined,
    mentionUserIds: mentionUserIds?.length ? mentionUserIds : undefined,
  })
  return res.data.data
}

// 只有评论作者和相册所有者能删，服务端已做校验，前端只需按身份显示入口
export async function deleteComment(id: string): Promise<void> {
  await api.delete(`/comments/${id}`)
}
