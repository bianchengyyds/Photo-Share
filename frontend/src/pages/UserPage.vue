<template>
  <div v-if="loading" class="flex items-center justify-center min-h-screen">
    <div class="text-gray-500">加载中...</div>
  </div>

  <div v-else-if="!profile" class="flex items-center justify-center min-h-screen">
    <div class="text-gray-500">用户不存在</div>
  </div>

  <div v-else class="max-w-6xl mx-auto px-4 py-8">
    <div class="bg-white rounded-xl border border-gray-100 shadow-sm p-5 sm:p-6">
      <div class="flex items-start gap-4">
        <img
          v-if="profile.avatarUrl"
          :src="profile.avatarUrl"
          :alt="displayName"
          class="w-16 h-16 sm:w-20 sm:h-20 rounded-full object-cover flex-shrink-0"
        />
        <div
          v-else
          class="w-16 h-16 sm:w-20 sm:h-20 rounded-full bg-gray-200 text-gray-500 text-2xl flex items-center justify-center flex-shrink-0"
        >
          {{ displayName.slice(0, 1) }}
        </div>

        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-3 flex-wrap">
            <h1 class="text-xl sm:text-2xl font-bold text-gray-900 truncate">{{ displayName }}</h1>
            <FollowButton
              v-if="!profile.self"
              :user-id="profile.id"
              :followed="profile.followedByMe"
              @change="handleFollowChange"
            />
          </div>
          <p class="text-sm text-gray-400 mt-0.5">@{{ profile.username }}</p>
          <p class="text-xs text-gray-400 mt-1">加入于 {{ formatDate(profile.createdAt) }}</p>

          <div class="flex gap-5 mt-4">
            <button
              v-for="tab in TABS"
              :key="tab.value"
              @click="activeTab = tab.value"
              :class="[
                'text-sm transition',
                activeTab === tab.value ? 'text-blue-600 font-medium' : 'text-gray-500 hover:text-gray-700',
              ]"
            >
              {{ tab.label }}
              <span class="text-gray-400">{{ tab.count() }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 相册 -->
    <template v-if="activeTab === 'albums'">
      <div v-if="!profile.canViewContent" class="text-center py-16 text-gray-400">
        <p class="text-lg">TA 还没有公开的相册</p>
        <p class="mt-2">{{ profile.followedByMe ? '对方把相册设为私密，只对自己可见' : '关注 TA 之后才能看到公开相册' }}</p>
      </div>

      <div v-else-if="profile.albums.length === 0" class="text-center py-16 text-gray-400">
        <p class="text-lg">还没有相册</p>
      </div>

      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 mt-6">
        <AlbumCard
          v-for="album in profile.albums"
          :key="album.id"
          :album="album"
          @open="router.push(`/album/${album.id}`)"
          @tags-change="(tags) => (album.tags = tags)"
        />
      </div>
    </template>

    <!-- 关注 / 粉丝 -->
    <div v-else class="mt-6">
      <p v-if="people.length === 0" class="text-center py-16 text-gray-400">
        {{ activeTab === 'following' ? '还没有关注任何人' : '还没有粉丝' }}
      </p>
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
        <UserCard
          v-for="person in people"
          :key="person.id"
          :user="person"
          @open="openUser(person.id)"
          @follow-change="(followed) => handlePeopleFollowChange(person, followed)"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserProfile, listFollowers, listFollowing } from '../services/user'
import type { UserBrief, UserProfile } from '../types'
import { formatDate } from '../utils/format'
import AlbumCard from '../components/AlbumCard.vue'
import UserCard from '../components/UserCard.vue'
import FollowButton from '../components/FollowButton.vue'

type TabValue = 'albums' | 'following' | 'followers'

const route = useRoute()
const router = useRouter()
const id = computed(() => route.params.id as string)

const profile = ref<UserProfile | null>(null)
const people = ref<UserBrief[]>([])
const loading = ref(true)
const activeTab = ref<TabValue>('albums')

const displayName = computed(() => profile.value?.nickname || profile.value?.username || '')

const TABS: { value: TabValue; label: string; count: () => number }[] = [
  { value: 'albums', label: '相册', count: () => profile.value?.albumCount ?? 0 },
  { value: 'following', label: '关注', count: () => profile.value?.followingCount ?? 0 },
  { value: 'followers', label: '粉丝', count: () => profile.value?.followerCount ?? 0 },
]

onMounted(() => load())

// 从粉丝列表点进另一个用户主页时组件复用，需要整体重置
watch(id, () => {
  activeTab.value = 'albums'
  people.value = []
  load()
})

watch(activeTab, (tab) => {
  if (tab !== 'albums' && people.value.length === 0) loadPeople(tab)
})

async function load(silent = false) {
  if (!silent) loading.value = true
  try {
    profile.value = await getUserProfile(id.value)
  } catch {
    profile.value = null
  } finally {
    if (!silent) loading.value = false
  }
}

async function loadPeople(tab: Exclude<TabValue, 'albums'>) {
  try {
    people.value = tab === 'following'
      ? await listFollowing(id.value)
      : await listFollowers(id.value)
  } catch {
    people.value = []
  }
}

function openUser(userId: string) {
  router.push(`/user/${userId}`)
}

function handleFollowChange(followed: boolean) {
  if (!profile.value) return
  profile.value = { ...profile.value, followedByMe: followed }
  profile.value.followerCount += followed ? 1 : -1
  // 关注即授权：关注状态一变，TA 公开出来的相册列表也跟着变
  load(true)
}

function handlePeopleFollowChange(person: UserBrief, followed: boolean) {
  person.followed = followed
}
</script>
