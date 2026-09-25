import axios from 'axios'
import type { Result } from '../types'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

// 请求拦截器：添加 token
api.interceptors.request.use(
  (config) => {
    const token = sessionStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    const res = response.data as Result<unknown>
    if (res.code !== 200) {
      return Promise.reject(new Error(res.message))
    }
    return response
  },
  (error) => {
    // 401 未授权：token 过期或无效
    if (error.response?.status === 401) {
      // 延迟导入，避免循环依赖
      import('../stores/user').then(({ useUserStore }) => {
        const userStore = useUserStore()
        userStore.logout()
      })
      import('../router').then(({ default: router }) => {
        router.push('/login')
      })
      return Promise.reject(new Error('登录已过期，请重新登录'))
    }

    const message = error.response?.data?.message || error.message || '请求失败'
    return Promise.reject(new Error(message))
  }
)

export default api
