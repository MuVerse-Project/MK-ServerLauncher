import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import * as path from "node:path";

// https://vite.dev/config/
export default defineConfig({
  build: {
    outDir: '../src/main/resources/muview',
    emptyOutDir: true,
  },
  resolve: {
    alias: {
      '@': path.resolve(import.meta.dirname, './src'),
      '@muapi': path.resolve(import.meta.dirname, './src/api'),
      '@assets': path.resolve(import.meta.dirname, './src/assets'),
      '@router': path.resolve(import.meta.dirname, './src/router'),
      '@muview': path.resolve(import.meta.dirname, './src/views'),
      '@mucom': path.resolve(import.meta.dirname, './src/components/mucom'),
      '@shadcn': path.resolve(import.meta.dirname, './src/components/ui'),
      '@com': path.resolve(import.meta.dirname, './src/custom/components'),
      '@view': path.resolve(import.meta.dirname, './src/custom/views'),
    },
  },
  plugins: [
    vue(),
    tailwindcss(),
  ],
  server: {
    // 开发模式下把后端接口代理到本地 MuView,前端始终用同源相对路径
    proxy: {
      '/api': 'http://127.0.0.1:20038',
      '/muclose': 'http://127.0.0.1:20038',
    },
  },
})
