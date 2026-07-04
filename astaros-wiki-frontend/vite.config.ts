import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://8.146.227.206:8801',
        changeOrigin: true,
      },
      '/webjars': {
        target: 'http://8.146.227.206:8801',
        changeOrigin: true,
      },
      '/swagger-ui': {
        target: 'http://8.146.227.206:8801',
        changeOrigin: true,
      },
      '/v3/api-docs': {
        target: 'http://8.146.227.206:8801',
        changeOrigin: true,
      },
      '/doc.html': {
        target: 'http://8.146.227.206:8801',
        changeOrigin: true,
      },
    },
  },
})
