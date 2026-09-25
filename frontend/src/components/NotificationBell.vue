<template>
  <div class="relative">
    <button
      type="button"
      @click="toggle"
      class="relative p-2 rounded-full text-gray-600 hover:bg-gray-100 transition"
      :title="unread ? `${unread} 条未读通知` : '通知'"
    >
      <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          d="M15 17h5l-1.4-1.4A2 2 0 0118 14.2V11a6 6 0 10-12 0v3.2a2 2 0 01-.6 1.4L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
        />
      </svg>
      <span
        v-if="unread > 0"
        class="absolute -top-0.5 -right-0.5 min-w-[18px] h-[18px] px-1 rounded-full bg-red-500 text-white text-[10px] leading-[18px] text-center"
      >
        {{ unread > 99 ? '99+' : unread }}
      </span>
    </button>

    <template v-if="open">
      <!-- 点面板外收起，避免注册全局监听 -->
      <div class="fixed inset-0 z-40" @click="open = false" />
      <div class="absolute right-0 mt-2 w-80 max-w-[calc(100vw-2rem)] bg-white border border-gray-200 rounded-xl shadow-lg z-50 overflow-hidden">
        <div class="flex items-center justify-between px-4 py-2.5 border-b border-gray-100">
          <span class="text-sm font-semibold text-gray-900">通知</span>
          <button
            v-if="unread > 0"
            @click="handleMarkAllRead"
            class="text-xs text-blue-600 hover:underline"
          >
            全部已读
          </button>
        </div>

        <p v-if="loading" class="px-4 py-6 text-sm text-gray-400 text-center">加载中...</p>
        <p v-else-if="items.length === 0" class="px-4 py-6 text-sm text-gray-400 text-center">
          暂无通知
        </p>
        <div v-else class="max-h-80 overflow-y-auto py-1">
          <NotificationRow
            v-for="item in items"
            :key="item.id"
            :item="item"
            @open="handleOpen(item)"
          />
        </div>

        <router-link
          to="/notifications"
          class="block px-4 py-2.5 text-center text-sm text-blue-600 hover:bg-gray-50 border-t border-gray-100"
          @click="open = false"
        >
          查看全部
        </router-link>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUnreadCount, listNotifications, markAllRead, markRead } from '../services/notification'
import { notificationLink } from '../utils/notification'
import NotificationRow from './NotificationRow.vue'
import type { NotificationItem } from '../types'

const route = useRoute()
const router = useRouter()
const open = ref(false)
const loading = ref(false)
const unread = ref(0)
const items = ref<NotificationItem[]>([])

onMounted(refreshCount)

// 从通知页返回后角标要跟上，这里跟着路由刷新未读数
watch(() => route.path, refreshCount)

async function toggle() {
  open.value = !open.value
  if (open.value) await loadPanel()
}

async function loadPanel() {
  loading.value = true
  try {
    items.value = await listNotifications(false, 1, 10)
    unread.value = await getUnreadCount()
  } catch {
    // 未读数是锦上添花，失败时保持上一次的显示
  } finally {
    loading.value = false
  }
}

async function refreshCount() {
  try {
    unread.value = await getUnreadCount()
  } catch {
    // ignore
  }
}

async function handleMarkAllRead() {
  try {
    await markAllRead()
    unread.value = 0
    for (const item of items.value) item.isRead = true
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '操作失败')
  }
}

async function handleOpen(item: NotificationItem) {
  open.value = false
  if (!item.isRead) {
    item.isRead = true
    unread.value = Math.max(0, unread.value - 1)
    markRead(item.id).catch(() => undefined)
  }
  const link = notificationLink(item)
  if (link) router.push(link)
}
</script>
