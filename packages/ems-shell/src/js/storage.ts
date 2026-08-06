import {
  BEANGLE_UI_STORAGE_PREFIX,
  EMS_CONTEXT_STORAGE_PREFIX,
  FONT_SIZE_CHANGE_EVENT,
  FONT_SIZE_STORAGE_KEY,
  LOCALE_CHANGE_EVENT,
  LOCALE_STORAGE_KEY,
  NAV_MULTI_TAB_STORAGE_KEY,
  THEME_MODE_CHANGE_EVENT,
  THEME_MODE_STORAGE_KEY,
  THEME_STORAGE_KEY,
} from './constants.js';
import type { NavTheme } from './types.js';
import { emitWujieBus } from './wujie.js';

export type UiThemeMode = 'light' | 'dark';
export type UiLocale = 'zh-CN' | 'en-US';
export type UiFontSize = 'small' | 'medium' | 'large';

const FONT_SIZE_CSS: Record<UiFontSize, string> = {
  small: '0.9286em',
  medium: '1em',
  large: '1.07143em',
};

const THEME_MODE_LIGHT = new Set(['light', 'day', 'default']);
const THEME_MODE_DARK = new Set(['dark', 'night']);
const LOCALE_ENGLISH = new Set(['en', 'en-us', 'en_us', 'en-US', 'en_US']);
const LOCALE_CHINESE = new Set(['zh', 'zh-cn', 'zh_cn', 'zh-CN', 'zh_CN', 'zh-hans', 'zh_hans']);

/** Normalize portal / stored theme tags to light | dark (aligns with @beangle/bui-vue). */
export function normalizeUiThemeMode(raw: unknown): UiThemeMode | null {
  if (raw == null) return null;
  const value = String(raw).trim();
  if (!value) return null;
  const lower = value.toLowerCase();
  if (THEME_MODE_LIGHT.has(value) || THEME_MODE_LIGHT.has(lower)) return 'light';
  if (THEME_MODE_DARK.has(value) || THEME_MODE_DARK.has(lower)) return 'dark';
  return null;
}

/** Normalize portal request_locale (zh_CN / en_US) to zh-CN | en-US. */
export function normalizeUiLocale(raw: unknown): UiLocale | null {
  if (raw == null) return null;
  const value = String(raw).trim();
  if (!value) return null;
  const lower = value.toLowerCase().replace(/_/g, '-');
  if (LOCALE_ENGLISH.has(value) || LOCALE_ENGLISH.has(lower) || lower.startsWith('en')) return 'en-US';
  if (LOCALE_CHINESE.has(value) || LOCALE_CHINESE.has(lower) || lower.startsWith('zh')) return 'zh-CN';
  return null;
}

export function getStoredThemeMode(): UiThemeMode | null {
  try {
    if (typeof localStorage === 'undefined') return null;
    return normalizeUiThemeMode(localStorage.getItem(THEME_MODE_STORAGE_KEY));
  } catch {
    return null;
  }
}

export function setStoredThemeMode(theme: UiThemeMode | string): UiThemeMode | null {
  const normalized = normalizeUiThemeMode(theme);
  if (!normalized) return null;
  try {
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(THEME_MODE_STORAGE_KEY, normalized);
    }
  } catch {
    /* ignore */
  }
  return normalized;
}

/**
 * Persist beangle.ui.theme-mode (light|dark only; not color JSON beangle.ui.theme),
 * dispatch beangle.ui.themechange (bui-vue THEME_CHANGE_EVENT), emit wujie bus
 * theme-mode-change. Skips notify when value unchanged.
 */
export function setStoredThemeModeAndNotify(theme: UiThemeMode | string): UiThemeMode | null {
  const normalized = normalizeUiThemeMode(theme);
  if (!normalized) return null;
  const prev = getStoredThemeMode();
  setStoredThemeMode(normalized);
  if (prev === normalized) return normalized;
  if (typeof window !== 'undefined') {
    try {
      window.dispatchEvent(new CustomEvent(THEME_MODE_CHANGE_EVENT, { detail: normalized }));
    } catch {
      /* ignore */
    }
  }
  emitWujieBus('theme-mode-change', normalized);
  return normalized;
}

export function getStoredLocale(): UiLocale | null {
  try {
    if (typeof localStorage === 'undefined') return null;
    return normalizeUiLocale(localStorage.getItem(LOCALE_STORAGE_KEY));
  } catch {
    return null;
  }
}

export function setStoredLocale(locale: UiLocale | string): UiLocale | null {
  const normalized = normalizeUiLocale(locale);
  if (!normalized) return null;
  try {
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(LOCALE_STORAGE_KEY, normalized);
      // request_locale is a backend query param only — never persist locally.
      localStorage.removeItem('request_locale');
    }
  } catch {
    /* ignore */
  }
  return normalized;
}

/**
 * Persist beangle.ui.locale, dispatch beangle.ui.localechange, emit wujie bus
 * locale-change. Skips notify when value unchanged.
 */
export function setStoredLocaleAndNotify(locale: UiLocale | string): UiLocale | null {
  const normalized = normalizeUiLocale(locale);
  if (!normalized) return null;
  const prev = getStoredLocale();
  setStoredLocale(normalized);
  if (prev === normalized) return normalized;
  if (typeof window !== 'undefined') {
    try {
      window.dispatchEvent(new CustomEvent(LOCALE_CHANGE_EVENT, { detail: normalized }));
    } catch {
      /* ignore */
    }
  }
  emitWujieBus('locale-change', normalized);
  return normalized;
}

/** Map semantic font size to portal root CSS font-size. */
export function fontSizeToCss(size: UiFontSize): string {
  return FONT_SIZE_CSS[size];
}

/** Map portal radio CSS / aliases to small|medium|large. */
export function cssToFontSize(css: unknown): UiFontSize | null {
  if (css == null) return null;
  const value = String(css).trim().toLowerCase();
  if (!value) return null;
  if (value === FONT_SIZE_CSS.small || value === '0.9286em') return 'small';
  if (value === FONT_SIZE_CSS.medium || value === '1em' || value === '1.0em') return 'medium';
  if (value === FONT_SIZE_CSS.large || value === '1.07143em') return 'large';
  return null;
}

/** Normalize portal / stored font-size tags to small|medium|large. */
export function normalizeUiFontSize(raw: unknown): UiFontSize | null {
  if (raw == null) return null;
  const value = String(raw).trim();
  if (!value) return null;
  const lower = value.toLowerCase();
  if (lower === 'small' || lower === 'sm' || lower === 's') return 'small';
  if (lower === 'medium' || lower === 'middle' || lower === 'md' || lower === 'm' || lower === 'default') {
    return 'medium';
  }
  if (lower === 'large' || lower === 'lg' || lower === 'l') return 'large';
  return cssToFontSize(value);
}

/** Read beangle.ui.font-size (small|medium|large). */
export function getStoredFontSize(): UiFontSize | null {
  try {
    if (typeof localStorage === 'undefined') return null;
    return normalizeUiFontSize(localStorage.getItem(FONT_SIZE_STORAGE_KEY));
  } catch {
    return null;
  }
}

export function setStoredFontSize(size: UiFontSize | string): UiFontSize | null {
  const normalized = normalizeUiFontSize(size);
  if (!normalized) return null;
  try {
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(FONT_SIZE_STORAGE_KEY, normalized);
    }
  } catch {
    /* ignore */
  }
  return normalized;
}

/**
 * Persist beangle.ui.font-size, dispatch beangle.ui.fontsizechange, emit wujie bus
 * font-size-change. Skips notify when value unchanged.
 */
export function setStoredFontSizeAndNotify(size: UiFontSize | string): UiFontSize | null {
  const normalized = normalizeUiFontSize(size);
  if (!normalized) return null;
  const prev = getStoredFontSize();
  setStoredFontSize(normalized);
  if (prev === normalized) return normalized;
  if (typeof window !== 'undefined') {
    try {
      window.dispatchEvent(new CustomEvent(FONT_SIZE_CHANGE_EVENT, { detail: normalized }));
    } catch {
      /* ignore */
    }
  }
  emitWujieBus('font-size-change', normalized);
  return normalized;
}

/** 切换 profile：清除 localStorage 中以 beangle.ems.context. 开头的业务缓存 */
export function clearContextLocalStorage(): void {
  if (typeof localStorage === 'undefined') return;
  try {
    const keys: string[] = [];
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key != null && key.startsWith(EMS_CONTEXT_STORAGE_PREFIX)) {
        keys.push(key);
      }
    }
    for (const key of keys) {
      localStorage.removeItem(key);
    }
  } catch {
    /* ignore */
  }
}

/**
 * 退出登录：清除同源 localStorage，但保留 `beangle.ui.*`
 *（locale / theme-mode / font-size / theme 色板等跨会话 UI 偏好）。
 */
export function clearAllLocalStorage(): void {
  if (typeof localStorage === 'undefined') return;
  try {
    const keys: string[] = [];
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key != null && !key.startsWith(BEANGLE_UI_STORAGE_PREFIX)) {
        keys.push(key);
      }
    }
    for (const key of keys) {
      localStorage.removeItem(key);
    }
  } catch {
    /* ignore */
  }
}

export function setLocal(name: string, value: string | null | undefined): void {
  if (typeof localStorage === 'undefined') return;
  if (value) {
    localStorage.setItem(name, value);
  } else {
    localStorage.removeItem(name);
  }
}

export function getLocal(name: string, defaultValue: string): string;
export function getLocal<T>(name: string, defaultValue: T): string | T;
export function getLocal<T>(name: string, defaultValue: T): string | T {
  if (typeof localStorage === 'undefined') {
    return defaultValue;
  }
  return localStorage.getItem(name) ?? defaultValue;
}

/** 读本地 multiTab 偏好；无记录时默认 true */
export function getMultiTabPreference(): boolean {
  const v = getLocal(NAV_MULTI_TAB_STORAGE_KEY, '1');
  return v !== '0' && v !== 'false';
}

export function setMultiTabPreference(enabled: boolean): void {
  setLocal(NAV_MULTI_TAB_STORAGE_KEY, enabled ? '1' : '0');
}

/** 合并服务端 params.multiTab 与 localStorage */
export function resolveMultiTabParam(explicit: unknown): string {
  if (explicit !== undefined && explicit !== null && String(explicit).trim() !== '') {
    const s = String(explicit).trim().toLowerCase();
    if (s === 'false' || s === '0') return 'false';
    if (s === 'true' || s === '1') return 'true';
    return String(explicit);
  }
  return getMultiTabPreference() ? 'true' : 'false';
}

/** 从 localStorage（beangle.ui.theme）读取主题色；无效或缺失时用 fallback */
export function loadThemeFromLocal(fallback: NavTheme): NavTheme {
  const raw = getLocal(THEME_STORAGE_KEY, '');
  if (!raw) return fallback;
  try {
    return normalizeNavTheme(JSON.parse(raw) as Partial<NavTheme>, fallback);
  } catch {
    return fallback;
  }
}

/** 将主题色写入 localStorage（beangle.ui.theme） */
export function saveThemeToLocal(theme: NavTheme): void {
  setLocal(
    THEME_STORAGE_KEY,
    JSON.stringify({
      primaryColor: theme.primaryColor,
      navbarBgColor: theme.navbarBgColor,
      searchBgColor: theme.searchBgColor,
      gridbarBgColor: theme.gridbarBgColor,
      gridHeaderBgColor: theme.gridHeaderBgColor,
      gridBorderColor: theme.gridBorderColor,
    })
  );
}

export function clearThemeFromLocal(): void {
  setLocal(THEME_STORAGE_KEY, null);
}

function normalizeNavTheme(raw: Partial<NavTheme> | null | undefined, fallback: NavTheme): NavTheme {
  return {
    primaryColor: raw?.primaryColor ?? fallback.primaryColor,
    navbarBgColor: raw?.navbarBgColor ?? fallback.navbarBgColor,
    searchBgColor: raw?.searchBgColor ?? fallback.searchBgColor,
    gridbarBgColor: raw?.gridbarBgColor ?? fallback.gridbarBgColor,
    gridHeaderBgColor: raw?.gridHeaderBgColor ?? fallback.gridHeaderBgColor,
    gridBorderColor: raw?.gridBorderColor ?? fallback.gridBorderColor,
  };
}
