<template>
  <section class="bg-white rounded-xl border border-gray-100 shadow-sm p-4 sm:p-6">
    <h2 class="text-base font-semibold text-gray-900 mb-4">
      评论
      <span class="text-gray-400 font-normal">({{ comments.length }})</span>
    </h2>

    <!-- 登录后可发表评论与回复；分享页游客只保留阅读能力 -->
    <CommentComposer
      v-if="canWrite"
      class="mb-5"
      :submitting="submitting"
      :placeholder="placeholder"
      @submit="(content, ids) => handleSubmit(content, ids, null)"
    />
    <p v-else class="mb-5 text-sm text-gray-400">
      登录后即可参与讨论。
      <button @click="emit('need-login')" class="text-blue-600 hover:underline">去登录</button>
    </p>

    <div v-if="loading" class="text-sm text-gray-400 py-4">评论加载中...</div>
    <div v-else-if="threads.length === 0" class="text-sm text-gray-400 py-4 text-center">
      还没有评论
    </div>

    <ul v-else class="space-y-5">
      <li v-for="thread in threads" :key="thread.root.id" class="flex gap-3">
        <img
          v-if="thread.root.avatarUrl"
          :src="thread.root.avatarUrl"
          :alt="thread.root.userName"
          class="w-8 h-8 rounded-full object-cover flex-shrink-0"
        />
        <div
          v-else
          class="w-8 h-8 text-sm rounded-full bg-gray-200 text-gray-500 flex items-center justify-center flex-shrink-0"
        >
          {{ (thread.root.userName || '?').slice(0, 1) }}
        </div>

        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2">
            <router-link
              v-if="authed && thread.root.userId"
              :to="`/user/${thread.root.userId}`"
              class="text-sm font-medium text-gray-900 truncate hover:text-blue-600"
            >
              {{ thread.root.userName || '用户' }}
            </router-link>
            <span v-else class="text-sm font-medium text-gray-900 truncate">
              {{ thread.root.userName || '用户' }}
            </span>
            <span class="text-xs text-gray-400 flex-shrink-0">
              {{ formatRelativeTime(thread.root.createdAt) }}
            </span>
            <div class="ml-auto flex items-center gap-2 flex-shrink-0">
              <button
                v-if="canWrite"
                @click="openReply(thread.root)"
                class="text-xs text-gray-400 hover:text-blue-600"
              >
                回复
              </button>
              <button
                v-if="canDelete(thread.root)"
                @click="handleDelete(thread.root.id)"
                class="text-xs text-gray-400 hover:text-red-500"
              >
                删除
              </button>
            </div>
          </div>

          <p class="text-sm text-gray-700 mt-0.5 break-words whitespace-pre-wrap">{{ thread.root.content }}</p>

          <div v-if="replyTarget?.id === thread.root.id" class="mt-2">
            <p class="text-xs text-gray-500 mb-1.5 flex items-center gap-2">
              回复 @{{ thread.root.userName || '用户' }}
              <button @click="replyTarget = null" class="text-gray-400 hover:text-gray-600">取消</button>
            </p>
            <CommentComposer
              submit-label="发表回复"
              placeholder="友好地说点什么"
              :submitting="submitting"
              @submit="(content, ids) => handleSubmit(content, ids, thread.root.id)"
            />
          </div>

          <!-- 二级评论：整组可折叠，折叠后只剩一条"展开 N 条回复" -->
          <div v-if="thread.replies.length" class="mt-3">
            <ul v-if="!isCollapsed(thread.root.id)" class="space-y-3 pl-3 border-l-2 border-gray-100">
              <li v-for="reply in visibleReplies(thread)" :key="reply.id" class="flex gap-2.5">
                <img
                  v-if="reply.avatarUrl"
                  :src="reply.avatarUrl"
                  :alt="reply.userName"
                  class="w-7 h-7 rounded-full object-cover flex-shrink-0"
                />
                <div
                  v-else
                  class="w-7 h-7 text-xs rounded-full bg-gray-200 text-gray-500 flex items-center justify-center flex-shrink-0"
                >
                  {{ (reply.userName || '?').slice(0, 1) }}
                </div>

                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2">
                    <router-link
                      v-if="authed && reply.userId"
                      :to="`/user/${reply.userId}`"
                      class="text-sm font-medium text-gray-900 truncate hover:text-blue-600"
                    >
                      {{ reply.userName || '用户' }}
                    </router-link>
                    <span v-else class="text-sm font-medium text-gray-900 truncate">
                      {{ reply.userName || '用户' }}
                    </span>
                    <span class="text-xs text-gray-400 truncate">回复 @{{ replyLabel(thread, reply) }}</span>
                    <span class="text-xs text-gray-400 flex-shrink-0">
                      {{ formatRelativeTime(reply.createdAt) }}
                    </span>
                    <div class="ml-auto flex items-center gap-2 flex-shrink-0">
                      <button
                        v-if="canWrite"
                        @click="openReply(reply)"
                        class="text-xs text-gray-400 hover:text-blue-600"
                      >
                        回复
                      </button>
                      <button
                        v-if="canDelete(reply)"
                        @click="handleDelete(reply.id)"
                        class="text-xs text-gray-400 hover:text-red-500"
                      >
                        删除
                      </button>
                    </div>
                  </div>

                  <p class="text-sm text-gray-700 mt-0.5 break-words whitespace-pre-wrap">{{ reply.content }}</p>

                  <!-- 回复一条二级评论时，parentId 仍传这条回复，后端负责挂到同一条一级评论下 -->
                  <div v-if="replyTarget?.id === reply.id" class="mt-2">
                    <p class="text-xs text-gray-500 mb-1.5 flex items-center gap-2">
                      回复 @{{ reply.userName || '用户' }}
                      <button @click="replyTarget = null" class="text-gray-400 hover:text-gray-600">取消</button>
                    </p>
                    <CommentComposer
                      submit-label="发表回复"
                      placeholder="友好地说点什么"
                      :submitting="submitting"
                      @submit="(content, ids) => handleSubmit(content, ids, reply.id)"
                    />
                  </div>
                </div>
              </li>
            </ul>

            <div class="flex items-center gap-3 text-xs mt-1.5">
              <button
                v-if="isCollapsed(thread.root.id)"
                @click="setCollapsed(thread.root.id, false)"
                class="text-blue-600 hover:underline"
              >
                展开 {{ thread.replies.length }} 条回复
              </button>
              <template v-else>
                <button
                  v-if="hiddenReplyCount(thread) > 0"
                  @click="showMoreReplies(thread)"
                  class="text-blue-600 hover:underline"
                >
                  展示更多
                </button>
                <button
                  @click="setCollapsed(thread.root.id, true)"
                  class="text-gray-400 hover:text-gray-600"
                >
                  收起
                </button>
              </template>
            </div>
          </div>
        </div>
      </li>
    </ul>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { addComment, deleteComment, listComments } from '../services/comment'
import { addShareComment, getShareComments } from '../services/share'
import { useUserStore } from '../stores/user'
import CommentComposer from './CommentComposer.vue'
import type { AlbumComment } from '../types'
import { formatRelativeTime } from '../utils/format'

const props = withDefaults(
  defineProps<{
    // 分享页走 shareCode（游客可读），相册详情页走 albumId（需登录）
    albumId?: string | null
    shareCode?: string | null
    sharePassword?: string
    authed?: boolean
    // 相册所有者可删除相册下的任意评论
    isOwner?: boolean
  }>(),
  { albumId: null, shareCode: null, sharePassword: undefined, authed: true, isOwner: false },
)

const emit = defineEmits<{ (e: 'change', count: number): void; (e: 'need-login'): void }>()

const userStore = useUserStore()
const comments = ref<AlbumComment[]>([])
const loading = ref(true)
const submitting = ref(false)
const replyTarget = ref<AlbumComment | null>(null)

// 相册详情页用 albumId 写，分享页用 shareCode 写（链接本身就是访问授权）
const canWrite = computed(() => props.authed && !!(props.albumId || props.shareCode))

const placeholder = computed(() =>
  props.shareCode ? '发表你的看法（对所有人可见）' : '说说你的想法',
)

interface CommentThread {
  root: AlbumComment
  replies: AlbumComment[]
}

// 折叠前每页展示的二级评论条数
const REPLIES_PAGE = 3

const visibleReplyCount = ref<Record<string, number>>({})
const collapsedThreads = ref<Record<string, boolean>>({})

// 后端给的是按时间倒序的扁平列表，回复比父评论更新会排在前面，
// 因此先取出所有一级评论，再把各自的回复按正序收进同一条线程
const threads = computed<CommentThread[]>(() => {
  const list = comments.value
  const repliesByParent = new Map<string, AlbumComment[]>()
  for (const comment of list) {
    if (!comment.parentId) continue
    const siblings = repliesByParent.get(comment.parentId)
    if (siblings) siblings.push(comment)
    else repliesByParent.set(comment.parentId, [comment])
  }

  const result: CommentThread[] = []
  for (const comment of list) {
    if (comment.parentId) continue
    const replies = (repliesByParent.get(comment.id) ?? []).slice().reverse()
    result.push({ root: comment, replies })
  }
  return result
})

function isCollapsed(rootId: string) {
  return !!collapsedThreads.value[rootId]
}

function setCollapsed(rootId: string, value: boolean) {
  collapsedThreads.value[rootId] = value
  // 重新展开回到"默认三条"，不保留上次点开的分页
  if (!value) delete visibleReplyCount.value[rootId]
}

function shownReplyCount(thread: CommentThread) {
  return Math.min(visibleReplyCount.value[thread.root.id] ?? REPLIES_PAGE, thread.replies.length)
}

function visibleReplies(thread: CommentThread) {
  return thread.replies.slice(0, shownReplyCount(thread))
}

function hiddenReplyCount(thread: CommentThread) {
  return thread.replies.length - shownReplyCount(thread)
}

function showMoreReplies(thread: CommentThread) {
  visibleReplyCount.value[thread.root.id] = shownReplyCount(thread) + REPLIES_PAGE
}

// 早期数据没有 reply_to_user_id，那时回复只可能指向一级评论的作者
function replyLabel(thread: CommentThread, reply: AlbumComment) {
  return reply.replyToUserName || thread.root.userName || '用户'
}

function revealReply(rootId: string, replyId: string) {
  const thread = threads.value.find((item) => item.root.id === rootId)
  if (!thread) return
  collapsedThreads.value[rootId] = false
  const index = thread.replies.findIndex((item) => item.id === replyId)
  const needed = index < 0 ? thread.replies.length : index + 1
  if ((visibleReplyCount.value[rootId] ?? REPLIES_PAGE) < needed) {
    visibleReplyCount.value[rootId] = needed
  }
}

watch(
  () => [props.albumId, props.shareCode],
  () => {
    visibleReplyCount.value = {}
    collapsedThreads.value = {}
    replyTarget.value = null
    load()
  },
)

// silent 用于发帖后刷新：不显示加载态，避免整块列表闪一下
async function load(silent = false) {
  const target = props.shareCode ?? props.albumId
  if (!target) {
    comments.value = []
    loading.value = false
    return
  }
  if (!silent) loading.value = true
  try {
    comments.value = props.shareCode
      ? await getShareComments(props.shareCode, props.sharePassword)
      : await listComments(props.albumId as string)
    emit('change', comments.value.length)
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

function canDelete(comment: AlbumComment) {
  return props.isOwner || comment.userId === userStore.user?.id
}

function openReply(comment: AlbumComment) {
  if (replyTarget.value?.id === comment.id) {
    replyTarget.value = null
    return
  }
  replyTarget.value = comment
  if (comment.parentId) revealReply(comment.parentId, comment.id)
}

async function handleSubmit(content: string, mentionUserIds: string[], parentId: string | null) {
  if (!canWrite.value || submitting.value) return
  submitting.value = true
  try {
    if (props.shareCode) {
      await addShareComment(props.shareCode, {
        content,
        parentId,
        mentionUserIds,
        password: props.sharePassword,
      })
    } else {
      await addComment(props.albumId as string, content, parentId, mentionUserIds)
    }
    replyTarget.value = null
    await load(true)
    // 刚发的回复要能立刻看到，不能被分页或折叠挡住
    if (parentId) {
      const answered = comments.value.find((item) => item.id === parentId)
      const rootId = answered?.parentId ?? parentId
      const thread = threads.value.find((item) => item.root.id === rootId)
      collapsedThreads.value[rootId] = false
      if (thread) visibleReplyCount.value[rootId] = thread.replies.length
    }
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '发表失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: string) {
  if (!confirm('确定删除这条评论？它下面的回复也会一并删除。')) return
  try {
    await deleteComment(id)
    if (replyTarget.value?.id === id) replyTarget.value = null
    await load(true)
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '删除失败')
  }
}

load()
</script>
