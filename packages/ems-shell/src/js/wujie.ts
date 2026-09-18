export function resolveWujieRuntime(): NonNullable<Window['wujie']> | null {
  if (typeof window !== 'undefined' && window.wujie?.startApp) {
    return window.wujie;
  }
  try {
    if (typeof beangle !== 'undefined' && beangle.amd?.pickModuleExport) {
      const picked = beangle.amd.pickModuleExport('wujie') as NonNullable<Window['wujie']> | undefined;
      if (picked?.startApp) {
        window.wujie = picked;
        return picked;
      }
    }
  } catch {
    /* ignore */
  }
  return null;
}

/** 向无界总线发事件（alive 子应用可 $on）；运行时未就绪时静默跳过 */
export function emitWujieBus(event: string, ...args: unknown[]): void {
  if (!event) return;
  try {
    const bus = resolveWujieRuntime()?.bus ?? (typeof window !== 'undefined' ? window.wujie?.bus : undefined);
    bus?.$emit?.(event, ...args);
  } catch {
    /* ignore */
  }
}

/** 取 URL 的 origin + pathname（忽略 query/hash），用于比较是否同一个入口文档 */
function requestUrlKey(input: unknown): string {
  let raw = '';
  if (typeof input === 'string') {
    raw = input;
  } else if (input && typeof input === 'object') {
    raw = String((input as { url?: unknown }).url ?? '');
  }
  if (!raw) return '';
  try {
    const base = typeof window !== 'undefined' ? window.location.href : 'http://localhost/';
    const u = new URL(raw, base);
    return u.origin + (u.pathname.replace(/\/+$/, '') || '/');
  } catch {
    return raw.split('#')[0].split('?')[0];
  }
}

/**
 * 判断是否无界为**加载子应用入口文档**发起的请求。
 *
 * 无界把 `startApp({ fetch })` 装到子应用沙箱的 `window.fetch` 上（wujie-core `sandbox.active`），
 * 所以壳层这个 fetch 既用于加载入口 HTML/JS/CSS，也会收到子应用自己的接口请求。
 * 入口文档的地址与 tab 入口一致（强刷时只多一个 `_=时间戳`），据此把两者区分开，
 * 避免把「入口强刷」的 `cache: 'no-store'` 施加到子应用的业务请求上。
 */
export function isWujieEntryRequest(input: unknown, entryUrl: unknown): boolean {
  const target = requestUrlKey(input);
  return !!target && target === requestUrlKey(entryUrl);
}
