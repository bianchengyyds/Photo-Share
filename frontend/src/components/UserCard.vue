<template>
  <div
    @click="emit('open')"
    class="flex items-center gap-3 p-3 bg-white rounded-xl border border-gray-100 shadow-sm hover:shadow-md transition cursor-pointer"
  >
    <img
      v-if="user.avatarUrl"
      :src="user.avatarUrl"
      :alt="displayName"
      class="w-11 h-11 rounded-full object-cover flex-shrink-0"
    />
    <div
      v-else
      class="w-11 h-11 rounded-full bg-gray-200 text-gray-500 flex items-center justify-center flex-shrink-0"
    >
      {{ displayName.slice(0, 1) }}
    </div>

    <div class="flex-1 min-w-0">
      <p class="text-sm font-medium text-gray-900 truncate">{{ displayName }}</p>
      <p class="text-xs text-gray-400 truncate">@{{ user.username }}</p>
    </div>

    <FollowButton
      :user-id="user.id"
      :followed="user.followed ?? false"
      :self="self"
      compact
      @change="emit('follow-change', $event)"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import FollowButton from './FollowButton.vue'
import type { UserBrief } from '../types'

const props = defineProps<{
  user: UserBrief
  self?: boolean
}>()

const emit = defineEmits<{
  (e: 'open'): void
  (e: 'follow-change', followed: boolean): void
}>()

const displayName = computed(() => props.user.nickname || props.user.username)
</script>
