<template>
  <div class="flex flex-wrap items-center gap-1">
    <span
      v-for="group in groups"
      :key="group.name"
      :class="[
        'inline-flex items-center gap-0.5 rounded-full bg-gray-100 text-gray-600',
        size === 'sm' ? 'px-1.5 py-0.5 text-[11px]' : 'px-2 py-0.5 text-xs',
      ]"
    >
      <span class="truncate max-w-24" :title="group.name">#{{ group.name }}</span>
      <button
        v-if="group.mine"
        type="button"
        class="text-gray-400 hover:text-red-500 pl-0.5"
        title="删除我添加的这个标签"
        @click.stop="removeMine(group)"
      >
        <svg class="w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </span>

    <button
      v-if="authed && !adding"
      type="button"
      :class="[
        'inline-flex items-center rounded-full border border-dashed border-gray-300 text-gray-400 hover:text-blue-600 hover:border-blue-400 transition',
        size === 'sm' ? 'px-1.5 py-0.5 text-[11px]' : 'px-2 py-0.5 text-xs',
      ]"
      title="添加标签"
      @click.stop="openInput"
    >
      + 标签
    </button>

    <input
      v-if="adding"
      ref="inputEl"
      v-model.trim="draft"
      type="text"
      maxlength="50"
      placeholder="标签名，回车确认"
      :class="[
        'rounded-full border border-blue-400 focus:outline-none',
        size === 'sm' ? 'w-20 px-1.5 py-0.5 text-[11px]' : 'w-28 px-2 py-0.5 text-xs',
      ]"
      @keyup.enter="submit"
      @keyup.escape="closeInput"
      @blur="closeInput"
      @click.stop
    />
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { addTag, deleteTag } from '../services/tag'
import { useUserStore } from '../stores/user'
import type { TagView, TargetType } from '../types'

interface TagGroup {
  name: string
  mine: TagView | null
}

const props = withDefaults(
  defineProps<{
    targetType: TargetType
    targetId: string
    tags?: TagView[]
    authed?: boolean
    size?: 'sm' | 'md'
  }>(),
  { tags: () => [], authed: true, size: 'md' },
)

const emit = defineEmits<{ (e: 'change', tags: TagView[]): void }>()

const userStore = useUserStore()
const myId = computed(() => userStore.user?.id)

// 后端可能返回 null（该实体未装配标签），withDefaults 只对 undefined 生效
const list = ref<TagView[]>(props.tags ?? [])
watch(
  () => props.tags,
  (next) => {
    list.value = next ?? []
  },
)

// 不同人可以打同名标签，展示时按名字合并成一个，删除时只删自己那条关联
const groups = computed<TagGroup[]>(() => {
  const map = new Map<string, TagGroup>()
  for (const tag of list.value) {
    const group = map.get(tag.name)
    if (!group) {
      map.set(tag.name, { name: tag.name, mine: tag.userId === myId.value ? tag : null })
    } else if (!group.mine && tag.userId === myId.value) {
      group.mine = tag
    }
  }
  return Array.from(map.values())
})

const adding = ref(false)
const draft = ref('')
const inputEl = ref<HTMLInputElement | null>(null)

function openInput() {
  draft.value = ''
  adding.value = true
  nextTick(() => inputEl.value?.focus())
}

function closeInput() {
  adding.value = false
  draft.value = ''
}

async function submit() {
  const name = draft.value.trim()
  if (!name) {
    closeInput()
    return
  }
  adding.value = false
  try {
    const created = await addTag(props.targetType, props.targetId, name)
    list.value = [...list.value, created]
    emit('change', list.value)
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '添加标签失败')
  }
}

async function removeMine(group: TagGroup) {
  if (!group.mine) return
  const relationId = group.mine.relationId
  try {
    await deleteTag(relationId)
    list.value = list.value.filter((tag) => tag.relationId !== relationId)
    emit('change', list.value)
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '删除标签失败')
  }
}
</script>
