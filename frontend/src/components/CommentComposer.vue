<template>
  <div class="relative">
    <textarea
      ref="textareaRef"
      v-model="text"
      rows="2"
      maxlength="500"
      :placeholder="placeholder"
      class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
      @input="syncMention"
      @keyup.esc="closeMention"
      @click="syncMention"
      @blur="hidePanelSoon"
    />

    <!-- @ 选人面板：输入 @ 或点右下角 @ 按钮唤起 -->
    <div
      v-if="panelOpen"
      class="absolute left-0 right-0 mt-1 z-20 bg-white border border-gray-200 rounded-lg shadow-lg overflow-hidden"
    >
      <p v-if="candidates.length === 0" class="px-3 py-2 text-sm text-gray-400">没有匹配的用户</p>
      <button
        v-for="item in candidates"
        :key="item.id"
        type="button"
        class="w-full flex items-center gap-2 px-3 py-2 text-left hover:bg-gray-50"
        @mousedown.prevent="pick(item)"
      >
        <img
          v-if="item.avatarUrl"
          :src="item.avatarUrl"
          :alt="item.nickname || item.username"
          class="w-7 h-7 rounded-full object-cover flex-shrink-0"
        />
        <span
          v-else
          class="w-7 h-7 rounded-full bg-gray-200 text-gray-500 flex items-center justify-center text-xs flex-shrink-0"
        >
          {{ (item.nickname || item.username).slice(0, 1) }}
        </span>
        <span class="flex-1 min-w-0">
          <span class="block text-sm text-gray-900 truncate">{{ item.nickname || item.username }}</span>
          <span class="block text-xs text-gray-400 truncate">@{{ item.username }}</span>
        </span>
      </button>
    </div>

    <!-- 已选中的 @ 对象，提交时作为 mentionUserIds 发给后端 -->
    <div v-if="mentions.length" class="flex flex-wrap gap-1.5 mt-2">
      <span
        v-for="item in mentions"
        :key="item.id"
        class="inline-flex items-center gap-1 px-2 py-0.5 bg-blue-50 text-blue-700 rounded-full text-xs"
      >
        @{{ item.nickname || item.username }}
        <button type="button" class="text-blue-400 hover:text-blue-600" @click="dropMention(item.id)">
          ×
        </button>
      </span>
    </div>

    <div class="flex items-center justify-between mt-2">
      <div class="flex items-center gap-3">
        <button
          type="button"
          class="text-sm text-gray-500 hover:text-blue-600"
          title="提及某人"
          @click="openMention"
        >
          @ 提及
        </button>
        <span class="text-xs text-gray-400">{{ text.length }}/500</span>
      </div>
      <button
        type="button"
        @click="submit"
        :disabled="!text.trim() || submitting"
        class="px-4 py-1.5 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {{ submitting ? '发表中...' : submitLabel }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { suggestUsers } from '../services/user'
import type { UserBrief } from '../types'

withDefaults(
  defineProps<{
    placeholder?: string
    submitLabel?: string
    submitting?: boolean
  }>(),
  { placeholder: '说说你的想法', submitLabel: '发表评论', submitting: false },
)

const emit = defineEmits<{ (e: 'submit', content: string, mentionUserIds: string[]): void }>()

const text = ref('')
const textareaRef = ref<HTMLTextAreaElement | null>(null)
const panelOpen = ref(false)
const candidates = ref<UserBrief[]>([])
const mentions = ref<UserBrief[]>([])

// 当前 @ 片段的起点，用于选中后原位替换成 @昵称
let mentionStart = -1
let keyword = ''
let hideTimer: ReturnType<typeof setTimeout> | null = null

function syncMention() {
  const el = textareaRef.value
  if (!el) return
  const caret = el.selectionStart ?? el.value.length
  const match = /@([^\s@]*)$/.exec(el.value.slice(0, caret))
  if (!match) {
    panelOpen.value = false
    mentionStart = -1
    return
  }
  keyword = match[1]
  mentionStart = caret - match[0].length
  loadCandidates()
}

function loadCandidates() {
  if (hideTimer) clearTimeout(hideTimer)
  panelOpen.value = true
  // 先保留上一次结果，避免搜索结果回来前面板闪空
  suggestUsers(keyword)
    .then((list) => {
      candidates.value = list.filter((u) => !mentions.value.some((m) => m.id === u.id))
    })
    .catch(() => {
      candidates.value = []
    })
}

function openMention() {
  const el = textareaRef.value
  if (!el) return
  el.focus()
  const caret = el.selectionStart ?? el.value.length
  text.value = el.value.slice(0, caret) + '@' + el.value.slice(caret)
  el.setSelectionRange(caret + 1, caret + 1)
  keyword = ''
  mentionStart = caret
  loadCandidates()
}

function pick(user: UserBrief) {
  const el = textareaRef.value
  const name = user.nickname || user.username
  const caret = el ? (el.selectionStart ?? text.value.length) : text.value.length
  const before = text.value.slice(0, mentionStart >= 0 ? mentionStart : caret)
  const after = text.value.slice(caret)
  text.value = `${before}@${name} ${after}`
  mentions.value = [...mentions.value, user]
  closeMention()
  el?.focus()
}

function dropMention(id: string) {
  mentions.value = mentions.value.filter((u) => u.id !== id)
}

function closeMention() {
  panelOpen.value = false
  mentionStart = -1
}

function hidePanelSoon() {
  // mousedown.prevent 已保证点击候选行不会先触发 blur，这里只处理真的移开焦点
  if (hideTimer) clearTimeout(hideTimer)
  hideTimer = setTimeout(closeMention, 150)
}

function submit() {
  const content = text.value.trim()
  if (!content) return
  emit('submit', content, mentions.value.map((u) => u.id))
  text.value = ''
  mentions.value = []
  closeMention()
}
</script>
