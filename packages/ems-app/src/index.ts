/**
 * 主入口：端无关的 EMS 应用接入层（PC 与移动端都可引用）。
 *
 * 只依赖 `vue`（会话状态与权限用 ref/computed）与 `vue-router`（MicroShell）。
 * 需要 tdesign 的 PC 独立壳请从 `@beangle/ems-app/pc` 导入。
 */
export * from './contract'

export * from './api/auth'
export * from './api/permissions'
export * from './api/runtimeConfig'
export * from './api/request'
export * from './api/jsonapi'
export * from './api/qrLogin'

export * from './utils/microApp'
export * from './utils/portalUi'

export { useQrLogin } from './composables/useQrLogin'
export type { UseQrLogin, UseQrLoginOptions } from './composables/useQrLogin'

export { default as MicroShell } from './components/MicroShell.vue'
