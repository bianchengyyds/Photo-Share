<template>
  <button
    v-if="!self"
    type="button"
    :disabled="pending"
    :class="[
      'inline-flex items-center gap-1 rounded-full text-sm transition select-none',
      following
        ? 'bg-gray-100 text-gray-600 hover:bg-gray-200'
        : 'bg-blue-600 text-white hover:bg-blue-700',
      compact ? 'px-2.5 py-1 text-xs' : 'px-4 py-2',
      pending ? 'opacity-60 cursor-wait' : 'cursor-pointer',
    ]"
    @click.stop="toggle"
  >
    {{ following ? '已关注' : '关注' }}
  </button>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { followUser, unfollowUser } from '../services/user'

const props = withDefaults(
  defineProps<{
    userId: string
    followed?: boolean
    // 未登录时只提示去登录，不发请求，避免触发全局 401 登出
    authed?: boolean
    // 自己不需要关注按钮
    self?: boolean
    compact?: boolean
  }>(),
  { followed: false, authed: true, self: false, compact: false },
)

const emit = defineEmits<{
  (e: 'change', followed: boolean): void
  (e: 'need-login'): void
}>()

const following = ref(props.followed)
const pending = ref(false)

watch(
  () => props.followed,
  (next) => {
    following.value = next
  },
)

async function toggle() {
  if (pending.value) return
  if (!props.authed) {
    emit('need-login')
    return
  }

  const next = !following.value
  // 关注是高频小操作，先给反馈再落库，失败回滚
  following.value = next
  pending.value = true
  try {
    if (next) await followUser(props.userId)
    else await unfollowUser(props.userId)
    emit('change', next)
  } catch (e: unknown) {
    following.value = !next
    alert(e instanceof Error ? e.message : '操作失败')
  } finally {
    pending.value = false
  }
}
</script>
