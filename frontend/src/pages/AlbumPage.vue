<template>
  <div v-if="loading" class="flex items-center justify-center min-h-screen">
    <div class="text-gray-500">加载中...</div>
  </div>

  <div v-else-if="!album" class="flex items-center justify-center min-h-screen">
    <div class="text-gray-500">相册不存在</div>
  </div>

  <div v-else class="max-w-6xl mx-auto px-4 py-8">
    <div class="mb-8">
      <!-- 左上角按钮 -->
      <button
        @click="navigate('/')"
        class="text-blue-600 hover:text-blue-700 mb-4 inline-flex items-center gap-1"
      >
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
        返回相册列表
      </button>

      <div class="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 inline-flex items-center gap-2 flex-wrap">
            {{ album.title }}
            <span
              :class="[
                'px-1.5 py-0.5 rounded text-xs font-normal',
                album.visibility === 'PUBLIC'
                  ? 'bg-emerald-50 text-emerald-600'
                  : 'bg-gray-100 text-gray-500',
              ]"
            >
              {{ album.visibility === 'PUBLIC' ? '公开' : '私密' }}
            </span>
          </h1>
          <p v-if="album.description" class="text-gray-500 mt-1">{{ album.description }}</p>
          <p class="text-sm text-gray-400 mt-1">
            {{ approvedCount }} 张照片 · {{ album.commentCount ?? 0 }} 条评论 · 创建于 {{ formatRelativeTime(album.createdAt) }}
          </p>
          <div class="flex items-center gap-3 flex-wrap mt-3">
            <LikeButton
              target-type="ALBUM"
              :target-id="album.id"
              :liked="likedAlbumIds.has(album.id)"
              :count="album.likeCount ?? 0"
              @change="handleAlbumLikeChange"
            />
            <TagChips
              target-type="ALBUM"
              :target-id="album.id"
              :tags="album.tags"
              @change="handleAlbumTagsChange"
            />
          </div>
        </div>

        <!-- 右上角按钮 -->
        <div class="flex gap-2 flex-wrap">
          <button
            @click="openEdit"
            class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition"
          >
            编辑
          </button>
          <button
            @click="openShare"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
          >
            分享
          </button>
          <button
            v-if="!userStore.isAdmin"
            @click="triggerFileInput"
            class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition"
          >
            上传照片
          </button>
          <input
            v-if="!userStore.isAdmin"
            ref="fileInput"
            type="file"
            multiple
            accept="image/*"
            class="hidden"
            @change="handleFileSelect"
          />
        </div>
      </div>
    </div>

    <div v-if="photos.length === 0" class="text-center py-20 text-gray-400">
      <p class="text-lg">还没有照片</p>
      <p class="mt-2">点击"上传照片"开始添加</p>
    </div>

    <div v-else>
      <!-- 审核状态标签页：待审核/已拒绝的照片与已通过的照片分开放置 -->
      <div class="flex gap-1 mb-6 border-b border-gray-200">
        <button
          v-for="tab in TABS"
          :key="tab.value"
          @click="activeTab = tab.value"
          :class="[
            'px-4 py-2 text-sm font-medium border-b-2 -mb-px transition inline-flex items-center gap-1.5',
            activeTab === tab.value
              ? 'border-blue-600 text-blue-600'
              : 'border-transparent text-gray-500 hover:text-gray-700'
          ]"
        >
          {{ tab.label }}
          <span
            :class="[
              'px-1.5 py-0.5 rounded-full text-xs',
              activeTab === tab.value ? 'bg-blue-100 text-blue-700' : 'bg-gray-100 text-gray-500'
            ]"
          >
            {{ tabCounts[tab.value] }}
          </span>
        </button>
      </div>

      <div v-if="filteredPhotos.length === 0" class="text-center py-16 text-gray-400">
        <p class="text-lg">{{ EMPTY_TEXT[activeTab] }}</p>
        <p v-if="activeTab === 'approved'" class="mt-2">点击"上传照片"开始添加</p>
      </div>

      <!-- 按上传日期分组：同一天的照片放在同一个日期标题下方 -->
      <div v-else class="space-y-8">
        <section v-for="group in groupedPhotos" :key="group.date">
        <div class="flex items-center gap-3 mb-3">
          <h2 class="text-base font-semibold text-gray-800">{{ formatDateLabel(group.date) }}</h2>
          <span class="text-xs text-gray-400">{{ group.items.length }} 张</span>
          <div class="flex-1 h-px bg-gray-200" />
        </div>

        <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
          <div v-for="item in group.items" :key="item.photo.id" class="relative">
            <!-- 图片上方的上传时间 -->
            <p class="text-xs text-gray-500 mb-1">{{ formatTime(item.photo.createdAt) }}</p>

            <div class="relative group aspect-square bg-gray-100 rounded-lg overflow-hidden">
              <img
                :src="item.photo.fileUrl"
                :alt="item.photo.fileName"
                class="w-full h-full object-cover cursor-pointer"
                @click="openLightbox(item.index)"
              />

              <!-- 照片点赞：hover 之外的常态可见入口 -->
              <div class="absolute top-2 right-2">
                <LikeButton
                  target-type="PHOTO"
                  :target-id="item.photo.id"
                  :liked="likedPhotoIds.has(item.photo.id)"
                  :count="item.photo.likeCount ?? 0"
                  variant="overlay"
                  @change="(liked) => handlePhotoLikeChange(item.photo, liked)"
                />
              </div>

              <!-- 审核状态角标（已通过的不显示） -->
              <div
                v-if="item.photo.status && item.photo.status !== 'approved'"
                :class="[
                  'absolute top-2 left-2 px-2 py-0.5 rounded-full text-xs font-medium text-white shadow',
                  item.photo.status === 'pending' ? 'bg-amber-500/90' : 'bg-red-500/90'
                ]"
              >
                {{ item.photo.status === 'pending' ? '待审核' : '已拒绝' }}
              </div>

              <!-- 删除按钮 -->
              <div class="absolute inset-0 bg-black/0 group-hover:bg-black/40 transition flex items-center justify-center opacity-0 group-hover:opacity-100 pointer-events-none">
                <button
                  @click="handleDeletePhoto(item.photo.id)"
                  class="p-2 bg-red-500 text-white rounded-full hover:bg-red-600 pointer-events-auto"
                >
                  <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              </div>

              <!-- 图片信息层（已拒绝的照片常驻显示，便于看到拒绝原因） -->
              <div
                :class="[
                  'absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/60 to-transparent p-2 transition',
                  item.photo.status === 'rejected' ? 'opacity-100' : 'opacity-0 group-hover:opacity-100'
                ]"
              >
                <p class="text-white text-xs truncate">{{ item.photo.fileName }}</p>
                <p class="text-white/70 text-xs">{{ formatFileSize(item.photo.fileSize) }}</p>
                <p
                  v-if="item.photo.status === 'rejected' && item.photo.rejectReason"
                  class="text-red-300 text-xs mt-0.5 line-clamp-2"
                  :title="item.photo.rejectReason"
                >
                  拒绝原因：{{ item.photo.rejectReason }}
                </p>
              </div>
            </div>

            <TagChips
              class="mt-1"
              target-type="PHOTO"
              :target-id="item.photo.id"
              :tags="item.photo.tags"
              size="sm"
              @change="(tags) => (item.photo.tags = tags)"
            />
          </div>
        </div>
        </section>
      </div>
    </div>

    <!-- 评论区：登录用户可发表，作者与相册所有者可删除 -->
    <CommentSection
      id="comments"
      class="mt-8"
      :album-id="id"
      :is-owner="album.userId === userStore.user?.id"
      @change="handleCommentCount"
    />

    <!-- map 就是做这个 纯字符串数组['https://cdn/a.jpg', 'https://cdn/b.jpg'] “抽取”动作 -->
    <Lightbox
      :is-open="lightboxIndex >= 0"
      :images="filteredPhotos.map(p => p.fileUrl)"
      :initial-index="lightboxIndex"
      v-model:index="lightboxIndex"
      @close="lightboxIndex = -1"
    />

    <!-- 上传照片弹窗 -->
    <div v-if="showUpload" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-xl p-6 w-full max-w-md">
        <h2 class="text-lg font-bold mb-4">上传照片 ({{ uploadFiles.length }} 张)</h2>
        <div v-if="uploading">
          <div class="w-full bg-gray-200 rounded-full h-3 mb-2">
            <div class="bg-blue-600 h-3 rounded-full transition-all" :style="{ width: `${uploadProgress}%` }" />
          </div>
          <p class="text-sm text-gray-500 text-center">{{ uploadProgress }}%</p>
        </div>
        <ul v-else class="max-h-60 overflow-y-auto mb-4 space-y-1">
          <li v-for="(file, i) in uploadFiles" :key="i" class="text-sm text-gray-600 truncate">
            {{ file.name }} ({{ formatFileSize(file.size) }})
          </li>
        </ul>
        <div class="flex justify-end gap-3">
          <button
            v-if="!uploading"
            @click="cancelUpload"
            class="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-lg transition"
          >
            取消
          </button>
          <button
            v-if="!uploading"
            @click="handleUpload"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
          >
            开始上传
          </button>
        </div>
      </div>
    </div>

    <!-- 分享照片弹窗 -->
    <div v-if="showShare" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-xl p-6 w-full max-w-md">
        <h2 class="text-lg font-bold mb-4">创建分享链接</h2>
        <div v-if="shareLink">
          <p class="text-sm text-gray-500 mb-2">分享链接已创建：</p>
          <div class="flex gap-2">
            <input
              type="text"
              :value="shareLink"
              readonly
              class="flex-1 px-3 py-2 border border-gray-300 rounded-lg bg-gray-50 text-sm"
            />
            <button
              @click="copyLink"
              class="px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm"
            >
              复制
            </button>
          </div>
          <p class="text-xs mt-2" :class="shareAllowUpload ? 'text-amber-600' : 'text-gray-500'">
            这条链接的权限：{{ createdPermText }}
          </p>
          <p class="text-xs text-gray-400 mt-1">
            之前生成的链接仍然有效，权限改动只对这条新链接生效
          </p>
          <button
            @click="showShare = false"
            class="mt-4 w-full px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition"
          >
            关闭
          </button>
        </div>
        <div v-else>
          <div class="mb-3">
            <label class="block text-sm text-gray-600 mb-1">访问密码（可选）</label>
            <input
              v-model="sharePassword"
              type="text"
              placeholder="留空则无需密码"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div class="mb-3">
            <label class="block text-sm text-gray-600 mb-1">有效期</label>
            <div class="flex gap-2">
              <button
                v-for="opt in EXPIRE_OPTIONS"
                :key="opt.value"
                @click="shareExpireDays = opt.value"
                :class="[
                  'flex-1 py-2 rounded-lg text-sm transition',
                  shareExpireDays === opt.value
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                ]"
              >
                {{ opt.label }}
              </button>
            </div>
          </div>
          <label class="flex items-center gap-2 mb-4 cursor-pointer">
            <input
              v-model="shareAllowDownload"
              type="checkbox"
              class="w-4 h-4 text-blue-600 rounded"
            />
            <span class="text-sm text-gray-600">允许下载原图</span>
          </label>
          <label class="flex items-center gap-2 mb-4 cursor-pointer">
            <input
              v-model="shareAllowUpload"
              type="checkbox"
              class="w-4 h-4 text-blue-600 rounded"
            />
            <span class="text-sm text-gray-600">允许协作（投稿 + 编辑）</span>
          </label>
          <p class="text-xs text-gray-400 mb-4">
            <template v-if="shareAllowUpload">
              勾选后这条链接同时开放两件事：任何人都能投稿照片（需管理员审核）；已登录用户还能修改相册名称、描述并删除相册里的照片。
            </template>
            <template v-else>
              未勾选：这条链接对所有人都只能浏览，登录后也不能改名或删除照片。
            </template>
          </p>
          <div class="flex justify-end gap-3">
            <button
              @click="showShare = false"
              class="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-lg transition"
            >
              取消
            </button>
            <button
              @click="handleCreateShare"
              class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
            >
              创建链接
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑相册弹窗 -->
    <div v-if="showEdit" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-xl p-6 w-full max-w-md">
        <h2 class="text-lg font-bold mb-4">编辑相册</h2>
        <input
          v-model="editTitle"
          type="text"
          placeholder="相册标题"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg mb-3 focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <textarea
          v-model="editDescription"
          placeholder="相册描述（可选）"
          rows="3"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg mb-4 focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
        />
        <div class="mb-4">
          <label class="block text-sm text-gray-600 mb-1">可见范围</label>
          <div class="flex gap-2">
            <button
              v-for="opt in VISIBILITY_OPTIONS"
              :key="opt.value"
              type="button"
              @click="editVisibility = opt.value"
              :class="[
                'flex-1 py-2 rounded-lg text-sm transition',
                editVisibility === opt.value
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200',
              ]"
            >
              {{ opt.label }}
            </button>
          </div>
          <p class="text-xs text-gray-400 mt-2">
            {{ editVisibility === 'PUBLIC'
              ? '公开后，关注你的人能在动态和搜索里看到这个相册；分享链接不受影响。'
              : '私密相册只有你自己能看到，不会出现在别人的动态和搜索结果里。' }}
          </p>
        </div>
        <div class="flex justify-end gap-3">
          <button
            @click="showEdit = false"
            class="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-lg transition"
          >
            取消
          </button>
          <button
            @click="handleUpdateAlbum"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
          >
            保存
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAlbum, updateAlbum } from '../services/album'
import { listPhotos, uploadPhotos, deletePhoto } from '../services/photo'
import { createShareLink } from '../services/share'
import { getMyLikes } from '../services/like'
import type { Album, Photo, TagView, Visibility } from '../types'
import { formatRelativeTime, formatFileSize, formatTime, formatDateKey, formatDateLabel } from '../utils/format'
import Lightbox from '../components/Lightbox.vue'
import LikeButton from '../components/LikeButton.vue'
import TagChips from '../components/TagChips.vue'
import CommentSection from '../components/CommentSection.vue'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const EXPIRE_OPTIONS = [
  { label: '1天', value: 1 },
  { label: '7天', value: 7 },
  { label: '30天', value: 30 },
  { label: '永久', value: 0 },
]

const VISIBILITY_OPTIONS: { label: string; value: Visibility }[] = [
  { label: '私密', value: 'PRIVATE' },
  { label: '公开', value: 'PUBLIC' },
]

const route = useRoute()
const router = useRouter()
const id = route.params.id as string

const album = ref<Album | null>(null)
const photos = ref<Photo[]>([])
const loading = ref(true)

// 审核状态标签页：待审核/已拒绝的照片与已通过的照片分开放置
const TABS = [
  { value: 'approved', label: '已通过' },
  { value: 'pending', label: '待审核' },
  { value: 'rejected', label: '已拒绝' },
] as const

type TabValue = typeof TABS[number]['value']

const EMPTY_TEXT: Record<TabValue, string> = {
  approved: '还没有通过审核的照片',
  pending: '没有待审核的照片',
  rejected: '没有被拒绝的照片',
}

const activeTab = ref<TabValue>('approved')

const approvedCount = computed(() => photos.value.filter(p => p.status === 'approved').length)
const pendingCount = computed(() => photos.value.filter(p => p.status === 'pending').length)
const rejectedCount = computed(() => photos.value.filter(p => p.status === 'rejected').length)

const tabCounts = computed<Record<TabValue, number>>(() => ({
  approved: approvedCount.value,
  pending: pendingCount.value,
  rejected: rejectedCount.value,
}))

// 当前标签页对应的照片，供栅格与 Lightbox 共用
const filteredPhotos = computed(() => photos.value.filter(p => p.status === activeTab.value))

// 按上传日期分组：同一天的照片归到同一个日期标题下
// index 保留照片在 filteredPhotos 中的下标，供 Lightbox 使用
interface PhotoGroup {
  date: string
  items: { photo: Photo; index: number }[]
}

const groupedPhotos = computed<PhotoGroup[]>(() => {
  const groups = new Map<string, PhotoGroup>()
  filteredPhotos.value.forEach((photo, index) => {
    const date = formatDateKey(photo.createdAt)
    let group = groups.get(date)
    if (!group) {
      group = { date, items: [] }
      groups.set(date, group)
    }
    group.items.push({ photo, index })
  })
  return Array.from(groups.values())
})

const showUpload = ref(false)
const uploadFiles = ref<File[]>([])
const uploadProgress = ref(0)
const uploading = ref(false)

const showShare = ref(false)
const sharePassword = ref('')
const shareExpireDays = ref(7)
const shareAllowDownload = ref(true)
const shareAllowUpload = ref(false)
const shareLink = ref('')

const createdPermText = computed(() => {
  const parts = [shareAllowUpload.value ? '可投稿，登录用户可改名和删照片' : '只能浏览，任何人都不能改动']
  if (shareAllowDownload.value) parts.push('可下载原图')
  return parts.join(' · ')
})

const showEdit = ref(false)
const editTitle = ref('')
const editDescription = ref('')
const editVisibility = ref<Visibility>('PRIVATE')

const lightboxIndex = ref(-1)
const fileInput = ref<HTMLInputElement | null>(null)

// 记录当前用户点过赞的对象，决定心形是否高亮
const likedAlbumIds = ref<Set<string>>(new Set())
const likedPhotoIds = ref<Set<string>>(new Set())

onMounted(async () => {
  await loadData()
})

async function loadData() {
  try {
    const [albumRes, photosRes, likesRes] = await Promise.all([
      getAlbum(id),
      listPhotos(id),
      getMyLikes(),
    ])
    album.value = albumRes
    photos.value = photosRes
    likedAlbumIds.value = new Set(likesRes.likedAlbumIds ?? [])
    likedPhotoIds.value = new Set(likesRes.likedPhotoIds ?? [])
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

function handleAlbumLikeChange(liked: boolean) {
  const ids = new Set(likedAlbumIds.value)
  if (liked) ids.add(id)
  else ids.delete(id)
  likedAlbumIds.value = ids
}

function handlePhotoLikeChange(photo: Photo, liked: boolean) {
  const ids = new Set(likedPhotoIds.value)
  if (liked) ids.add(photo.id)
  else ids.delete(photo.id)
  likedPhotoIds.value = ids
}

function handleAlbumTagsChange(tags: TagView[]) {
  if (album.value) album.value.tags = tags
}

// 评论区自己统计了条数，回写到头部统计行，避免为了一次计数整页刷新
function handleCommentCount(count: number) {
  if (album.value) album.value.commentCount = count
}

function triggerFileInput() {
  // ? 是 可选链操作符（optional chaining），意思是：「如果 fileInput.value 有值就调用 click()，如果是 null / undefined 就什么都不做、也不报错」。
  fileInput.value?.click()
}

function handleFileSelect(e: Event) {
  const target = e.target as HTMLInputElement
  const files = Array.from(target.files || [])
  uploadFiles.value = files
  showUpload.value = true
  target.value = ''
}

async function handleUpload() {
  if (uploadFiles.value.length === 0) return
  uploading.value = true
  uploadProgress.value = 0
  try {
    await uploadPhotos(id, uploadFiles.value, (percent) => {
      uploadProgress.value = percent
    })
    uploadFiles.value = []
    showUpload.value = false
    await loadData()
    // 普通用户上传后照片处于待审核状态，自动切到「待审核」标签页
    if (!userStore.isAdmin) activeTab.value = 'pending'
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '上传失败')
  } finally {
    uploading.value = false
  }
}

function cancelUpload() {
  showUpload.value = false
  uploadFiles.value = []
}

async function handleDeletePhoto(photoId: string) {
  if (!confirm('确定删除此照片？')) return
  try {
    await deletePhoto(photoId)
    await loadData()
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '删除失败')
  }
}

function openShare() {
  // 每次重新打开都回到默认值，避免沿用上次勾选的权限
  sharePassword.value = ''
  shareExpireDays.value = 7
  shareAllowDownload.value = true
  shareAllowUpload.value = false
  shareLink.value = ''
  showShare.value = true
}

async function handleCreateShare() {
  try {
    const expireAt = shareExpireDays.value === 0
      ? undefined
      : new Date(Date.now() + shareExpireDays.value * 86400000).toISOString()

    const result = await createShareLink({
      albumId: id,
      password: sharePassword.value || undefined,
      expireAt,
      allowDownload: shareAllowDownload.value,
      allowUpload: shareAllowUpload.value,
    })
    const code = result.shareCode
    shareLink.value = `${window.location.origin}/share/${code}`
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '创建分享链接失败')
  }
}

function copyLink() {
  navigator.clipboard.writeText(shareLink.value)
  alert('已复制')
}

function openEdit() {
  editTitle.value = album.value?.title || ''
  editDescription.value = album.value?.description || ''
  editVisibility.value = album.value?.visibility === 'PUBLIC' ? 'PUBLIC' : 'PRIVATE'
  showEdit.value = true
}

async function handleUpdateAlbum() {
  if (!editTitle.value.trim()) return
  try {
    await updateAlbum(id, {
      title: editTitle.value.trim(),
      description: editDescription.value.trim() || undefined,
      visibility: editVisibility.value,
    })
    showEdit.value = false
    await loadData()
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '更新失败')
  }
}

function openLightbox(index: number) {
  lightboxIndex.value = index
}

function navigate(path: string) {
  router.push(path)
}
</script>
