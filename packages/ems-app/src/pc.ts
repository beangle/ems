/**
 * PC 入口：独立部署（非 Wujie 微前端）时的应用壳层。
 *
 * 依赖 `tdesign-vue-next` / `tdesign-icons-vue-next`（在 package.json 中为 optional peer），
 * 移动端不要引用本入口。
 */
export { default as AppShell } from './components/AppShell.vue'
export type { AppShellMenuItem, AppShellProfile } from './components/app-shell'
export { default as AppShellFullscreenToggle } from './components/AppShellFullscreenToggle.vue'
export { default as AppShellLocaleToggle } from './components/AppShellLocaleToggle.vue'
export { default as AppShellThemeToggle } from './components/AppShellThemeToggle.vue'
