/**
 * EMS 宿主（门户壳 `@beangle/ems-shell`）与子应用（本包）之间的契约常量。
 *
 * 这里只放**双方按约定共享的字面量**：localStorage key、自定义事件名、Wujie 总线事件名、
 * URL 参数名。宿主与子应用各自实现自己的逻辑，不共享代码，但必须用同一套字符串。
 *
 * 约定变更流程：改这里 → 同步 `docs/ems-shell-contract.md` → 确认 `@beangle/ems-shell`
 * （`src/js/storage.ts`、`src/js/tab-keys.ts`、`src/js/theme.ts`）与 `@beangle/bui-vue`
 * （`src/palette.ts`）后一并发布。`tests/contract.spec.ts` 固定这些字面量，防止误改。
 */

/** 与门户壳共享的 localStorage key。 */
export const EMS_STORAGE_KEYS = {
  /** 本应用会话 token（子应用侧）。 */
  authToken: 'beangle.ems.auth_token',
  /** 当前用户信息（子应用侧，JSON）。 */
  userInfo: 'beangle.ems.user_info',
  /** 门户工作台多 Tab 状态（宿主侧维护）。 */
  navTabs: 'beangle.ems.nav_tabs',
  /** 多 Tab 开关偏好（宿主侧维护）。 */
  multiTab: 'beangle.ems.multi_tab',
  /** 门户顶栏吸顶状态（宿主侧维护）。 */
  navStickyHeader: 'beangle.ems.nav_sticky_header',
} as const

/** 门户 UI 偏好的 localStorage key（与 bui-vue / ems-shell 共享）。 */
export const UI_STORAGE_KEYS = {
  locale: 'beangle.ui.locale',
  theme: 'beangle.ui.theme',
  themeMode: 'beangle.ui.theme-mode',
  fontSize: 'beangle.ui.font-size',
} as const

/** 门户 UI 偏好的同页自定义事件（与 bui-vue / ems-shell 共享）。 */
export const UI_STORAGE_EVENTS = {
  localeChange: 'beangle.ui.localechange',
  themeChange: 'beangle.ui.themechange',
  fontSizeChange: 'beangle.ui.fontsizechange',
} as const

/** Wujie 宿主总线事件名（ems-shell `emitWujieBus` 发出）。 */
export const WUJIE_BUS_EVENTS = {
  localeChange: 'locale-change',
  themeModeChange: 'theme-mode-change',
  fontSizeChange: 'font-size-change',
} as const

/** 登录 / 微前端相关的 URL 参数名。 */
export const EMS_URL_PARAMS = {
  /** 跨域会话续期用的一次性会话 id（CAS 回传）。 */
  sid: 'URP_SID',
  /** CAS 登录接口约定的会话参数名。 */
  sidName: 'sid_name',
  /** 本地调试壳层（`?micro=1`）。 */
  micro: 'micro',
  /** CAS 直接回传的登录结果（token/name/code 三件套）。 */
  token: 'token',
  userName: 'name',
  userCode: 'code',
  /** 门户业务场景切换参数。 */
  contextProfileId: 'contextProfileId',
} as const
