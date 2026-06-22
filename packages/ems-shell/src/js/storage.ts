import { NAV_MULTI_TAB_STORAGE_KEY } from './constants.js';

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
