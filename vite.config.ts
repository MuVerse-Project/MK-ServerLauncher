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
      '@': path.resolve(__dirname, './src'),
      '@muapi': path.resolve(__dirname, './src/api'),
      '@assets': path.resolve(__dirname, './src/assets'),
      '@router': path.resolve(__dirname, './src/router'),
      '@internalViews': path.resolve(__dirname, './src/views'),
      '@mucom': path.resolve(__dirname, './src/components/mucom'),
      '@shadcn': path.resolve(__dirname, './src/components/ui'),
    },
  },
  plugins: [
    vue(),
    tailwindcss(),
  ],
})
