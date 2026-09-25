<template>
  <div v-if="loading" class="flex items-center justify-center min-h-screen">
    <div class="text-gray-500">加载中...</div>
  </div>

  <div v-else-if="error" class="flex items-center justify-center min-h-screen">
    <div class="text-center">
      <p class="text-red-500 text-lg">{{ error }}</p>
    </div>
  </div>

  <div v-else class="max-w-6xl mx-auto px-4 py-8">
    <div class="mb-6 flex items-start justify-between flex-wrap gap-4">
      <div>
        <p class="text-xs text-gray-400 mb-1">链接 {{ code }} · {{ linkPermText }}</p>
        <h1 class="text-2xl font-bold text-gray-900">{{ view?.albumTitle || '分享相册' }}</h1>
        <p v-if="view?.albumDescription" class="text-gray-500 mt-1">{{ view.albumDescription }}</p>
        <p class="text-sm text-gray-400 mt-1">
          {{ photos.length }} 张照片
          <span> · {{ view?.albumCommentCount ?? 0 }} 条评论</span>
          <span v-if="view?.allowDownload"> · 可下载</span>
          <span v-if="view?.canContribute"> · 可协作上传</span>
        </p>

        <!-- 点赞与标签对所有人可见，但只有登录用户能操作 -->
        <div v-if="view?.canView && view.albumId" class="flex items-center gap-3 flex-wrap mt-3">
          <LikeButton
            target-type="ALBUM"
            :target-id="view.albumId"
            :liked="likedAlbumIds.has(view.albumId)"
            :count="view.albumLikeCount ?? 0"
            :authed="userStore.isLoggedIn"
            @change="handleAlbumLikeChange"
            @need-login="goLogin"
          />
          <TagChips
            target-type="ALBUM"
            :target-id="view.albumId"
            :tags="view.albumTags ?? []"
            :authed="userStore.isLoggedIn"
            @change="handleAlbumTagsChange"
          />
        </div>
      </div>

      <!-- 操作区：编辑与上传入口按后端返回的权限位渲染 -->
      <div class="flex gap-2 flex-wrap">
        <button
          v-if="view?.canEdit"
          @click="openEdit"
          class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition"
        >
          编辑相册
        </button>
        <button
          v-if="view?.canContribute"
          @click="triggerFileInput"
          class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition"
        >
          上传照片
        </button>
        <input
          v-if="view?.canContribute"
          ref="fileInput"
          type="file"
          multiple
          accept="image/*"
          class="hidden"
          @change="handleFileSelect"
        />
      </div>
    </div>

    <!-- 身份区：游客直接协作，登录成员可解锁编辑 -->
    <div class="mb-6 flex items-center justify-between flex-wrap gap-3 px-4 py-3 bg-gray-50 rounded-lg">
      <span class="text-sm text-gray-600">
        <template v-if="userStore.isLoggedIn">
          {{ displayName }} · {{ permissionLabel }}
        </template>
        <template v-else>
          {{ view?.canContribute ? '游客身份 · 可浏览与投稿，照片需管理员审核' : '游客身份 · 仅可浏览' }}
        </template>
      </span>
      <button
        v-if="!userStore.isLoggedIn"
        @click="goLogin"
        class="px-3 py-1.5 text-sm bg-white border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-100 transition"
      >
        {{ view?.canContribute ? '登录后编辑相册' : '登录' }}
      </button>
    </div>

    <!-- 上传后的提示：照片需管理员审核 -->
    <div
      v-if="actionNotice"
      class="mb-6 p-4 bg-amber-50 border border-amber-200 rounded-lg text-sm text-amber-800"
    >
      {{ actionNotice }}
    </div>

    <div v-if="photos.length === 0" class="text-center py-20 text-gray-400">
      <p>暂无照片</p>
      <p v-if="view?.canContribute" class="mt-2">点击右上角"上传照片"为这个相册贡献照片</p>
    </div>

    <div v-else class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
      <div v-for="(photo, index) in photos" :key="photo.id" class="relative group">
        <div
          class="aspect-square bg-gray-100 rounded-lg overflow-hidden cursor-pointer hover:shadow-lg transition"
          @click="openLightbox(index)"
        >
          <img :src="photo.fileUrl" :alt="photo.fileName" class="w-full h-full object-cover" />

          <!-- 相册所有者走分享链接时，可看到自己待审核、被拒绝的照片 -->
          <div
            v-if="photo.status && photo.status !== 'approved'"
            :class="[
              'absolute top-2 left-2 px-2 py-0.5 rounded-full text-xs font-medium text-white shadow',
              photo.status === 'pending' ? 'bg-amber-500/90' : 'bg-red-500/90'
            ]"
          >
            {{ photo.status === 'pending' ? '待审核' : '已拒绝' }}
          </div>

          <!-- 编辑权限下的删除按钮 -->
          <div
            v-if="view?.canEdit"
            class="absolute inset-0 bg-black/0 group-hover:bg-black/40 transition flex items-center justify-center opacity-0 group-hover:opacity-100 pointer-events-none"
          >
            <button
              @click.stop="handleDeletePhoto(photo.id)"
              class="p-2 bg-red-500 text-white rounded-full hover:bg-red-600 pointer-events-auto"
            >
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </div>

        <div class="absolute top-2 right-2 z-10">
          <LikeButton
            target-type="PHOTO"
            :target-id="photo.id"
            :liked="likedPhotoIds.has(photo.id)"
            :count="photo.likeCount ?? 0"
            variant="overlay"
            :authed="userStore.isLoggedIn"
            @change="(liked) => handlePhotoLikeChange(photo, liked)"
            @need-login="goLogin"
          />
        </div>

        <TagChips
          class="mt-1"
          target-type="PHOTO"
          :target-id="photo.id"
          :tags="photo.tags"
          size="sm"
          :authed="userStore.isLoggedIn"
          @change="(tags) => (photo.tags = tags)"
        />
      </div>
    </div>

    <div v-if="view?.allowDownload && photos.length > 0" class="fixed bottom-6 right-6">
      <a
        :href="downloadUrl"
        class="flex items-center gap-2 px-6 py-3 bg-blue-600 text-white rounded-full shadow-lg hover:bg-blue-700 transition"
      >
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
        </svg>
        下载全部
      </a>
    </div>

    <!-- 上传照片弹窗 -->
    <div v-if="showUpload" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-xl p-6 w-full max-w-md">
        <h2 class="text-lg font-bold mb-4">上传照片 ({{ uploadFiles.length }} 张)</h2>

        <div v-if="uploading">
          <div class="w-full bg-gray-200 rounded-full h-3 mb-2">
            <div class="bg-blue-600 h-3 rounded-full transition-all" :style="{ width: `${uploadProgress}%` }" />
          </div>
          <p class="text-sm text-gray-500 text-center mb-4">{{ uploadProgress }}%</p>
        </div>
        <ul v-else class="max-h-48 overflow-y-auto mb-4 space-y-1">
          <li v-for="(file, i) in uploadFiles" :key="i" class="text-sm text-gray-600 truncate">
            {{ file.name }} ({{ formatFileSize(file.size) }})
          </li>
        </ul>

        <div v-if="!uploading" class="mb-3">
          <label class="block text-sm text-gray-600 mb-1">你的昵称（可选）</label>
          <input
            v-model="uploaderNickname"
            type="text"
            maxlength="50"
            placeholder="留空则不记录上传者"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <p class="text-xs text-gray-400 mb-4">
          上传后需要管理员审核，通过后才会显示在这个相册中
        </p>

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

    <!-- 编辑相册弹窗：仅登录成员可见 -->
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
        <p class="text-xs text-gray-400 mb-4">修改后立即对全部访客生效，照片删除可在相册回收站恢复</p>
        <div class="flex justify-end gap-3">
          <button
            @click="showEdit = false"
            class="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-lg transition"
          >
            取消
          </button>
          <button
            @click="handleSaveAlbum"
            :disabled="savingAlbum"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition disabled:opacity-60"
          >
            {{ savingAlbum ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 身份确认：相册密码通过后，未登录访客必须先选一次身份 -->
    <div v-if="showIdentityGate" class="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-xl p-6 w-full max-w-sm">
        <h2 class="text-lg font-bold mb-2">确认你的身份</h2>
        <p class="text-sm text-gray-500 mb-5">
          <template v-if="view?.canContribute">
            相册密码已通过。这条链接开放了协作：游客可投稿照片（需管理员审核），登录后还能修改相册名称、删除照片。
          </template>
          <template v-else>
            相册密码已通过。这条链接只开放浏览，任何人都不能修改相册，登录后也一样。
          </template>
        </p>
        <button
          @click="goLogin"
          class="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition mb-2"
        >
          登录 / 注册
        </button>
        <button
          @click="continueAsGuest"
          class="w-full px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-lg transition text-sm"
        >
          以游客身份浏览
        </button>
      </div>
    </div>

    <!-- 评论区：游客可读，登录后可发表 -->
    <CommentSection
      v-if="view?.canView"
      id="comments"
      class="mt-8"
      :album-id="view.albumId"
      :share-code="code"
      :share-password="password"
      :authed="userStore.isLoggedIn"
      :is-owner="view.myPermission === 'OWNER'"
      @change="handleCommentCount"
      @need-login="goLogin"
    />

    <Lightbox
      :is-open="lightboxIndex >= 0"
      :images="photos.map(p => p.fileUrl)"
      :initial-index="lightboxIndex"
      v-model:index="lightboxIndex"
      @close="lightboxIndex = -1"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getShare, getSharePhotos, uploadSharePhotos, updateShareAlbum, deleteSharePhoto } from '../services/share'
import { getMyLikes } from '../services/like'
import { useUserStore } from '../stores/user'
import { formatFileSize } from '../utils/format'
import type { Photo, ShareView, TagView } from '../types'
import Lightbox from '../components/Lightbox.vue'
import LikeButton from '../components/LikeButton.vue'
import TagChips from '../components/TagChips.vue'
import CommentSection from '../components/CommentSection.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const code = route.params.code as string
const passwordKey = `share_pwd_${code}`
// 身份确认只做一次：记住本会话里"以游客身份继续"的选择
const identityKey = `share_idn_${code}`

const guestConfirmed = ref(!!sessionStorage.getItem(identityKey))

const view = ref<ShareView | null>(null)
const photos = ref<Photo[]>([])
const loading = ref(true)
const error = ref('')
const lightboxIndex = ref(-1)

// 访问密码：在密码页验证成功后暂存，避免同一会话内重复输入
const password = ref<string | undefined>(undefined)

const showUpload = ref(false)
const uploadFiles = ref<File[]>([])
const uploaderNickname = ref('')
const uploading = ref(false)
const uploadProgress = ref(0)
const actionNotice = ref('')
const fileInput = ref<HTMLInputElement | null>(null)

const showEdit = ref(false)
const editTitle = ref('')
const editDescription = ref('')
const savingAlbum = ref(false)

const PERMISSION_LABEL: Record<ShareView['myPermission'], string> = {
  GUEST: '游客',
  VIEWER: '已登录 · 只读',
  MEMBER: '登录成员 · 可投稿与编辑',
  OWNER: '相册所有者',
}

const linkPermText = computed(() => {
  const v = view.value
  if (!v) return ''
  const parts = [v.canContribute ? '开放协作（投稿 + 登录可编辑）' : '仅浏览，不可修改']
  if (v.allowDownload) parts.push('可下载')
  return parts.join(' · ')
})

const permissionLabel = computed(() => PERMISSION_LABEL[view.value?.myPermission ?? 'GUEST'])
const displayName = computed(() => userStore.user?.nickname || userStore.user?.username || '已登录')
const showIdentityGate = computed(
  () => !!view.value?.canView && !userStore.isLoggedIn && !guestConfirmed.value,
)

// 有密码的分享链接下载时也要带上密码
const downloadUrl = computed(() => {
  const base = `/api/shares/${code}/download`
  return password.value ? `${base}?password=${encodeURIComponent(password.value)}` : base
})

// 当前登录用户点过赞的对象；游客不请求该接口，避免触发全局 401 跳转
const likedAlbumIds = ref<Set<string>>(new Set())
const likedPhotoIds = ref<Set<string>>(new Set())

async function loadLikes() {
  if (!userStore.isLoggedIn) return
  try {
    const summary = await getMyLikes()
    likedAlbumIds.value = new Set(summary.likedAlbumIds ?? [])
    likedPhotoIds.value = new Set(summary.likedPhotoIds ?? [])
  } catch {
    // ignore
  }
}

function handleAlbumLikeChange(liked: boolean) {
  const albumId = view.value?.albumId
  if (!albumId) return
  const ids = new Set(likedAlbumIds.value)
  if (liked) ids.add(albumId)
  else ids.delete(albumId)
  likedAlbumIds.value = ids
}

function handlePhotoLikeChange(photo: Photo, liked: boolean) {
  const ids = new Set(likedPhotoIds.value)
  if (liked) ids.add(photo.id)
  else ids.delete(photo.id)
  likedPhotoIds.value = ids
}

function handleAlbumTagsChange(tags: TagView[]) {
  if (view.value) view.value.albumTags = tags
}

function handleCommentCount(count: number) {
  if (view.value) view.value.albumCommentCount = count
}

onMounted(async () => {
  await loadData()
  await loadLikes()
})

async function loadData() {
  try {
    const stored = sessionStorage.getItem(passwordKey) || undefined
    const viewData = await getShare(code, stored)
    view.value = viewData
    password.value = stored

    // 需要密码且本会话尚未验证通过，先去密码页（登录后同样要求密码）
    if (viewData.requirePassword && !viewData.canView) {
      sessionStorage.removeItem(passwordKey)
      router.push(`/share/${code}/password`)
      return
    }

    // 密码已通过但身份未确认：先拦在确认弹窗，不加载相册内容
    if (showIdentityGate.value) return

    await loadPhotos()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

async function loadPhotos() {
  try {
    photos.value = await getSharePhotos(code, password.value)
  } catch (e: unknown) {
    // 暂存的密码已失效，清除后重新走密码页
    if (view.value?.requirePassword) {
      sessionStorage.removeItem(passwordKey)
      router.push(`/share/${code}/password`)
      return
    }
    throw e
  }
}

function goLogin() {
  router.push({ path: '/login', query: { redirect: route.fullPath } })
}

async function continueAsGuest() {
  sessionStorage.setItem(identityKey, 'guest')
  guestConfirmed.value = true
  try {
    await loadPhotos()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '加载失败'
  }
}

function openLightbox(index: number) {
  lightboxIndex.value = index
}

function triggerFileInput() {
  fileInput.value?.click()
}

function handleFileSelect(e: Event) {
  const input = e.target as HTMLInputElement
  if (!input.files || input.files.length === 0) return
  uploadFiles.value = Array.from(input.files)
  actionNotice.value = ''
  // 登录成员默认用自己的昵称
  if (!uploaderNickname.value) uploaderNickname.value = userStore.user?.nickname || ''
  showUpload.value = true
  // 重置，保证再次选择同一批文件时也能触发 change
  input.value = ''
}

function cancelUpload() {
  showUpload.value = false
  uploadFiles.value = []
}

async function handleUpload() {
  uploading.value = true
  uploadProgress.value = 0
  try {
    const uploaded = await uploadSharePhotos(code, uploadFiles.value, {
      password: password.value,
      uploaderNickname: uploaderNickname.value.trim() || undefined,
      onProgress: (percent) => {
        uploadProgress.value = percent
      },
    })
    showUpload.value = false
    uploadFiles.value = []
    actionNotice.value = `已提交 ${uploaded.length} 张照片，管理员审核通过后才会显示在相册中。`
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '上传失败')
  } finally {
    uploading.value = false
  }
}

function openEdit() {
  editTitle.value = view.value?.albumTitle || ''
  editDescription.value = view.value?.albumDescription || ''
  actionNotice.value = ''
  showEdit.value = true
}

async function handleSaveAlbum() {
  if (!editTitle.value.trim()) {
    alert('相册标题不能为空')
    return
  }
  savingAlbum.value = true
  try {
    await updateShareAlbum(code, {
      title: editTitle.value.trim(),
      description: editDescription.value.trim() || undefined,
      password: password.value,
    })
    showEdit.value = false
    actionNotice.value = '相册信息已更新，对所有访客立即生效。'
    const stored = sessionStorage.getItem(passwordKey) || undefined
    view.value = await getShare(code, stored)
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '保存失败')
  } finally {
    savingAlbum.value = false
  }
}

async function handleDeletePhoto(photoId: string) {
  if (!confirm('确定删除这张照片吗？删除后可在相册回收站恢复')) return
  try {
    await deleteSharePhoto(code, photoId, password.value)
    actionNotice.value = '照片已删除。'
    await loadPhotos()
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '删除失败')
  }
}
</script>
