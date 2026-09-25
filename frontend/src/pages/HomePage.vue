<template>
  <div v-if="loading" class="flex items-center justify-center min-h-screen">
    <div class="text-gray-500">加载中...</div>
  </div>

  <div v-else class="max-w-6xl mx-auto px-4 py-8">
    <div class="flex items-center justify-between mb-4">
      <h1 class="text-2xl font-bold text-gray-900">相册</h1>
      <button
        @click="showCreate = true"
        class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
      >
        新建相册
      </button>
    </div>

    <!-- 首页标签页：自己创建的相册 与 自己点赞过的相册/照片 -->
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
          {{ tab.value === 'mine' ? albums.length : likedAlbums.length + likedPhotos.length }}
        </span>
      </button>
    </div>

    <!-- 我的相册 -->
    <template v-if="activeTab === 'mine'">
      <div v-if="albums.length === 0" class="text-center py-20 text-gray-400">
        <p class="text-lg">还没有相册</p>
        <p class="mt-2">点击上方按钮创建第一个相册</p>
      </div>

      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <AlbumCard
          v-for="album in albums"
          :key="album.id"
          :album="album"
          :liked="likedAlbumIds.has(album.id)"
          deletable
          show-visibility
          @open="navigate(`/album/${album.id}`)"
          @remove="handleDelete(album.id)"
          @like-change="(liked) => handleAlbumLikeChange(album, liked)"
          @tags-change="(tags) => (album.tags = tags)"
        />
      </div>
    </template>

    <!-- 我点赞的 -->
    <template v-else>
      <div v-if="likedAlbums.length === 0 && likedPhotos.length === 0" class="text-center py-20 text-gray-400">
        <p class="text-lg">还没有点赞过的内容</p>
        <p class="mt-2">在相册或照片上点心，就会出现在这里</p>
      </div>

      <div v-else class="space-y-10">
        <section v-if="likedAlbums.length > 0">
          <h2 class="text-base font-semibold text-gray-800 mb-3">点赞过的相册</h2>
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            <AlbumCard
              v-for="album in likedAlbums"
              :key="album.id"
              :album="album"
              liked
              @open="navigate(`/album/${album.id}`)"
              @like-change="(liked) => handleAlbumLikeChange(album, liked)"
              @tags-change="(tags) => (album.tags = tags)"
            />
          </div>
        </section>

        <section v-if="likedPhotos.length > 0">
          <h2 class="text-base font-semibold text-gray-800 mb-3">点赞过的照片</h2>
          <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
            <div
              v-for="photo in likedPhotos"
              :key="photo.id"
              @click="navigate(`/album/${photo.albumId}`)"
              class="group cursor-pointer"
            >
              <div class="relative aspect-square bg-gray-100 rounded-lg overflow-hidden">
                <img :src="photo.fileUrl" :alt="photo.fileName" class="w-full h-full object-cover" />
                <div class="absolute top-2 right-2">
                  <LikeButton
                    target-type="PHOTO"
                    :target-id="photo.id"
                    liked
                    :count="photo.likeCount ?? 0"
                    variant="overlay"
                    @change="(liked) => handlePhotoLikeChange(photo, liked)"
                  />
                </div>
              </div>
              <p class="text-xs text-gray-500 mt-1 truncate">{{ photo.albumTitle || photo.fileName }}</p>
              <TagChips
                target-type="PHOTO"
                :target-id="photo.id"
                :tags="photo.tags"
                size="sm"
                @change="(tags) => (photo.tags = tags)"
              />
            </div>
          </div>
        </section>
      </div>
    </template>

    <div v-if="showCreate" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-xl p-6 w-full max-w-md">
        <h2 class="text-lg font-bold mb-4">新建相册</h2>
        <input
          v-model="title"
          type="text"
          placeholder="相册标题"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg mb-3 focus:outline-none focus:ring-2 focus:ring-blue-500"
          autofocus
        />
        <textarea
          v-model="description"
          placeholder="相册描述（可选）"
          rows="3"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg mb-4 focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
        />
        <div class="flex justify-end gap-3">
          <button
            @click="cancelCreate"
            class="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-lg transition"
          >
            取消
          </button>
          <button
            @click="handleCreate"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
          >
            创建
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listAlbums, createAlbum, deleteAlbum } from '../services/album'
import { getMyLikes, listLikedAlbums, listLikedPhotos } from '../services/like'
import AlbumCard from '../components/AlbumCard.vue'
import LikeButton from '../components/LikeButton.vue'
import TagChips from '../components/TagChips.vue'
import type { Album, Photo } from '../types'

const TABS = [
  { value: 'mine', label: '我的相册' },
  { value: 'liked', label: '我点赞的' },
] as const

type TabValue = typeof TABS[number]['value']

const router = useRouter()
const albums = ref<Album[]>([])
const likedAlbums = ref<Album[]>([])
const likedPhotos = ref<Photo[]>([])
const likedAlbumIds = ref<Set<string>>(new Set())
const likedPhotoIds = ref<Set<string>>(new Set())
const activeTab = ref<TabValue>('mine')
const showCreate = ref(false)
const title = ref('')
const description = ref('')
const loading = ref(true)

onMounted(async () => {
  await loadData()
})

async function loadData() {
  try {
    const [myAlbums, myLikedAlbums, myLikedPhotos, summary] = await Promise.all([
      listAlbums(),
      listLikedAlbums(),
      listLikedPhotos(),
      getMyLikes(),
    ])
    albums.value = myAlbums
    likedAlbums.value = myLikedAlbums
    likedPhotos.value = myLikedPhotos
    likedAlbumIds.value = new Set(summary.likedAlbumIds ?? [])
    likedPhotoIds.value = new Set(summary.likedPhotoIds ?? [])
  } catch {
    // 未建表时后端会报错，保留空列表即可
  } finally {
    loading.value = false
  }
}

// 取消点赞后把该行从「我点赞的」列表里摘掉，避免标签页数据与点心状态不一致
function handleAlbumLikeChange(album: Album, liked: boolean) {
  const ids = new Set(likedAlbumIds.value)
  if (liked) ids.add(album.id)
  else {
    ids.delete(album.id)
    likedAlbums.value = likedAlbums.value.filter((item) => item.id !== album.id)
  }
  likedAlbumIds.value = ids
}

function handlePhotoLikeChange(photo: Photo, liked: boolean) {
  const ids = new Set(likedPhotoIds.value)
  if (liked) ids.add(photo.id)
  else {
    ids.delete(photo.id)
    likedPhotos.value = likedPhotos.value.filter((item) => item.id !== photo.id)
  }
  likedPhotoIds.value = ids
}

async function handleCreate() {
  if (!title.value.trim()) return
  try {
    await createAlbum({ title: title.value.trim(), description: description.value.trim() || undefined })
    title.value = ''
    description.value = ''
    showCreate.value = false
    await loadData()
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '创建失败')
  }
}

function cancelCreate() {
  showCreate.value = false
  title.value = ''
  description.value = ''
}

async function handleDelete(id: string) {
  if (!confirm('确定删除此相册？相册内的照片和分享链接也会被删除。')) return
  try {
    await deleteAlbum(id)
    await loadData()
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '删除失败')
  }
}

function navigate(path: string) {
  router.push(path)
}
</script>
