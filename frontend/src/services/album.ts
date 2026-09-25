import api from './api'
import type { Album, AlbumDTO } from '../types'

export async function createAlbum(data: AlbumDTO): Promise<Album> {
  const res = await api.post('/albums', data)
  return res.data.data
}

export async function listAlbums(): Promise<Album[]> {
  const res = await api.get('/albums')
  return res.data.data
}

export async function getAlbum(id: string): Promise<Album> {
  const res = await api.get(`/albums/${id}`)
  return res.data.data
}

export async function updateAlbum(id: string, data: AlbumDTO): Promise<Album> {
  const res = await api.put(`/albums/${id}`, data)
  return res.data.data
}

export async function deleteAlbum(id: string): Promise<void> {
  await api.delete(`/albums/${id}`)
}

export async function listDeletedAlbums(): Promise<Album[]> {
  const res = await api.get('/albums/trash/list')
  return res.data.data
}

export async function restoreAlbum(id: string): Promise<void> {
  await api.post(`/albums/trash/${id}/restore`)
}

export async function permanentlyDeleteAlbum(id: string): Promise<void> {
  await api.delete(`/albums/trash/${id}`)
}
