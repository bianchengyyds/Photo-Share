<template>
  <button
    type="button"
    :disabled="pending"
    :class="[
      'inline-flex items-center gap-1 rounded-full transition select-none',
      variant === 'overlay'
        ? 'px-2 py-1 text-xs backdrop-blur bg-black/40 text-white hover:bg-black/60'
        : 'px-2.5 py-1 text-sm border',
      variant === 'solid' && (liked ? 'bg-rose-50 text-rose-600 border-rose-200 hover:bg-rose-100' : 'bg-white text-gray-600 border-gray-300 hover:bg-gray-50'),
      pending ? 'opacity-60 cursor-wait' : 'cursor-pointer',
    ]"
    :title="tipText"
    @click.stop="toggle"
  >
    <svg class="w-4 h-4" viewBox="0 0 24 24" :fill="liked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
      <path stroke-linecap="round" stroke-linejoin="round" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
    </svg>
    <span class="tabular-nums">{{ count }}</span>
  </button>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { likeTarget, unlikeTarget } from '../services/like'
import type { TargetType } from '../types'

const props = withDefaults(
  defineProps<{
    targetType: TargetType
    targetId: string
    liked?: boolean
    count?: number
    // overlay 用于叠在照片上，solid 用于正文区
    variant?: 'solid' | 'overlay'
    // 未登录时只提示去登录，不发请求，避免触发全局 401 登出
    authed?: boolean
  }>(),
  { liked: false, count: 0, variant: 'solid', authed: true },
)

const emit = defineEmits<{
  (e: 'change', liked: boolean): void
  (e: 'need-login'): void
}>()

const liked = ref(props.liked)
const count = ref(props.count)
const pending = ref(false)

// liked 由父组件回传后可能再次变化，只在没有请求在途时才用 props 覆盖本地值，
// 否则会把点赞的乐观计数冲掉
watch(
  () => props.liked,
  (next) => {
    liked.value = next
  },
)

watch(
  () => props.count,
  (next) => {
    if (!pending.value) count.value = next
  },
)

const tipText = computed(() => {
  if (!props.authed) return '登录后才能点赞'
  return liked.value ? '取消点赞' : '点赞'
})

async function toggle() {
  if (pending.value) return
  if (!props.authed) {
    emit('need-login')
    return
  }

  const next = !liked.value
  // 乐观更新，请求失败再回滚，避免点赞这种高频小操作出现等待感
  apply(next)
  pending.value = true
  try {
    if (next) await likeTarget(props.targetType, props.targetId)
    else await unlikeTarget(props.targetType, props.targetId)
  } catch (e: unknown) {
    apply(!next)
    alert(e instanceof Error ? e.message : '操作失败')
  } finally {
    pending.value = false
  }
}

function apply(next: boolean) {
  liked.value = next
  count.value += next ? 1 : -1
  emit('change', next)
}
</script>
