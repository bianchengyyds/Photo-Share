import api from './api'
import type { Photo } from '../types'

export async function uploadPhotos(albumId: string, files: File[], onProgress?: (percent: number) => void): Promise<Photo[]> {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))

  const res = await api.post(`/albums/${albumId}/photos`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: (e) => {
      if (onProgress && e.total) {
        onProgress(Math.round((e.loaded * 100) / e.total))
      }
    },
  })
  return res.data.data
}

export async function listPhotos(albumId: string): Promise<Photo[]> {
  const res = await api.get(`/albums/${albumId}/photos`)
  return res.data.data
}

export async function deletePhoto(id: string): Promise<void> {
  await api.delete(`/photos/${id}`)
}

export async function listDeletedPhotos(): Promise<Photo[]> {
  const res = await api.get('/trash/photos')
  return res.data.data
}

export async function restorePhoto(id: string): Promise<void> {
  await api.post(`/trash/photos/${id}/restore`)
}

export async function permanentlyDeletePhoto(id: string): Promise<void> {
  await api.delete(`/trash/photos/${id}`)
}

export async function listDeletedPhotosByAlbumId(albumId: string): Promise<Photo[]> {
  const res = await api.get(`/albums/${albumId}/photos/trash`)
  return res.data.data
}

// 管理员审核相关 API
export async function listPendingPhotos(): Promise<Photo[]> {
  const res = await api.get('/admin/photos/pending')
  return res.data.data
}

export async function listAllPhotosForAdmin(status?: string): Promise<Photo[]> {
  const params = status ? { status } : {}
  const res = await api.get('/admin/photos', { params })
  return res.data.data
}

export async function approvePhoto(id: string): Promise<void> {
  await api.post(`/admin/photos/${id}/approve`)
}

export async function rejectPhoto(id: string, reason?: string): Promise<void> {
  await api.post(`/admin/photos/${id}/reject`, { reason })
}

export async function batchApprovePhotos(photoIds: string[]): Promise<void> {
  await api.post('/admin/photos/batch-approve', { photoIds })
}

export async function batchRejectPhotos(photoIds: string[], reason?: string): Promise<void> {
  await api.post('/admin/photos/batch-reject', { photoIds, reason })
}
