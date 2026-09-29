import { fileURLToPath, URL } from 'node:url'
import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vitest/config'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: { '@beangle/ems-app': fileURLToPath(new URL('./src/index.ts', import.meta.url)) },
  },
  test: {
    environment: 'jsdom',
    include: ['tests/**/*.spec.ts'],
  },
})
