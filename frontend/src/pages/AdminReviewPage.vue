<template>
  <div class="min-h-screen bg-gray-50">
    <div class="container mx-auto px-4 py-8">
      <h1 class="text-3xl font-bold text-gray-900 mb-8">照片审核管理</h1>

      <!-- 状态筛选标签 -->
      <div class="flex gap-2 mb-6">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          @click="currentTab = tab.value"
          :class="[
            'px-4 py-2 rounded-lg font-medium transition',
            currentTab === tab.value
              ? 'bg-blue-600 text-white'
              : 'bg-white text-gray-700 hover:bg-gray-100'
          ]"
        >
          {{ tab.label }}
          <span v-if="tab.count !== undefined" class="ml-2 text-sm opacity-80">
            ({{ tab.count }})
          </span>
        </button>
      </div>

      <!-- 批量操作栏 -->
      <div v-if="selectedPhotos.length > 0" class="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
        <div class="flex items-center justify-between">
          <span class="text-blue-900">
            已选择 <strong>{{ selectedPhotos.length }}</strong> 张照片
          </span>
          <div class="flex gap-3">
            <button
              @click="handleBatchApprove"
              class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition"
            >
              批量通过
            </button>
            <button
              @click="showBatchRejectModal = true"
              class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
            >
              批量拒绝
            </button>
            <button
              @click="selectedPhotos = []"
              class="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition"
            >
              取消选择
            </button>
          </div>
        </div>
      </div>

      <!-- 照片网格 -->
      <div v-if="loading" class="text-center py-12">
        <p class="text-gray-500">加载中...</p>
      </div>

      <div v-else-if="photos.length === 0" class="text-center py-12">
        <p class="text-gray-500">暂无{{ currentTab === 'pending' ? '待审核' : currentTab === 'approved' ? '已通过' : currentTab === 'rejected' ? '已拒绝' : '' }}照片</p>
      </div>

      <div v-else class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
        <div
          v-for="photo in photos"
          :key="photo.id"
          class="bg-white rounded-lg shadow-sm overflow-hidden hover:shadow-md transition"
        >
          <!-- 照片预览 -->
          <div class="relative aspect-square bg-gray-100">
            <img
              :src="photo.fileUrl"
              :alt="photo.fileName"
              class="w-full h-full object-cover cursor-pointer"
              @click="openPreview(photo)"
            />
            <!-- 选择框 -->
            <div
              v-if="photo.status === 'pending'"
              class="absolute top-2 left-2"
            >
              <input
                type="checkbox"
                :checked="selectedPhotos.includes(photo.id)"
                @change="toggleSelection(photo.id)"
                class="w-5 h-5 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
              />
            </div>
            <!-- 状态标签 -->
            <div
              :class="[
                'absolute top-2 right-2 px-2 py-1 rounded text-xs font-medium',
                photo.status === 'pending' ? 'bg-yellow-500 text-white' :
                photo.status === 'approved' ? 'bg-green-500 text-white' :
                'bg-red-500 text-white'
              ]"
            >
              {{ photo.status === 'pending' ? '待审核' : photo.status === 'approved' ? '已通过' : '已拒绝' }}
            </div>
          </div>

          <!-- 照片信息 -->
          <div class="p-4">
            <p class="text-sm font-medium text-gray-900 truncate mb-1">
              {{ photo.fileName }}
            </p>
            <p class="text-xs text-gray-500 mb-2">
              上传者: {{ photo.uploaderName || '未知' }}
            </p>
            <p class="text-xs text-gray-500 mb-3">
              {{ formatTime(photo.createdAt) }}
            </p>

            <!-- 拒绝原因 -->
            <div v-if="photo.status === 'rejected' && photo.rejectReason" class="mb-3 p-2 bg-red-50 rounded">
              <p class="text-xs text-red-700">
                <strong>拒绝原因:</strong> {{ photo.rejectReason }}
              </p>
            </div>

            <!-- 操作按钮 -->
            <div v-if="photo.status === 'pending'" class="flex gap-2">
              <button
                @click="handleApprove(photo.id)"
                class="flex-1 px-3 py-2 bg-green-600 text-white text-sm rounded hover:bg-green-700 transition"
              >
                通过
              </button>
              <button
                @click="openRejectModal(photo.id)"
                class="flex-1 px-3 py-2 bg-red-600 text-white text-sm rounded hover:bg-red-700 transition"
              >
                拒绝
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 拒绝原因弹窗 -->
    <div v-if="showRejectModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 max-w-md w-full mx-4">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">拒绝原因</h3>
        <textarea
          v-model="rejectReason"
          placeholder="请输入拒绝原因（可选）"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          rows="4"
        ></textarea>
        <div class="flex gap-3 mt-4">
          <button
            @click="closeRejectModal"
            class="flex-1 px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition"
          >
            取消
          </button>
          <button
            @click="confirmReject"
            class="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
          >
            确认拒绝
          </button>
        </div>
      </div>
    </div>

    <!-- 批量拒绝原因弹窗 -->
    <div v-if="showBatchRejectModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 max-w-md w-full mx-4">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">批量拒绝原因</h3>
        <textarea
          v-model="batchRejectReason"
          placeholder="请输入拒绝原因（可选）"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          rows="4"
        ></textarea>
        <div class="flex gap-3 mt-4">
          <button
            @click="showBatchRejectModal = false"
            class="flex-1 px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition"
          >
            取消
          </button>
          <button
            @click="confirmBatchReject"
            class="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
          >
            确认拒绝
          </button>
        </div>
      </div>
    </div>

    <!-- 照片预览弹窗 -->
    <div v-if="previewPhoto" class="fixed inset-0 bg-black bg-opacity-90 flex items-center justify-center z-50" @click="closePreview">
      <div class="relative max-w-4xl max-h-full p-4" @click.stop>
        <img
          :src="previewPhoto.fileUrl"
          :alt="previewPhoto.fileName"
          class="max-w-full max-h-[90vh] object-contain"
        />
        <button
          @click="closePreview"
          class="absolute top-4 right-4 text-white text-3xl hover:text-gray-300"
        >
          ×
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { listAllPhotosForAdmin, approvePhoto, rejectPhoto, batchApprovePhotos, batchRejectPhotos } from '../services/photo'
import type { Photo } from '../types'
import { format } from 'date-fns'

const loading = ref(true)
const photos = ref<Photo[]>([])
const currentTab = ref<string>('pending')
const selectedPhotos = ref<string[]>([])

// 拒绝相关
const showRejectModal = ref(false)
const rejectPhotoId = ref<string | null>(null)
const rejectReason = ref('')

// 批量拒绝相关
const showBatchRejectModal = ref(false)
const batchRejectReason = ref('')

// 预览相关
const previewPhoto = ref<Photo | null>(null)

// 统计
const pendingCount = ref(0)
const approvedCount = ref(0)
const rejectedCount = ref(0)

const tabs = computed(() => [
  { label: '待审核', value: 'pending', count: pendingCount.value },
  { label: '已通过', value: 'approved', count: approvedCount.value },
  { label: '已拒绝', value: 'rejected', count: rejectedCount.value },
  { label: '全部', value: 'all' }
])

onMounted(async () => {
  await loadPhotos()
  await loadCounts()
})

watch(currentTab, async () => {
  await loadPhotos()
})

async function loadPhotos() {
  loading.value = true
  try {
    const status = currentTab.value === 'all' ? undefined : currentTab.value
    photos.value = await listAllPhotosForAdmin(status)
  } catch (error) {
    console.error('加载照片失败:', error)
  } finally {
    loading.value = false
  }
}

async function loadCounts() {
  try {
    const [pending, approved, rejected] = await Promise.all([
      listAllPhotosForAdmin('pending'),
      listAllPhotosForAdmin('approved'),
      listAllPhotosForAdmin('rejected')
    ])
    pendingCount.value = (pending || []).length
    approvedCount.value = (approved || []).length
    rejectedCount.value = (rejected || []).length
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

function toggleSelection(photoId: string) {
  const index = selectedPhotos.value.indexOf(photoId)
  if (index === -1) {
    selectedPhotos.value.push(photoId)
  } else {
    selectedPhotos.value.splice(index, 1)
  }
}

async function handleApprove(photoId: string) {
  if (!confirm('确认通过这张照片？')) return
  
  try {
    await approvePhoto(photoId)
    await loadPhotos()
    await loadCounts()
  } catch (error) {
    console.error('审核失败:', error)
    alert('审核失败，请重试')
  }
}

function openRejectModal(photoId: string) {
  rejectPhotoId.value = photoId
  rejectReason.value = ''
  showRejectModal.value = true
}

function closeRejectModal() {
  showRejectModal.value = false
  rejectPhotoId.value = null
  rejectReason.value = ''
}

async function confirmReject() {
  if (!rejectPhotoId.value) return
  
  try {
    await rejectPhoto(rejectPhotoId.value, rejectReason.value)
    closeRejectModal()
    await loadPhotos()
    await loadCounts()
  } catch (error) {
    console.error('拒绝失败:', error)
    alert('拒绝失败，请重试')
  }
}

async function handleBatchApprove() {
  if (selectedPhotos.value.length === 0) return
  if (!confirm(`确认通过选中的 ${selectedPhotos.value.length} 张照片？`)) return
  
  try {
    await batchApprovePhotos(selectedPhotos.value)
    selectedPhotos.value = []
    await loadPhotos()
    await loadCounts()
  } catch (error) {
    console.error('批量审核失败:', error)
    alert('批量审核失败，请重试')
  }
}

async function confirmBatchReject() {
  if (selectedPhotos.value.length === 0) return
  
  try {
    await batchRejectPhotos(selectedPhotos.value, batchRejectReason.value)
    showBatchRejectModal.value = false
    batchRejectReason.value = ''
    selectedPhotos.value = []
    await loadPhotos()
    await loadCounts()
  } catch (error) {
    console.error('批量拒绝失败:', error)
    alert('批量拒绝失败，请重试')
  }
}

function openPreview(photo: Photo) {
  previewPhoto.value = photo
}

function closePreview() {
  previewPhoto.value = null
}

function formatTime(dateString: string) {
  try {
    return format(new Date(dateString), 'yyyy-MM-dd HH:mm')
  } catch {
    return dateString
  }
}
</script>
