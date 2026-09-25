<template>
  <div class="max-w-6xl mx-auto px-4 py-8">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900">搜索</h1>
      <p class="text-sm text-gray-500 mt-1">在自己的相册和已关注用户的公开相册里找内容</p>
    </div>

    <!-- 筛选条件：关键词命中相册标题、描述与标签名 -->
    <div class="bg-white rounded-xl border border-gray-100 shadow-sm p-4 mb-6">
      <div class="flex flex-col sm:flex-row gap-3">
        <input
          v-model.trim="filters.keyword"
          type="text"
          placeholder="搜索标题、描述或标签"
          class="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          @keyup.enter="search"
        />
        <div class="flex flex-wrap gap-2">
          <select
            v-model="filters.albumId"
            class="px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 max-w-40"
          >
            <option value="">全部相册</option>
            <option v-for="album in albumOptions" :key="album.id" :value="album.id">{{ album.title }}</option>
          </select>
          <select
            v-model="filters.tagId"
            class="px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 max-w-32"
          >
            <option value="">全部标签</option>
            <option v-for="tag in tags" :key="tag.id" :value="tag.id">#{{ tag.name }}</option>
          </select>
          <input
            v-model="filters.startDate"
            type="date"
            class="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <input
            v-model="filters.endDate"
            type="date"
            class="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div class="flex gap-2">
          <button
            @click="search"
            class="px-5 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition text-sm"
          >
            搜索
          </button>
          <button
            @click="reset"
            class="px-4 py-2 border border-gray-300 text-gray-600 rounded-lg hover:bg-gray-50 transition text-sm"
          >
            重置
          </button>
        </div>
      </div>
    </div>

    <div class="flex gap-1 mb-6 border-b border-gray-200">
      <button
        v-for="tab in TABS"
        :key="tab.value"
        @click="activeTab = tab.value"
        :class="[
          'px-4 py-2 text-sm font-medium border-b-2 -mb-px transition',
          activeTab === tab.value
            ? 'border-blue-600 text-blue-600'
            : 'border-transparent text-gray-500 hover:text-gray-700',
        ]"
      >
        {{ tab.label }}
      </button>
    </div>

    <div v-if="loading" class="text-center py-16 text-gray-400">搜索中...</div>

    <div v-else-if="isEmpty" class="text-center py-16 text-gray-400">
      <template v-if="hasCriteria">
        <p class="text-lg">没有匹配的结果</p>
        <p class="mt-2">换个关键词，或放宽时间范围</p>
      </template>
      <template v-else>
        <p class="text-lg">还没有搜索</p>
        <p class="mt-2">输入关键词，或选择相册、标签、时间范围后再搜索</p>
      </template>
    </div>

    <template v-else>
      <div v-if="activeTab === 'albums'" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <AlbumCard
          v-for="album in albums"
          :key="album.id"
          :album="album"
          @open="router.push(`/album/${album.id}`)"
          @tags-change="(tags2) => (album.tags = tags2)"
        />
      </div>

      <div v-else-if="activeTab === 'users'" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
        <UserCard
          v-for="person in users"
          :key="person.id"
          :user="person"
          @open="router.push(`/user/${person.id}`)"
          @follow-change="(followed) => (person.followed = followed)"
        />
      </div>

      <div v-else class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
        <div v-for="(photo, index) in photos" :key="photo.id">
          <div class="relative aspect-square bg-gray-100 rounded-lg overflow-hidden group">
            <img
              :src="photo.fileUrl"
              :alt="photo.fileName"
              loading="lazy"
              class="w-full h-full object-cover cursor-pointer"
              @click="lightboxIndex = index"
            />
            <div class="absolute top-2 right-2">
              <LikeButton
                target-type="PHOTO"
                :target-id="photo.id"
                :liked="likedPhotoIds.has(photo.id)"
                :count="photo.likeCount ?? 0"
                variant="overlay"
                @change="(liked) => handleLikeChange(photo.id, liked)"
              />
            </div>
            <div class="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/60 to-transparent p-2">
              <p class="text-white text-xs truncate">{{ photo.albumTitle || photo.fileName }}</p>
              <p class="text-white/70 text-xs truncate">{{ photo.uploaderName }}</p>
            </div>
          </div>
          <TagChips
            target-type="PHOTO"
            :target-id="photo.id"
            :tags="photo.tags"
            size="sm"
            @change="(tags2) => (photo.tags = tags2)"
          />
        </div>
      </div>

      <div v-if="hasMore" class="text-center mt-8">
        <button
          @click="loadMore"
          :disabled="busy"
          class="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition disabled:opacity-50"
        >
          {{ busy ? '加载中...' : '加载更多' }}
        </button>
      </div>
    </template>

    <Lightbox
      :is-open="lightboxIndex >= 0"
      :images="photos.map((p) => p.fileUrl)"
      :initial-index="lightboxIndex"
      v-model:index="lightboxIndex"
      @close="lightboxIndex = -1"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchAlbums, searchPhotos } from '../services/search'
import { suggestUsers } from '../services/user'
import { listPopularTags } from '../services/tag'
import { getMyLikes } from '../services/like'
import type { Album, Photo, SearchQuery, TagCount, UserBrief } from '../types'
import AlbumCard from '../components/AlbumCard.vue'
import UserCard from '../components/UserCard.vue'
import LikeButton from '../components/LikeButton.vue'
import TagChips from '../components/TagChips.vue'
import Lightbox from '../components/Lightbox.vue'

const TABS = [
  { value: 'albums', label: '相册' },
  { value: 'photos', label: '照片' },
  { value: 'users', label: '用户' },
] as const

type TabValue = typeof TABS[number]['value']

const SIZE = 24

const route = useRoute()
const router = useRouter()

// 未填写的筛选项统一留空串，后端把空串视为不加该条件
interface FilterState {
  keyword: string
  tagId: string
  albumId: string
  startDate: string
  endDate: string
}

const filters = ref<FilterState>({
  keyword: '',
  tagId: '',
  albumId: '',
  startDate: '',
  endDate: '',
})

const activeTab = ref<TabValue>('albums')
const albums = ref<Album[]>([])
const photos = ref<Photo[]>([])
const users = ref<UserBrief[]>([])
const albumOptions = ref<Album[]>([])
const tags = ref<TagCount[]>([])
const likedPhotoIds = ref<Set<string>>(new Set())
const loading = ref(true)
const busy = ref(false)
const hasMore = ref(false)
const page = ref(1)
const lightboxIndex = ref(-1)

const isEmpty = computed(() => {
  if (activeTab.value === 'albums') return albums.value.length === 0
  if (activeTab.value === 'users') return users.value.length === 0
  return photos.value.length === 0
})

// 搜索框和筛选项全空时不算一次搜索，避免把"无条件"当成"查全部"
const hasCriteria = computed(() => {
  const f = filters.value
  return !!(f.keyword || f.tagId || f.albumId || f.startDate || f.endDate)
})

onMounted(async () => {
  // 支持从别处带关键词跳过来，例如首页的搜索入口
  const keyword = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  if (keyword) filters.value.keyword = keyword
  loadFilters()
  await run()
})

watch(activeTab, () => {
  page.value = 1
  run()
})

async function loadFilters() {
  try {
    const [albumList, tagList, likes] = await Promise.all([
      searchAlbums({ size: 100 }),
      listPopularTags(30),
      getMyLikes(),
    ])
    albumOptions.value = albumList
    tags.value = tagList
    likedPhotoIds.value = new Set(likes.likedPhotoIds ?? [])
  } catch {
    // ignore
  }
}

function buildQuery(): SearchQuery {
  return { ...filters.value, page: page.value, size: SIZE }
}

async function run(append = false) {
  if (busy.value) return
  if (!hasCriteria.value) {
    albums.value = []
    photos.value = []
    users.value = []
    hasMore.value = false
    loading.value = false
    return
  }
  busy.value = true
  if (!append) loading.value = true
  try {
    if (activeTab.value === 'users') {
      // 用户候选由后端限量返回，不参与分页
      users.value = await suggestUsers(filters.value.keyword)
      hasMore.value = false
    } else if (activeTab.value === 'albums') {
      const list = await searchAlbums(buildQuery())
      albums.value = append ? [...albums.value, ...list] : list
      hasMore.value = list.length === SIZE
    } else {
      const list = await searchPhotos(buildQuery())
      photos.value = append ? [...photos.value, ...list] : list
      hasMore.value = list.length === SIZE
    }
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '搜索失败')
  } finally {
    loading.value = false
    busy.value = false
  }
}

function search() {
  page.value = 1
  run()
}

function reset() {
  filters.value = { keyword: '', tagId: '', albumId: '', startDate: '', endDate: '' }
  search()
}

function loadMore() {
  page.value += 1
  run(true)
}

function handleLikeChange(photoId: string, liked: boolean) {
  const ids = new Set(likedPhotoIds.value)
  if (liked) ids.add(photoId)
  else ids.delete(photoId)
  likedPhotoIds.value = ids
}
</script>
