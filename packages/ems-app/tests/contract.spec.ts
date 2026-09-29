import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  EMS_STORAGE_KEYS,
  EMS_URL_PARAMS,
  UI_STORAGE_EVENTS,
  UI_STORAGE_KEYS,
  WUJIE_BUS_EVENTS,
  bindPortalUiSync,
  createEmsAuth,
} from '../src/index'

/**
 * 契约冻结测试：这些字面量同时被门户壳 `@beangle/ems-shell`、组件层 `@beangle/bui-vue`
 * 使用。改动它们会静默破坏登录态、多 Tab、语言/主题同步，所以在这里写死期望值。
 */
describe('EMS 宿主机契约常量', () => {
  it('localStorage key 与 ems-shell / bui-vue 一致', () => {
    expect(EMS_STORAGE_KEYS.authToken).toBe('beangle.ems.auth_token')
    expect(EMS_STORAGE_KEYS.userInfo).toBe('beangle.ems.user_info')
    expect(EMS_STORAGE_KEYS.navTabs).toBe('beangle.ems.nav_tabs')
    expect(EMS_STORAGE_KEYS.multiTab).toBe('beangle.ems.multi_tab')
    expect(EMS_STORAGE_KEYS.navStickyHeader).toBe('beangle.ems.nav_sticky_header')

    expect(UI_STORAGE_KEYS.locale).toBe('beangle.ui.locale')
    expect(UI_STORAGE_KEYS.theme).toBe('beangle.ui.theme')
    expect(UI_STORAGE_KEYS.themeMode).toBe('beangle.ui.theme-mode')
    expect(UI_STORAGE_KEYS.fontSize).toBe('beangle.ui.font-size')
  })

  it('自定义事件名与 URL 参数名与 ems-shell 一致', () => {
    expect(UI_STORAGE_EVENTS.localeChange).toBe('beangle.ui.localechange')
    expect(UI_STORAGE_EVENTS.themeChange).toBe('beangle.ui.themechange')
    expect(UI_STORAGE_EVENTS.fontSizeChange).toBe('beangle.ui.fontsizechange')

    expect(WUJIE_BUS_EVENTS.localeChange).toBe('locale-change')
    expect(WUJIE_BUS_EVENTS.themeModeChange).toBe('theme-mode-change')
    expect(WUJIE_BUS_EVENTS.fontSizeChange).toBe('font-size-change')

    expect(EMS_URL_PARAMS.sid).toBe('URP_SID')
    expect(EMS_URL_PARAMS.sidName).toBe('sid_name')
    expect(EMS_URL_PARAMS.micro).toBe('micro')
  })
})

describe('token 管理使用契约中的存储 key', () => {
  afterEach(() => localStorage.clear())

  const auth = createEmsAuth({
    appBasePath: '/edu/learning/',
    casAuthLoginUrl: '/cas/login.json',
    casLoginUrl: '/cas/login',
    casLogoutUrl: '/cas/logout',
  })

  it('setToken / setUserInfo 写入 ems-shell 约定的 key', () => {
    auth.sessionService.setToken('t-1')
    auth.sessionService.setUserInfo({ name: '张三', code: '2024001' })

    expect(localStorage.getItem('beangle.ems.auth_token')).toBe('t-1')
    expect(JSON.parse(localStorage.getItem('beangle.ems.user_info') as string)).toEqual({
      name: '张三',
      code: '2024001',
    })
    expect(auth.sessionService.getToken()).toBe('t-1')
    expect(auth.sessionService.getUserInfo()?.code).toBe('2024001')
  })

  it('clearAuth 清理 token 与用户信息', () => {
    auth.sessionService.setToken('t-2')
    auth.sessionService.setUserInfo({ name: '李四', code: '2024002' })
    auth.sessionService.clearAuth()

    expect(localStorage.getItem('beangle.ems.auth_token')).toBeNull()
    expect(localStorage.getItem('beangle.ems.user_info')).toBeNull()
  })
})

describe('bindPortalUiSync 监听门户下发的 UI 偏好', () => {
  afterEach(() => {
    delete (window as { $wujie?: unknown }).$wujie
    localStorage.clear()
  })

  it('响应 Wujie 总线事件名 locale-change / theme-mode-change / font-size-change', () => {
    const handlers = new Map<string, (...args: unknown[]) => void>()
    ;(window as unknown as { $wujie: unknown }).$wujie = {
      bus: {
        $on: (event: string, handler: (...args: unknown[]) => void) => handlers.set(event, handler),
        $off: () => {},
      },
    }

    const onLocale = vi.fn()
    const onThemeMode = vi.fn()
    const onFontSize = vi.fn()
    bindPortalUiSync({ onLocale, onThemeMode, onFontSize, applyInitial: false })

    handlers.get('locale-change')?.('en-US')
    handlers.get('theme-mode-change')?.('dark')
    handlers.get('font-size-change')?.('large')

    expect(onLocale).toHaveBeenCalledWith('en-US')
    expect(onThemeMode).toHaveBeenCalledWith('dark')
    expect(onFontSize).toHaveBeenCalledWith('large')
  })

  it('响应同页自定义事件与 storage 事件（beangle.ui.*）', () => {
    const onLocale = vi.fn()
    bindPortalUiSync({ onLocale, applyInitial: false })

    window.dispatchEvent(new CustomEvent('beangle.ui.localechange', { detail: 'en-US' }))
    expect(onLocale).toHaveBeenCalledWith('en-US')

    onLocale.mockClear()
    window.dispatchEvent(new StorageEvent('storage', {
      key: 'beangle.ui.locale',
      newValue: 'zh-CN',
      storageArea: window.localStorage,
    }))
    expect(onLocale).toHaveBeenCalledWith('zh-CN')
  })
})
