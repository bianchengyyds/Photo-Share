<template>
  <div class="max-w-2xl mx-auto px-4 py-8">
    <div class="flex items-center justify-between mb-4">
      <h1 class="text-2xl font-bold text-gray-900">通知</h1>
      <button
        v-if="unread > 0"
        @click="handleMarkAllRead"
        class="px-3 py-1.5 text-sm text-blue-600 border border-blue-200 rounded-lg hover:bg-blue-50 transition"
      >
        全部标记已读 ({{ unread }})
      </button>
    </div>

    <div class="flex gap-1 mb-5 border-b border-gray-200">
      <button
        v-for="tab in TABS"
        :key="tab.value"
        @click="switchTab(tab.value)"
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

    <div v-if="loading" class="text-sm text-gray-400 py-6">通知加载中...</div>

    <div v-else-if="items.length === 0" class="text-center py-16 text-gray-400">
      <p class="text-lg">{{ activeTab === 'unread' ? '没有未读通知' : '还没有收到通知' }}</p>
      <p class="mt-2">有人点赞、评论、关注你或 @ 你时会出现在这里</p>
    </div>

    <div v-else class="space-y-1">
      <NotificationRow
        v-for="item in items"
        :key="item.id"
        :item="item"
        removable
        @open="handleOpen(item)"
        @remove="handleRemove(item.id)"
      />

      <div v-if="hasMore" class="text-center pt-4">
        <button
          @click="loadMore"
          :disabled="loadingMore"
          class="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition disabled:opacity-50"
        >
          {{ loadingMore ? '加载中...' : '加载更多' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  deleteNotification,
  getUnreadCount,
  listNotifications,
  markAllRead,
  markRead,
} from '../services/notification'
import { notificationLink } from '../utils/notification'
import NotificationRow from '../components/NotificationRow.vue'
import type { NotificationItem } from '../types'

const TABS = [
  { value: 'all', label: '全部' },
  { value: 'unread', label: '未读' },
] as const

type TabValue = typeof TABS[number]['value']

const SIZE = 20

const router = useRouter()
const items = ref<NotificationItem[]>([])
const activeTab = ref<TabValue>('all')
const loading = ref(true)
const loadingMore = ref(false)
const hasMore = ref(false)
const unread = ref(0)
const page = ref(1)

onMounted(() => load())

function switchTab(tab: TabValue) {
  if (activeTab.value === tab) return
  activeTab.value = tab
  page.value = 1
  load()
}

async function load() {
  if (page.value === 1) loading.value = true
  try {
    const list = await listNotifications(activeTab.value === 'unread', page.value, SIZE)
    items.value = page.value === 1 ? list : [...items.value, ...list]
    hasMore.value = list.length === SIZE
    unread.value = await getUnreadCount()
  } catch {
    // 表格未建或网络异常时保留当前列表
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  if (loadingMore.value) return
  loadingMore.value = true
  page.value += 1
  await load()
  loadingMore.value = false
}

async function handleOpen(item: NotificationItem) {
  if (!item.isRead) {
    item.isRead = true
    unread.value = Math.max(0, unread.value - 1)
    markRead(item.id).catch(() => undefined)
  }
  const link = notificationLink(item)
  if (link) router.push(link)
}

async function handleRemove(id: string) {
  try {
    await deleteNotification(id)
    items.value = items.value.filter((item) => item.id !== id)
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '删除失败')
  }
}

async function handleMarkAllRead() {
  try {
    await markAllRead()
    await load()
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '操作失败')
  }
}
</script>
