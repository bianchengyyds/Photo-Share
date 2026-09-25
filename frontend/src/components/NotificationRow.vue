<template>
  <div
    @click="emit('open')"
    :class="[
      'flex items-start gap-3 p-3 rounded-lg transition',
      clickable ? 'hover:bg-gray-50 cursor-pointer' : '',
      !item.isRead ? 'bg-blue-50/60' : '',
    ]"
  >
    <img
      v-if="item.actorAvatarUrl"
      :src="item.actorAvatarUrl"
      :alt="item.actorName || ''"
      class="w-8 h-8 rounded-full object-cover flex-shrink-0"
    />
    <div
      v-else
      class="w-8 h-8 rounded-full bg-gray-200 text-gray-500 flex items-center justify-center text-sm flex-shrink-0"
    >
      {{ (item.actorName || '?').slice(0, 1) }}
    </div>

    <div class="flex-1 min-w-0">
      <p class="text-sm text-gray-800">
        <span class="font-medium">{{ item.actorName || '用户' }}</span>
        {{ actionText }}
        <span v-if="item.targetTitle" class="text-gray-500 truncate">· {{ item.targetTitle }}</span>
      </p>
      <p v-if="item.snippet" class="text-xs text-gray-500 mt-0.5 line-clamp-2">{{ item.snippet }}</p>
      <p class="text-xs text-gray-400 mt-1">{{ formatRelativeTime(item.createdAt) }}</p>
    </div>

    <span v-if="!item.isRead" class="w-2 h-2 rounded-full bg-blue-500 flex-shrink-0 mt-2" />

    <button
      v-if="removable"
      @click.stop="emit('remove')"
      class="text-xs text-gray-400 hover:text-red-500 flex-shrink-0"
    >
      删除
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { NotificationItem } from '../types'
import { describeNotification } from '../utils/notification'
import { formatRelativeTime } from '../utils/format'

const props = withDefaults(
  defineProps<{
    item: NotificationItem
    removable?: boolean
    clickable?: boolean
  }>(),
  { removable: false, clickable: true },
)

const emit = defineEmits<{ (e: 'open'): void; (e: 'remove'): void }>()

const actionText = computed(() => describeNotification(props.item))
</script>
