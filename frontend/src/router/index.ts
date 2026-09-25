import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../pages/HomePage.vue'
import AlbumPage from '../pages/AlbumPage.vue'
import SharePage from '../pages/SharePage.vue'
import PasswordPage from '../pages/PasswordPage.vue'
import TrashPage from '../pages/TrashPage.vue'
import FeedPage from '../pages/FeedPage.vue'
import SearchPage from '../pages/SearchPage.vue'
import UserPage from '../pages/UserPage.vue'
import NotificationPage from '../pages/NotificationPage.vue'
import LoginPage from '../pages/LoginPage.vue'
import RegisterPage from '../pages/RegisterPage.vue'
import AdminHomePage from '../pages/AdminHomePage.vue'
import AdminReviewPage from '../pages/AdminReviewPage.vue'
import { setupRouterGuards } from './guards'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomePage },
    { path: '/album/:id', component: AlbumPage },
    { path: '/share/:code', component: SharePage },
    { path: '/share/:code/password', component: PasswordPage },
    { path: '/trash', component: TrashPage },
    { path: '/feed', component: FeedPage },
    { path: '/search', component: SearchPage },
    { path: '/user/:id', component: UserPage },
    { path: '/notifications', component: NotificationPage },
    { path: '/login', component: LoginPage },
    { path: '/register', component: RegisterPage },
    { path: '/admin', component: AdminHomePage, meta: { requiresAdmin: true } },
    { path: '/admin/review', component: AdminReviewPage, meta: { requiresAdmin: true } },
  ],
})

// 设置路由守卫
setupRouterGuards(router)

export default router
