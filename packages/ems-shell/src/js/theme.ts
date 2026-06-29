import { THEME_STORAGE_KEY } from './constants.js';
import { loadThemeFromLocal } from './storage.js';
import type { NavTheme } from './types.js';

const EMPTY_THEME: NavTheme = {
  primaryColor: '',
  navbarBgColor: '',
  searchBgColor: '',
  gridbarBgColor: '',
  gridBorderColor: '',
};

/** 将主题色写入 :root CSS 变量（不依赖 jQuery / 侧栏色板 DOM） */
export function applyThemeVars(theme: NavTheme): void {
  const r = document.documentElement;
  if (theme.primaryColor) {
    r.style.setProperty('--primary-color', theme.primaryColor);
    r.style.setProperty(
      '--ems-tab-active-tint',
      'color-mix(in srgb, ' + theme.primaryColor + ' 12%, #ffffff)'
    );
  }
  if (theme.navbarBgColor) r.style.setProperty('--navbar-bg-color', theme.navbarBgColor);
  if (theme.searchBgColor) r.style.setProperty('--search-bg-color', theme.searchBgColor);
  if (theme.gridbarBgColor) r.style.setProperty('--gridbar-bg-color', theme.gridbarBgColor);
  if (theme.gridBorderColor) r.style.setProperty('--grid-border-color', theme.gridBorderColor);
}

/** 若 localStorage（beangle.ui.theme）有记录则应用；返回是否已应用 */
export function applyStoredThemeIfPresent(fallback?: NavTheme): boolean {
  if (typeof localStorage === 'undefined') return false;
  if (!localStorage.getItem(THEME_STORAGE_KEY)) return false;
  applyThemeVars(loadThemeFromLocal(fallback ?? EMPTY_THEME));
  return true;
}
