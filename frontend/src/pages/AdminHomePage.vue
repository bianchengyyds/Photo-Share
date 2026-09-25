<template>
  <div class="min-h-screen bg-gray-50">
    <div class="container mx-auto px-4 py-8">
      <h1 class="text-3xl font-bold text-gray-900 mb-8">管理员控制台</h1>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div class="bg-white rounded-lg shadow-sm p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-500 mb-1">待审核照片</p>
              <p class="text-3xl font-bold text-yellow-600">{{ stats.pending }}</p>
            </div>
            <div class="w-12 h-12 bg-yellow-100 rounded-full flex items-center justify-center">
              <svg class="w-6 h-6 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow-sm p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-500 mb-1">已通过照片</p>
              <p class="text-3xl font-bold text-green-600">{{ stats.approved }}</p>
            </div>
            <div class="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow-sm p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-500 mb-1">已拒绝照片</p>
              <p class="text-3xl font-bold text-red-600">{{ stats.rejected }}</p>
            </div>
            <div class="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center">
              <svg class="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="bg-white rounded-lg shadow-sm p-6 mb-8">
        <h2 class="text-xl font-semibold text-gray-900 mb-4">快捷操作</h2>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <router-link
            to="/admin/review"
            class="flex items-center p-4 border border-gray-200 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition"
          >
            <div class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center mr-4">
              <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div>
              <h3 class="font-medium text-gray-900">照片审核</h3>
              <p class="text-sm text-gray-500">审核用户上传的照片</p>
            </div>
          </router-link>

          <div class="flex items-center p-4 border border-gray-200 rounded-lg opacity-50 cursor-not-allowed">
            <div class="w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center mr-4">
              <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
            </div>
            <div>
              <h3 class="font-medium text-gray-900">上传照片</h3>
              <p class="text-sm text-gray-500">管理员无需上传照片</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 最近待审核照片预览 -->
      <div class="bg-white rounded-lg shadow-sm p-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-xl font-semibold text-gray-900">最近待审核照片</h2>
          <router-link to="/admin/review" class="text-blue-600 hover:text-blue-700 text-sm font-medium">
            查看全部 →
          </router-link>
        </div>

        <div v-if="loading" class="text-center py-12">
          <p class="text-gray-500">加载中...</p>
        </div>

        <div v-else-if="recentPhotos.length === 0" class="text-center py-12">
          <p class="text-gray-500">暂无待审核照片</p>
        </div>

        <div v-else class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
          <div
            v-for="photo in recentPhotos"
            :key="photo.id"
            class="relative aspect-square bg-gray-100 rounded-lg overflow-hidden group"
          >
            <img
              :src="photo.fileUrl"
              :alt="photo.fileName"
              class="w-full h-full object-cover"
            />
            <div class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-40 transition flex items-center justify-center">
              <span class="text-white opacity-0 group-hover:opacity-100 transition text-sm font-medium">
                {{ photo.uploaderName || '未知用户' }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listAllPhotosForAdmin } from '../services/photo'
import type { Photo } from '../types'

const loading = ref(true)
const stats = ref({
  pending: 0,
  approved: 0,
  rejected: 0
})
const recentPhotos = ref<Photo[]>([])

onMounted(async () => {
  await loadStats()
  await loadRecentPhotos()
})

async function loadStats() {
  try {
    const [pending, approved, rejected] = await Promise.all([
      listAllPhotosForAdmin('pending'),
      listAllPhotosForAdmin('approved'),
      listAllPhotosForAdmin('rejected')
    ])
    stats.value.pending = pending.length
    stats.value.approved = approved.length
    stats.value.rejected = rejected.length
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

async function loadRecentPhotos() {
  loading.value = true
  try {
    const photos = await listAllPhotosForAdmin('pending')
    recentPhotos.value = photos.slice(0, 12)
  } catch (error) {
    console.error('加载照片失败:', error)
  } finally {
    loading.value = false
  }
}
</script>
