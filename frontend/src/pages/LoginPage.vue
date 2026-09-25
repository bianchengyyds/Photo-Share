<template>
  <div
    :class="[
      'min-h-screen flex items-center justify-center transition-colors',
      isAdminMode
        ? 'bg-gradient-to-br from-purple-50 to-indigo-100'
        : 'bg-gradient-to-br from-blue-50 to-indigo-100'
    ]"
  >
    <div class="bg-white p-8 rounded-2xl shadow-xl w-full max-w-md">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-gray-800">
          {{ isAdminMode ? '管理员登录' : '用户登录' }}
        </h1>
        <p class="text-gray-500 mt-2">
          {{ isAdminMode ? '请使用管理员账号登录控制台' : '欢迎回来，请登录您的账号' }}
        </p>
      </div>

      <!-- 角色选择 -->
      <div class="grid grid-cols-2 gap-3 mb-8">
        <button
          type="button"
          @click="switchRole('USER')"
          :class="[
            'flex flex-col items-center py-3 rounded-xl border-2 transition',
            !isAdminMode
              ? 'border-blue-500 bg-blue-50 text-blue-700'
              : 'border-gray-200 bg-white text-gray-500 hover:border-gray-300'
          ]"
        >
          <svg class="w-6 h-6 mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
          </svg>
          <span class="text-sm font-medium">普通用户</span>
        </button>

        <button
          type="button"
          @click="switchRole('ADMIN')"
          :class="[
            'flex flex-col items-center py-3 rounded-xl border-2 transition',
            isAdminMode
              ? 'border-purple-500 bg-purple-50 text-purple-700'
              : 'border-gray-200 bg-white text-gray-500 hover:border-gray-300'
          ]"
        >
          <svg class="w-6 h-6 mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
          </svg>
          <span class="text-sm font-medium">管理员</span>
        </button>
      </div>

      <form @submit.prevent="handleLogin" class="space-y-6">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">用户名</label>
          <input
            v-model="form.username"
            type="text"
            required
            :class="[
              'w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:border-transparent transition',
              isAdminMode ? 'focus:ring-purple-500' : 'focus:ring-blue-500'
            ]"
            :placeholder="isAdminMode ? '请输入管理员用户名' : '请输入用户名'"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">密码</label>
          <input
            v-model="form.password"
            type="password"
            required
            :class="[
              'w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:border-transparent transition',
              isAdminMode ? 'focus:ring-purple-500' : 'focus:ring-blue-500'
            ]"
            placeholder="请输入密码"
          />
        </div>

        <p v-if="errorMessage" class="text-sm text-red-600 bg-red-50 px-3 py-2 rounded-lg">
          {{ errorMessage }}
        </p>

        <button
          type="submit"
          :disabled="loading"
          :class="[
            'w-full py-3 rounded-lg font-medium text-white transition disabled:opacity-50 disabled:cursor-not-allowed',
            isAdminMode ? 'bg-purple-600 hover:bg-purple-700' : 'bg-blue-600 hover:bg-blue-700'
          ]"
        >
          {{ loading ? '登录中...' : isAdminMode ? '进入管理控制台' : '登录' }}
        </button>
      </form>

      <div v-if="!isAdminMode" class="mt-6 text-center">
        <p class="text-gray-600">
          还没有账号？
          <router-link to="/register" class="text-blue-600 hover:text-blue-700 font-medium">
            立即注册
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  username: '',
  password: ''
})

const loading = ref(false)
const selectedRole = ref<'USER' | 'ADMIN'>('USER')
const errorMessage = ref('')

const isAdminMode = computed(() => selectedRole.value === 'ADMIN')

function switchRole(role: 'USER' | 'ADMIN') {
  selectedRole.value = role
  errorMessage.value = ''
}

async function handleLogin() {
  errorMessage.value = ''
  loading.value = true
  try {
    await userStore.login(form.username, form.password)

    // 校验登录身份与所选角色是否一致
    if (isAdminMode.value && !userStore.isAdmin) {
      userStore.logout()
      errorMessage.value = '该账号不是管理员账号，请切换到"普通用户"登录'
      return
    }
    if (!isAdminMode.value && userStore.isAdmin) {
      userStore.logout()
      errorMessage.value = '该账号是管理员账号，请切换到"管理员"登录'
      return
    }

    // 从分享页等页面跳来时，登录后回到原页面
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    const safeRedirect = redirect.startsWith('/') && !redirect.startsWith('//') ? redirect : ''

    // 管理员不使用普通用户页面，只回分享页或管理后台
    if (userStore.isAdmin) {
      router.push(safeRedirect.startsWith('/share') ? safeRedirect : '/admin')
    } else {
      router.push(safeRedirect || '/')
    }
  } catch (error: any) {
    errorMessage.value = error.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>
