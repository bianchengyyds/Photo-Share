<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 普通用户导航栏 -->
    <nav v-if="userStore.isLoggedIn && !userStore.isAdmin && !isSharePage" class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16 gap-3">
          <div class="flex items-center space-x-6 sm:space-x-8 overflow-x-auto min-w-0">
            <router-link to="/" class="text-lg sm:text-xl font-bold text-blue-600 whitespace-nowrap flex-shrink-0">
              照片分享平台
            </router-link>
            <router-link to="/" class="text-gray-700 hover:text-blue-600 transition whitespace-nowrap flex-shrink-0">
              首页
            </router-link>
            <router-link to="/feed" class="text-gray-700 hover:text-blue-600 transition whitespace-nowrap flex-shrink-0">
              动态
            </router-link>
            <router-link to="/search" class="text-gray-700 hover:text-blue-600 transition whitespace-nowrap flex-shrink-0">
              搜索
            </router-link>
            <router-link to="/trash" class="text-gray-700 hover:text-blue-600 transition whitespace-nowrap flex-shrink-0">
              回收站
            </router-link>
          </div>
          <div class="flex items-center gap-2 sm:gap-4 flex-shrink-0">
            <NotificationBell />
            <router-link
              v-if="userId"
              :to="`/user/${userId}`"
              class="text-gray-700 hover:text-blue-600 transition truncate max-w-24"
            >
              {{ userStore.username }}
            </router-link>
            <span v-else class="text-gray-700">{{ userStore.username }}</span>
            <button
              @click="handleLogout"
              class="px-3 sm:px-4 py-2 text-sm text-gray-600 hover:text-gray-800 transition"
            >
              退出登录
            </button>
          </div>
        </div>
      </div>
    </nav>

    <!-- 管理员导航栏 -->
    <nav v-if="userStore.isLoggedIn && userStore.isAdmin" class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center space-x-8">
            <router-link to="/admin" class="text-xl font-bold text-purple-600">
              管理员控制台
            </router-link>
            <router-link to="/admin" class="text-gray-700 hover:text-purple-600 transition">
              控制台
            </router-link>
            <router-link to="/admin/review" class="text-gray-700 hover:text-purple-600 transition">
              照片审核
            </router-link>
          </div>
          <div class="flex items-center space-x-4">
            <span class="text-gray-700">{{ userStore.username }}</span>
            <button
              @click="handleLogout"
              class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800 transition"
            >
              退出登录
            </button>
          </div>
        </div>
      </div>
    </nav>

    <!-- 主内容区 -->
    <main>
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from './stores/user'
import NotificationBell from './components/NotificationBell.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isSharePage = computed(() => route.path.startsWith('/share'))
const userId = computed(() => userStore.user?.id)

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>
