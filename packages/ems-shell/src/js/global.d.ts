/// <reference types="jquery" />

/* 门户页面全局依赖，由 beangle static bundle 在 ems-shell 之前注入 */

declare const bg: { Go: (url: string | HTMLElement, targetId: string) => void };

interface BeangleAmd {
  pickModuleExport: (name: string) => unknown;
}

interface BeangleHistory {
  convertUrl: (url: string) => string;
}

interface BeangleCookie {
  get: (name: string) => string | null;
  set: (name: string, value: string, path: string, days: number) => void;
  remove: (name: string, path: string) => void;
}

interface BeangleGlobal {
  amd?: BeangleAmd;
  history?: BeangleHistory;
  cookie?: BeangleCookie;
  Go?: (url: string | HTMLElement, targetId: string) => void;
}

declare const beangle: BeangleGlobal;

declare const define: {
  (name: string, factory: () => unknown): void;
  amd?: unknown;
};

interface WujieRuntime {
  startApp: (opts: Record<string, unknown>) => Promise<void>;
  destroyApp?: (name: string) => void;
}

interface EmsShellApi {
  config: unknown;
  hostName: (u: unknown) => string;
  sameDomain: (a: unknown, b: unknown) => boolean;
  init: (
    profiles: unknown[],
    profileId?: string | number | { profile?: string | number } | null
  ) => void;
  createNav: (...args: unknown[]) => unknown;
  changeGroup: (el: HTMLElement) => void;
  setup: (theme: unknown, params: Record<string, string>) => void;
  getNav: () => unknown;
  clearNavState: () => void;
  clearNavStateOnLogout: () => void;
  applyStoredThemeIfPresent: (fallback?: unknown) => boolean;
  applyThemeVars: (theme: unknown) => void;
}

interface Window {
  wujie?: WujieRuntime;
  emsShell?: EmsShellApi;
  /** @deprecated 与 emsShell 相同；兼容 ajax callback=ems.* */
  ems?: EmsShellApi;
  __emsNavTabHistoryBound?: boolean;
  __emsTabFullscreenIconBound?: boolean;
}
