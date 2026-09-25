<template>
  <div class="flex items-center justify-center min-h-screen p-4">
    <div class="bg-white rounded-xl p-6 w-full max-w-sm shadow-sm">
      <h2 class="text-lg font-bold mb-2 text-center">此相册需要密码</h2>
      <p class="text-sm text-gray-500 text-center mb-6">请输入访问密码查看照片</p>
      <form @submit.prevent="handleSubmit">
        <input
          v-model="password"
          type="password"
          placeholder="请输入密码"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg mb-3 focus:outline-none focus:ring-2 focus:ring-blue-500"
          autofocus
        />
        <p v-if="error" class="text-red-500 text-sm mb-3">{{ error }}</p>
        <button
          type="submit"
          :disabled="submitting"
          class="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition disabled:opacity-60"
        >
          {{ submitting ? '验证中...' : '查看照片' }}
        </button>
      </form>

      <!-- 登录只用于解锁编辑权限，访问密码对游客与登录成员同样要求 -->
      <div class="mt-4 pt-4 border-t border-gray-100 text-center">
        <p class="text-xs text-gray-400 mb-2">
          <template v-if="userStore.isLoggedIn">
            {{ userStore.user?.nickname || userStore.user?.username }}，已登录 · 输入密码后按这条链接授予的权限协作
          </template>
          <template v-else>
            已有账户？先登录，若这条链接开放了协作即可投稿与编辑
          </template>
        </p>
        <button
          v-if="!userStore.isLoggedIn"
          @click="goLogin"
          class="text-sm text-blue-600 hover:text-blue-700"
        >
          去登录
        </button>
      </div>

      <button
        @click="navigate('/')"
        class="w-full mt-3 px-4 py-2 text-gray-500 hover:bg-gray-100 rounded-lg transition text-sm"
      >
        返回首页
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getShare } from '../services/share'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const code = route.params.code as string

const password = ref('')
const error = ref('')
const submitting = ref(false)

async function handleSubmit() {
  error.value = ''
  submitting.value = true
  try {
    // 密码正确时后端返回 canView=true，验证通过后暂存并进入分享页
    const view = await getShare(code, password.value)
    if (!view.canView) {
      error.value = '访问密码错误'
      return
    }
    sessionStorage.setItem(`share_pwd_${code}`, password.value)
    router.replace(`/share/${code}`)
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '密码错误'
  } finally {
    submitting.value = false
  }
}

function goLogin() {
  router.push({ path: '/login', query: { redirect: `/share/${code}` } })
}

function navigate(path: string) {
  router.push(path)
}
</script>
