import api from './api'
import type { Album, AlbumComment, Photo, ShareLink, ShareLinkDTO, ShareView } from '../types'

export async function createShareLink(data: ShareLinkDTO): Promise<ShareLink> {
  const res = await api.post('/shares', data)
  return res.data.data
}

// password 可选：带上已验证过的访问密码时，后端才会返回相册标题与权限位
export async function getShare(code: string, password?: string): Promise<ShareView> {
  const res = await api.get(`/shares/${code}`, { params: password ? { password } : {} })
  return res.data.data
}

// 访问密码随请求体提交，避免出现在 URL 与服务器日志里
export async function getSharePhotos(code: string, password?: string): Promise<Photo[]> {
  const res = await api.post(`/shares/${code}/photos`, { password })
  return res.data.data
}

// 走分享链接的密码校验而非登录鉴权，因此未登录的访客也能读到评论列表
export async function getShareComments(code: string, password?: string): Promise<AlbumComment[]> {
  const res = await api.post(`/shares/${code}/comments`, { password })
  return res.data.data
}

// 发表评论仍要求登录，链接只负责访问授权；密码走请求体，不进 URL
export async function addShareComment(
  code: string,
  data: { content: string; parentId?: string | null; mentionUserIds?: string[]; password?: string },
): Promise<AlbumComment> {
  const res = await api.post(`/shares/${code}/comments/add`, {
    content: data.content,
    parentId: data.parentId || undefined,
    mentionUserIds: data.mentionUserIds?.length ? data.mentionUserIds : undefined,
    password: data.password,
  })
  return res.data.data
}

// 协作者通过分享链接上传照片，上传后需管理员审核通过才会出现在相册中
export async function uploadSharePhotos(
  code: string,
  files: File[],
  options: { password?: string; uploaderNickname?: string; onProgress?: (percent: number) => void } = {},
): Promise<Photo[]> {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))
  if (options.uploaderNickname) formData.append('uploaderNickname', options.uploaderNickname)

  const res = await api.post(`/shares/${code}/photos/upload`, formData, {
    params: options.password ? { password: options.password } : {},
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: (e) => {
      if (options.onProgress && e.total) {
        options.onProgress(Math.round((e.loaded * 100) / e.total))
      }
    },
  })
  return res.data.data
}

// 登录成员修改相册信息（创建者需已在链接上开启协作）
export async function updateShareAlbum(
  code: string,
  data: { title: string; description?: string; password?: string },
): Promise<Album> {
  const res = await api.put(`/shares/${code}/album`, data)
  return res.data.data
}

// 登录成员删除相册内的照片（软删除，可在相册回收站恢复）
export async function deleteSharePhoto(code: string, photoId: string, password?: string): Promise<void> {
  await api.delete(`/shares/${code}/photos/${photoId}`, { params: password ? { password } : {} })
}
