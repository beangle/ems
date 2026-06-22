import { createApi } from './api.js';

const api = createApi();

if (typeof define === 'function' && define.amd) {
  define('ems-shell', function () {
    return api;
  });
}

if (typeof window !== 'undefined') {
  window.emsShell = api as unknown as EmsShellApi;
  /** @deprecated 兼容 ajax 片段 callback=ems.*（如消息/待办 newly） */
  window.ems = api as unknown as EmsShellApi;
}

export default api;

export { hostName, sameDomain, navUrlToPath, resolveNavAbsoluteUrl, urlsMatchForRouting } from './url.js';
export {
  normalizeNavOpenMode,
  buildNavTabDedupeKey,
  formatNavGroupAttr,
  parseNavGroupAttr,
  buildMicroAppName,
} from './tab-keys.js';
export { resolveMultiTabParam, getMultiTabPreference } from './storage.js';
export type { EmsShellApi } from './api.js';
