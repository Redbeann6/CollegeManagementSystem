import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      // 处理所有API路径，移除/api前缀
      '^/api/(.*)': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      // 处理头像路径
      '^/avatar/(.*)': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})