/**
 * 工作台标签相关的纯函数（与 Nav 实例无关，便于单测）。
 * Nav 侧通过 workspace.ts 中的薄封装调用本模块。
 */
import type { NavOpenMode } from './types.js';
import {
  NAV_OPENMODE_AJAX,
  NAV_OPENMODE_IFRAME,
  NAV_OPENMODE_WUJIE,
} from './constants.js';
import { navUrlToPath } from './url.js';
import type { NavGroupAttr } from './types.js';

/** 将任意 openMode 规范为 ajax | iframe | wujie，未知值默认 wujie */
export function normalizeNavOpenMode(k: unknown): NavOpenMode {
  if (k === NAV_OPENMODE_IFRAME) return NAV_OPENMODE_IFRAME;
  if (k === NAV_OPENMODE_AJAX) return NAV_OPENMODE_AJAX;
  return NAV_OPENMODE_WUJIE;
}

/** 标签去重键：pathname+search 与 openMode 组合 */
export function buildNavTabDedupeKey(urlPath: string, openMode: unknown): string {
  return navUrlToPath(urlPath) + '\n' + normalizeNavOpenMode(openMode);
}

/** data-nav-group 属性：appId@groupId */
export function formatNavGroupAttr(appId: unknown, groupId: unknown): string {
  const a = appId != null && appId !== '' ? String(appId) : '';
  const g = groupId != null && groupId !== '' ? String(groupId) : '';
  if (!a && !g) return '';
  return a + '@' + g;
}

/** 解析 data-nav-group，缺 @ 时整段视为 appId */
export function parseNavGroupAttr(raw: unknown): NavGroupAttr {
  const out: NavGroupAttr = { appId: '', groupId: '' };
  if (raw == null || String(raw).trim() === '') return out;
  const s = String(raw).trim();
  const at = s.indexOf('@');
  if (at < 0) {
    out.appId = s;
    return out;
  }
  out.appId = s.slice(0, at).trim();
  out.groupId = s.slice(at + 1).trim();
  return out;
}

/** 无界子应用实例名：app_{appId}_tab_{序号} */
export function buildMicroAppName(appId: unknown, tabId: unknown): string {
  const aid = String(appId != null && appId !== '' ? appId : '0').replace(/[^a-zA-Z0-9_-]/g, '_');
  const mx = /^ems_tab_(\d+)$/.exec(String(tabId || ''));
  const idx = mx ? mx[1] : String(tabId || '0').replace(/[^a-zA-Z0-9_-]/g, '_');
  return 'app_' + aid + '_tab_' + idx;
}
