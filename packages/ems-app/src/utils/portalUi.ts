/**
 * Wujie portal locale / theme-mode / font-size sync for micro-apps.
 *
 * Listens for portal bus events (`locale-change`, `theme-mode-change`,
 * `font-size-change`) and for shared storage/custom events
 * (`beangle.ui.locale` / `beangle.ui.theme-mode` / `beangle.ui.font-size`,
 * same keys/events as `@beangle/bui-vue` / ems-shell). Does not persist —
 * apps or the portal write storage; this module only notifies.
 */

import { UI_STORAGE_EVENTS, UI_STORAGE_KEYS, WUJIE_BUS_EVENTS } from '../contract'

export type PortalLocale = 'zh-CN' | 'en-US'
export type PortalThemeMode = 'light' | 'dark'
export type PortalFontSize = 'small' | 'medium' | 'large'

export type BindPortalUiSyncOptions = {
  onLocale?: (locale: PortalLocale) => void
  onThemeMode?: (mode: PortalThemeMode) => void
  onFontSize?: (size: PortalFontSize) => void
  /** If true, call callbacks once with detected initial values. Default true. */
  applyInitial?: boolean
}

const LOCALE_STORAGE_KEY = UI_STORAGE_KEYS.locale
const THEME_STORAGE_KEY = UI_STORAGE_KEYS.themeMode
const FONT_SIZE_STORAGE_KEY = UI_STORAGE_KEYS.fontSize
const LOCALE_CHANGE_EVENT = UI_STORAGE_EVENTS.localeChange
const THEME_CHANGE_EVENT = UI_STORAGE_EVENTS.themeChange
const FONT_SIZE_CHANGE_EVENT = UI_STORAGE_EVENTS.fontSizeChange

const ENGLISH = new Set(['en', 'en-us', 'en_us', 'en-US', 'en_US'])
const CHINESE = new Set(['zh', 'zh-cn', 'zh_cn', 'zh-CN', 'zh_CN', 'zh-hans', 'zh_hans'])
const LIGHT = new Set(['light', 'day', 'default'])
const DARK = new Set(['dark', 'night'])

const FONT_SIZE_CSS: Record<PortalFontSize, string> = {
  small: '0.9286em',
  medium: '1em',
  large: '1.07143em',
}

type WujieBus = {
  $on?: (event: string, handler: (...args: unknown[]) => void) => void
  $off?: (event: string, handler: (...args: unknown[]) => void) => void
}

type WujieHost = {
  props?: Record<string, unknown>
  bus?: WujieBus
}

declare global {
  interface Window {
    $wujie?: WujieHost
  }
}

/** Normalize portal / browser locale tags to zh-CN | en-US. */
export function normalizePortalLocale(raw: unknown): PortalLocale | null {
  if (raw == null) return null
  const value = String(raw).trim()
  if (!value) return null
  const lower = value.toLowerCase().replace(/_/g, '-')
  if (ENGLISH.has(value) || ENGLISH.has(lower) || lower.startsWith('en')) return 'en-US'
  if (CHINESE.has(value) || CHINESE.has(lower) || lower.startsWith('zh')) return 'zh-CN'
  return null
}

/** Normalize portal / stored theme tags to light | dark. */
export function normalizePortalThemeMode(raw: unknown): PortalThemeMode | null {
  if (raw == null) return null
  const value = String(raw).trim()
  if (!value) return null
  const lower = value.toLowerCase()
  if (LIGHT.has(value) || LIGHT.has(lower)) return 'light'
  if (DARK.has(value) || DARK.has(lower)) return 'dark'
  return null
}

/** Map semantic font size to portal root CSS font-size. */
export function portalFontSizeToCss(size: PortalFontSize): string {
  return FONT_SIZE_CSS[size]
}

/** Normalize portal / stored font-size tags to small|medium|large. */
export function normalizePortalFontSize(raw: unknown): PortalFontSize | null {
  if (raw == null) return null
  const value = String(raw).trim()
  if (!value) return null
  const lower = value.toLowerCase()
  if (lower === 'small' || lower === 'sm' || lower === 's') return 'small'
  if (lower === 'medium' || lower === 'middle' || lower === 'md' || lower === 'm' || lower === 'default') {
    return 'medium'
  }
  if (lower === 'large' || lower === 'lg' || lower === 'l') return 'large'
  if (lower === FONT_SIZE_CSS.small || lower === '0.9286em') return 'small'
  if (lower === FONT_SIZE_CSS.medium || lower === '1em' || lower === '1.0em') return 'medium'
  if (lower === FONT_SIZE_CSS.large || lower === '1.07143em') return 'large'
  return null
}

function readStoredLocale(): PortalLocale | null {
  try {
    if (typeof localStorage === 'undefined') return null
    return normalizePortalLocale(localStorage.getItem(LOCALE_STORAGE_KEY))
  } catch {
    return null
  }
}

function readStoredThemeMode(): PortalThemeMode | null {
  try {
    if (typeof localStorage === 'undefined') return null
    return normalizePortalThemeMode(localStorage.getItem(THEME_STORAGE_KEY))
  } catch {
    return null
  }
}

function readStoredFontSize(): PortalFontSize | null {
  try {
    if (typeof localStorage === 'undefined') return null
    return normalizePortalFontSize(localStorage.getItem(FONT_SIZE_STORAGE_KEY))
  } catch {
    return null
  }
}

function readDocumentThemeMode(): PortalThemeMode | null {
  if (typeof document === 'undefined') return null
  return normalizePortalThemeMode(document.documentElement.getAttribute('theme-mode'))
}

function readWujieProps(): Record<string, unknown> | undefined {
  if (typeof window === 'undefined') return undefined
  return window.$wujie?.props
}

/** Resolve initial locale: wujie props → URL → localStorage → zh-CN. */
export function detectPortalLocale(): PortalLocale {
  if (typeof window === 'undefined') return 'zh-CN'
  const props = readWujieProps()
  const params = new URLSearchParams(window.location.search)
  return (
    normalizePortalLocale(props?.locale ?? props?.lang) ||
    normalizePortalLocale(params.get('locale') || params.get('lang')) ||
    readStoredLocale() ||
    'zh-CN'
  )
}

/** Resolve initial theme: wujie props → localStorage → html attribute → light. */
export function detectPortalThemeMode(): PortalThemeMode {
  if (typeof window === 'undefined') return 'light'
  const props = readWujieProps()
  return (
    normalizePortalThemeMode(props?.themeMode) ||
    readStoredThemeMode() ||
    readDocumentThemeMode() ||
    'light'
  )
}

/** Resolve initial font size: wujie props → localStorage → medium. */
export function detectPortalFontSize(): PortalFontSize {
  if (typeof window === 'undefined') return 'medium'
  const props = readWujieProps()
  return normalizePortalFontSize(props?.fontSize) || readStoredFontSize() || 'medium'
}

/**
 * Bind portal bus + shared storage listeners for locale, theme-mode, and font-size.
 * Returns unsubscribe (bus `$off` + removeEventListener).
 */
export function bindPortalUiSync(options: BindPortalUiSyncOptions): () => void {
  if (typeof window === 'undefined') return () => {}

  const { onLocale, onThemeMode, onFontSize, applyInitial = true } = options
  const cleanups: Array<() => void> = []

  const bus = window.$wujie?.bus

  if (onLocale && bus?.$on) {
    const onBus = (...args: unknown[]) => {
      const next = normalizePortalLocale(args[0])
      if (next) onLocale(next)
    }
    bus.$on(WUJIE_BUS_EVENTS.localeChange, onBus)
    cleanups.push(() => bus.$off?.(WUJIE_BUS_EVENTS.localeChange, onBus))
  }

  if (onThemeMode && bus?.$on) {
    const onBus = (...args: unknown[]) => {
      const next = normalizePortalThemeMode(args[0])
      if (next) onThemeMode(next)
    }
    bus.$on(WUJIE_BUS_EVENTS.themeModeChange, onBus)
    cleanups.push(() => bus.$off?.(WUJIE_BUS_EVENTS.themeModeChange, onBus))
  }

  if (onFontSize && bus?.$on) {
    const onBus = (...args: unknown[]) => {
      const next = normalizePortalFontSize(args[0])
      if (next) onFontSize(next)
    }
    bus.$on(WUJIE_BUS_EVENTS.fontSizeChange, onBus)
    cleanups.push(() => bus.$off?.(WUJIE_BUS_EVENTS.fontSizeChange, onBus))
  }

  if (onLocale) {
    const onCustom = (event: Event) => {
      const next = normalizePortalLocale((event as CustomEvent<unknown>).detail)
      if (next) onLocale(next)
    }
    const onStorage = (event: StorageEvent) => {
      if (event.key !== LOCALE_STORAGE_KEY || event.storageArea !== localStorage) return
      const next = normalizePortalLocale(event.newValue)
      if (next) onLocale(next)
    }
    window.addEventListener(LOCALE_CHANGE_EVENT, onCustom)
    window.addEventListener('storage', onStorage)
    cleanups.push(() => {
      window.removeEventListener(LOCALE_CHANGE_EVENT, onCustom)
      window.removeEventListener('storage', onStorage)
    })
  }

  if (onThemeMode) {
    const onCustom = (event: Event) => {
      const next = normalizePortalThemeMode((event as CustomEvent<unknown>).detail)
      if (next) onThemeMode(next)
    }
    const onStorage = (event: StorageEvent) => {
      if (event.key !== THEME_STORAGE_KEY || event.storageArea !== localStorage) return
      const next = normalizePortalThemeMode(event.newValue)
      if (next) onThemeMode(next)
    }
    window.addEventListener(THEME_CHANGE_EVENT, onCustom)
    window.addEventListener('storage', onStorage)
    cleanups.push(() => {
      window.removeEventListener(THEME_CHANGE_EVENT, onCustom)
      window.removeEventListener('storage', onStorage)
    })
  }

  if (onFontSize) {
    const onCustom = (event: Event) => {
      const next = normalizePortalFontSize((event as CustomEvent<unknown>).detail)
      if (next) onFontSize(next)
    }
    const onStorage = (event: StorageEvent) => {
      if (event.key !== FONT_SIZE_STORAGE_KEY || event.storageArea !== localStorage) return
      const next = normalizePortalFontSize(event.newValue)
      if (next) onFontSize(next)
    }
    window.addEventListener(FONT_SIZE_CHANGE_EVENT, onCustom)
    window.addEventListener('storage', onStorage)
    cleanups.push(() => {
      window.removeEventListener(FONT_SIZE_CHANGE_EVENT, onCustom)
      window.removeEventListener('storage', onStorage)
    })
  }

  if (applyInitial) {
    if (onLocale) onLocale(detectPortalLocale())
    if (onThemeMode) onThemeMode(detectPortalThemeMode())
    if (onFontSize) onFontSize(detectPortalFontSize())
  }

  return () => {
    for (const cleanup of cleanups) cleanup()
  }
}
