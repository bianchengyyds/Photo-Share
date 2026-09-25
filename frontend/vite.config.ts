// Vue项目的配置信息, 如: 端口号等

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 后端接口地址
        changeOrigin: true, // 改变源, 解决跨域问题
      },
      '/uploads': {
        target: 'http://localhost:8080', // 待审核照片的本地静态资源
        changeOrigin: true,
      },
    },
  },
})
