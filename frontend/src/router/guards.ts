import type { Router } from 'vue-router'

// 管理员只使用后台，首页与相册、回收站等普通用户页面对管理员不开放
const USER_ONLY_PREFIXES = ['/album', '/trash', '/feed', '/search', '/user', '/notifications']

function readStoredUser() {
  try {
    const raw = sessionStorage.getItem('user')
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function setupRouterGuards(router: Router) {
  router.beforeEach((to, _from, next) => {
    const token = sessionStorage.getItem('token')
    const isAdmin = readStoredUser()?.role === 'ADMIN'

    const isSharePage = to.path.startsWith('/share')
    const isAuthPage = to.path === '/login' || to.path === '/register'
    const requiresAuth = !isSharePage && !isAuthPage
    const isUserOnlyPage =
      to.path === '/' || USER_ONLY_PREFIXES.some((prefix) => to.path.startsWith(prefix))

    if (requiresAuth && !token) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }

    if (isAuthPage && token) {
      next(isAdmin ? '/admin' : '/')
      return
    }

    if (isAdmin && isUserOnlyPage) {
      next('/admin')
      return
    }

    if (to.meta.requiresAdmin && !isAdmin) {
      next(token ? '/' : { path: '/login', query: { redirect: to.fullPath } })
      return
    }

    next()
  })
}
