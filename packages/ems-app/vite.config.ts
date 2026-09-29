import { fileURLToPath, URL } from 'node:url'
import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'
import dts from 'vite-plugin-dts'

export default defineConfig({
  plugins: [
    vue(),
    dts({
      entryRoot: 'src',
      tsconfigPath: './tsconfig.json',
    }),
  ],
  build: {
    lib: {
      entry: {
        // 主入口：端无关（PC / 移动端都能用，只依赖 vue + vue-router）
        index: fileURLToPath(new URL('./src/index.ts', import.meta.url)),
        // PC 壳入口：独立部署时的侧栏/顶栏壳，依赖 tdesign
        pc: fileURLToPath(new URL('./src/pc.ts', import.meta.url)),
      },
      formats: ['es'],
      fileName: (format, entryName) => `${entryName}.js`,
      cssFileName: 'index',
    },
    rollupOptions: {
      external: [
        'vue',
        'vue-router',
        'tdesign-vue-next',
        'tdesign-icons-vue-next',
        /^tdesign-vue-next\//,
        /^tdesign-icons-vue-next\//,
      ],
    },
    outDir: 'dist',
  },
})
