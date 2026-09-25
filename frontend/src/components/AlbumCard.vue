<template>
  <div
    @click="emit('open')"
    class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden cursor-pointer hover:shadow-md transition group"
  >
    <img
      v-if="album.coverUrl"
      :src="album.coverUrl"
      :alt="album.title"
      class="w-full h-48 object-cover"
    />
    <div v-else class="w-full h-48 bg-gray-100 flex items-center justify-center">
      <svg class="w-12 h-12 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
      </svg>
    </div>

    <div class="p-4">
      <div class="flex items-center justify-between gap-2">
        <h3 class="font-semibold text-gray-900 truncate">{{ album.title }}</h3>
        <div class="flex items-center gap-1.5 flex-shrink-0">
          <span
            v-if="showVisibility"
            :class="[
              'px-1.5 py-0.5 rounded text-[11px]',
              album.visibility === 'PUBLIC'
                ? 'bg-emerald-50 text-emerald-600'
                : 'bg-gray-100 text-gray-500',
            ]"
          >
            {{ album.visibility === 'PUBLIC' ? '公开' : '私密' }}
          </span>
          <button
            v-if="deletable"
            @click.stop="emit('remove')"
            class="text-gray-400 hover:text-red-500 opacity-0 group-hover:opacity-100 transition"
          >
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </button>
        </div>
      </div>

      <p v-if="album.description" class="text-sm text-gray-500 mt-1 truncate">{{ album.description }}</p>

      <TagChips
        class="mt-2"
        target-type="ALBUM"
        :target-id="album.id"
        :tags="album.tags"
        @change="emit('tags-change', $event)"
      />

      <div class="flex items-center gap-3 mt-3">
        <LikeButton
          target-type="ALBUM"
          :target-id="album.id"
          :liked="liked"
          :count="album.likeCount ?? 0"
          @change="emit('like-change', $event)"
        />
        <span class="inline-flex items-center gap-1 text-xs text-gray-400">
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
          </svg>
          {{ album.commentCount ?? 0 }}
        </span>
        <span class="ml-auto text-xs text-gray-400">{{ formatRelativeTime(album.createdAt) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import LikeButton from './LikeButton.vue'
import TagChips from './TagChips.vue'
import type { Album, TagView } from '../types'
import { formatRelativeTime } from '../utils/format'

defineProps<{
  album: Album
  liked?: boolean
  // 自己的相册才显示删除按钮
  deletable?: boolean
  // 自己的相册才需要看到可见范围
  showVisibility?: boolean
}>()

const emit = defineEmits<{
  (e: 'open'): void
  (e: 'remove'): void
  (e: 'like-change', liked: boolean): void
  (e: 'tags-change', tags: TagView[]): void
}>()
</script>
