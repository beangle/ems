import { EMS_CONTEXT_STORAGE_PREFIX, NAV_MULTI_TAB_STORAGE_KEY, THEME_STORAGE_KEY } from './constants.js';
import type { NavTheme } from './types.js';

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

/** 退出登录：清空当前源下全部 localStorage（含各业务查询条件与缓存） */
export function clearAllLocalStorage(): void {
  if (typeof localStorage === 'undefined') return;
  try {
    localStorage.clear();
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

/** 将五个主题色写入 localStorage（beangle.ui.theme） */
export function saveThemeToLocal(theme: NavTheme): void {
  setLocal(
    THEME_STORAGE_KEY,
    JSON.stringify({
      primaryColor: theme.primaryColor,
      navbarBgColor: theme.navbarBgColor,
      searchBgColor: theme.searchBgColor,
      gridbarBgColor: theme.gridbarBgColor,
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
    gridBorderColor: raw?.gridBorderColor ?? fallback.gridBorderColor,
  };
}
