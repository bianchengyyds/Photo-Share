<template>
  <div v-if="loading" class="flex items-center justify-center min-h-screen">
    <div class="text-gray-500">加载中...</div>
  </div>

  <div v-else class="max-w-6xl mx-auto px-4 py-8">
    <div class="mb-8">
      <button
        @click="navigate('/')"
        class="text-blue-600 hover:text-blue-700 mb-4 inline-flex items-center gap-1"
      >
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
        返回相册列表
      </button>

      <div class="flex items-center gap-3">
        <svg class="w-8 h-8 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
        </svg>
        <h1 class="text-2xl font-bold text-gray-900">回收站</h1>
        <span class="text-sm text-gray-500">({{ albums.length }} 个相册, {{ deletedPhotos.length }} 张照片)</span>
      </div>
      <p class="text-sm text-gray-500 mt-2">已删除的相册和照片会保留在回收站中，可以恢复或永久删除</p>
    </div>

    <div v-if="albums.length > 0" class="mb-12">
      <h2 class="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
        <svg class="w-5 h-5 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
        </svg>
        已删除的相册
      </h2>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="album in albums"
          :key="album.id"
          @click="handleAlbumClick(album)"
          :class="[
            'bg-white rounded-xl shadow-sm border overflow-hidden cursor-pointer transition group',
            selectedAlbum?.id === album.id ? 'border-blue-500 ring-2 ring-blue-200' : 'border-gray-100 hover:shadow-md'
          ]"
        >
          <div class="p-4">
            <div class="flex items-center justify-between mb-2">
              <h3 class="font-semibold text-gray-900 truncate">{{ album.title }}</h3>
              <div class="flex gap-1">
                <button
                  @click.stop="handleRestoreAlbum(album.id)"
                  class="p-1.5 text-green-600 hover:bg-green-50 rounded-lg transition"
                  title="恢复"
                >
                  <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                  </svg>
                </button>
                <button
                  @click.stop="handlePermanentlyDeleteAlbum(album.id)"
                  class="p-1.5 text-red-600 hover:bg-red-50 rounded-lg transition"
                  title="永久删除"
                >
                  <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              </div>
            </div>
            <p v-if="album.description" class="text-sm text-gray-500 truncate">{{ album.description }}</p>
            <p class="text-xs text-gray-400 mt-2">删除于 {{ formatRelativeTime(album.updatedAt) }}</p>
          </div>
        </div>
      </div>
    </div>

    <div>
      <h2 class="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
        <svg class="w-5 h-5 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
        </svg>
        已删除的照片
      </h2>

      <div v-if="deletedPhotos.length === 0" class="text-center py-10 text-gray-400">
        <p>没有已删除的照片</p>
      </div>

      <div v-else class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
        <div
          v-for="photo in deletedPhotos"
          :key="photo.id"
          class="relative group aspect-square bg-gray-100 rounded-lg overflow-hidden"
        >
          <img
            :src="photo.fileUrl"
            :alt="photo.fileName"
            class="w-full h-full object-cover opacity-60"
          />
          <div class="absolute inset-0 bg-black/40 flex flex-col items-center justify-center gap-2 opacity-0 group-hover:opacity-100 transition">
            <button
              @click="handleRestorePhoto(photo.id)"
              class="px-3 py-1.5 bg-green-600 text-white text-sm rounded-lg hover:bg-green-700 transition"
            >
              恢复
            </button>
            <button
              @click="handlePermanentlyDeletePhoto(photo.id)"
              class="px-3 py-1.5 bg-red-600 text-white text-sm rounded-lg hover:bg-red-700 transition"
            >
              永久删除
            </button>
          </div>
          <div class="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/80 to-transparent p-2">
            <p class="text-white text-xs truncate">{{ photo.fileName }}</p>
            <p v-if="photo.albumTitle" class="text-white/70 text-xs truncate">来自: {{ photo.albumTitle }}</p>
          </div>
        </div>
      </div>
    </div>

    <div v-if="selectedAlbum" class="mt-12 border-t pt-8">
      <div class="flex items-center gap-3 mb-4">
        <button
          @click="clearSelectedAlbum"
          class="text-blue-600 hover:text-blue-700 inline-flex items-center gap-1 text-sm"
        >
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
          返回
        </button>
        <h2 class="text-xl font-bold text-gray-900">
          "{{ selectedAlbum.title }}" 中的照片
        </h2>
      </div>

      <div v-if="photoLoading" class="text-center py-10 text-gray-500">加载中...</div>

      <div v-else-if="photos.length === 0" class="text-center py-10 text-gray-400">
        <p>该相册中没有已删除的照片</p>
      </div>

      <div v-else class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
        <div
          v-for="photo in photos"
          :key="photo.id"
          class="relative group aspect-square bg-gray-100 rounded-lg overflow-hidden"
        >
          <img
            :src="photo.fileUrl"
            :alt="photo.fileName"
            class="w-full h-full object-cover opacity-60"
          />
          <div class="absolute inset-0 bg-black/40 flex flex-col items-center justify-center gap-2 opacity-0 group-hover:opacity-100 transition">
            <button
              @click="handleRestorePhoto(photo.id)"
              class="px-3 py-1.5 bg-green-600 text-white text-sm rounded-lg hover:bg-green-700 transition"
            >
              恢复
            </button>
            <button
              @click="handlePermanentlyDeletePhoto(photo.id)"
              class="px-3 py-1.5 bg-red-600 text-white text-sm rounded-lg hover:bg-red-700 transition"
            >
              永久删除
            </button>
          </div>
          <div class="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/60 to-transparent p-2">
            <p class="text-white text-xs truncate">{{ photo.fileName }}</p>
            <p class="text-white/70 text-xs">{{ formatFileSize(photo.fileSize) }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listDeletedAlbums, restoreAlbum, permanentlyDeleteAlbum } from '../services/album'
import { listDeletedPhotos, listDeletedPhotosByAlbumId, restorePhoto, permanentlyDeletePhoto } from '../services/photo'
import type { Album, Photo } from '../types'
import { formatRelativeTime, formatFileSize } from '../utils/format'

const router = useRouter()
const albums = ref<Album[]>([])
const deletedPhotos = ref<Photo[]>([])
const selectedAlbum = ref<Album | null>(null)
const photos = ref<Photo[]>([])
const loading = ref(true)
const photoLoading = ref(false)

onMounted(async () => {
  await loadTrashData()
})

async function loadTrashData() {
  try {
    const [deletedAlbums, allDeletedPhotos] = await Promise.all([
      listDeletedAlbums(),
      listDeletedPhotos(),
    ])

    const deletedAlbumIds = new Set(deletedAlbums.map((a: any) => a.id))
    const orphanPhotos = allDeletedPhotos.filter((p: any) => !p.albumId || !deletedAlbumIds.has(p.albumId))

    albums.value = deletedAlbums
    deletedPhotos.value = orphanPhotos
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

async function loadDeletedPhotos(albumId: string) {
  photoLoading.value = true
  try {
    photos.value = await listDeletedPhotosByAlbumId(albumId)
  } catch {
    // ignore
  } finally {
    photoLoading.value = false
  }
}

async function handleRestoreAlbum(id: string) {
  if (!confirm('确定恢复此相册？')) return
  try {
    await restoreAlbum(id)
    await loadTrashData()
    if (selectedAlbum.value?.id === id) {
      clearSelectedAlbum()
    }
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '恢复失败')
  }
}

async function handlePermanentlyDeleteAlbum(id: string) {
  if (!confirm('确定永久删除此相册？此操作不可恢复！')) return
  try {
    await permanentlyDeleteAlbum(id)
    await loadTrashData()
    if (selectedAlbum.value?.id === id) {
      clearSelectedAlbum()
    }
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '删除失败')
  }
}

async function handleRestorePhoto(id: string) {
  if (!confirm('确定恢复此照片？')) return
  try {
    await restorePhoto(id)
    await loadTrashData()
    if (selectedAlbum.value) {
      await loadDeletedPhotos(selectedAlbum.value.id)
    }
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '恢复失败')
  }
}

async function handlePermanentlyDeletePhoto(id: string) {
  if (!confirm('确定永久删除此照片？此操作不可恢复！')) return
  try {
    await permanentlyDeletePhoto(id)
    await loadTrashData()
    if (selectedAlbum.value) {
      await loadDeletedPhotos(selectedAlbum.value.id)
    }
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '删除失败')
  }
}

function handleAlbumClick(album: Album) {
  selectedAlbum.value = album
  loadDeletedPhotos(album.id)
}

function clearSelectedAlbum() {
  selectedAlbum.value = null
  photos.value = []
}

function navigate(path: string) {
  router.push(path)
}
</script>
