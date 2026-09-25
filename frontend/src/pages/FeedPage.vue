<template>
  <div v-if="loading" class="flex items-center justify-center min-h-screen">
    <div class="text-gray-500">加载中...</div>
  </div>

  <div v-else class="max-w-6xl mx-auto px-4 py-8">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900">动态</h1>
      <p class="text-sm text-gray-500 mt-1">我关注的用户公开出来的相册</p>
    </div>

    <div v-if="albums.length === 0" class="text-center py-20 text-gray-400">
      <p class="text-lg">这里还没有相册</p>
      <p class="mt-2">
        去
        <router-link to="/search" class="text-blue-600 hover:underline">搜索</router-link>
        里关注一些用户，他们设为公开的相册会出现在这里
      </p>
    </div>

    <template v-else>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="album in albums" :key="album.id">
          <router-link
            v-if="album.userId"
            :to="`/user/${album.userId}`"
            class="flex items-center gap-1.5 mb-1.5 text-xs text-gray-500 hover:text-blue-600 transition min-w-0"
          >
            <span class="w-5 h-5 rounded-full bg-gray-200 text-gray-600 flex items-center justify-center text-[11px] flex-shrink-0">
              {{ (album.uploaderName || 'U').slice(0, 1) }}
            </span>
            <span class="truncate">{{ album.uploaderName || '用户' }}</span>
          </router-link>

          <AlbumCard
            :album="album"
            :liked="likedAlbumIds.has(album.id)"
            @open="router.push(`/album/${album.id}`)"
            @tags-change="(tags) => (album.tags = tags)"
            @like-change="(liked) => handleLikeChange(album.id, liked)"
          />
        </div>
      </div>

      <div class="text-center mt-8">
        <button
          v-if="hasMore"
          @click="loadMore"
          :disabled="loadingMore"
          class="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition disabled:opacity-50"
        >
          {{ loadingMore ? '加载中...' : '加载更多' }}
        </button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listFollowedAlbums } from '../services/search'
import { getMyLikes } from '../services/like'
import type { Album } from '../types'
import AlbumCard from '../components/AlbumCard.vue'

const SIZE = 12

const router = useRouter()
const albums = ref<Album[]>([])
const likedAlbumIds = ref<Set<string>>(new Set())
const loading = ref(true)
const loadingMore = ref(false)
const hasMore = ref(false)
const page = ref(1)

onMounted(async () => {
  await Promise.all([loadAlbums(), loadLikes()])
})

async function loadAlbums() {
  loading.value = page.value === 1
  try {
    const list = await listFollowedAlbums(page.value, SIZE)
    albums.value = page.value === 1 ? list : [...albums.value, ...list]
    hasMore.value = list.length === SIZE
  } catch {
    // 出错时保留已有列表，避免翻页失败把整页清空
  } finally {
    loading.value = false
  }
}

async function loadLikes() {
  try {
    likedAlbumIds.value = new Set((await getMyLikes()).likedAlbumIds ?? [])
  } catch {
    // ignore
  }
}

async function loadMore() {
  if (loadingMore.value) return
  loadingMore.value = true
  page.value += 1
  await loadAlbums()
  loadingMore.value = false
}

function handleLikeChange(albumId: string, liked: boolean) {
  const ids = new Set(likedAlbumIds.value)
  if (liked) ids.add(albumId)
  else ids.delete(albumId)
  likedAlbumIds.value = ids
}
</script>
