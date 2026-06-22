export function hostName(u1: unknown): string {
  if (u1 == null || u1 === '') return '';
  let s = String(u1);
  let slashIdx = s.indexOf('//');
  if (slashIdx === -1) {
    slashIdx = 0;
  } else {
    slashIdx += 2;
  }
  let endIdx = s.indexOf(':', slashIdx);
  if (endIdx === -1) {
    endIdx = s.indexOf('/', slashIdx);
  }
  if (endIdx === -1) {
    endIdx = s.length;
  }
  return s.substring(slashIdx, endIdx);
}

/** 相对路径 base 视为当前页同源 */
export function sameDomain(u1: unknown, u2: unknown): boolean {
  let h1 = hostName(u1);
  let h2 = hostName(u2);
  if (h1 === '' && u1 && String(u1).charAt(0) === '/') {
    h1 = hostName(typeof window !== 'undefined' ? window.location.href : '');
  }
  if (h2 === '' && u2 && String(u2).charAt(0) === '/') {
    h2 = hostName(typeof window !== 'undefined' ? window.location.href : '');
  }
  return h1 === h2;
}

/** 同站微应用 tab 记录的 url：pathname + search */
export function navUrlToPath(href: unknown): string {
  if (href == null || href === '') return '';
  const s = String(href).trim();
  if (!s) return '';
  const hashIdx = s.indexOf('#');
  if (hashIdx >= 0) {
    const frag = s.substring(hashIdx + 1);
    if (frag.charAt(0) === '/') {
      try {
        const base = typeof window !== 'undefined' ? window.location.origin : 'http://localhost';
        const uFrag = new URL(frag, base);
        return (uFrag.pathname || '/') + uFrag.search;
      } catch {
        return frag;
      }
    }
  }
  try {
    const base = typeof window !== 'undefined' ? window.location.href : 'http://localhost/';
    const u = new URL(s, base);
    return (u.pathname || '/') + u.search;
  } catch {
    const noHash = s.split('#')[0];
    return noHash.charAt(0) === '/' ? noHash : s;
  }
}

export function resolveNavAbsoluteUrl(pathOrUrl: string): string {
  if (!pathOrUrl) return '';
  try {
    const base = typeof window !== 'undefined' ? window.location.href : 'http://localhost/';
    return new URL(pathOrUrl, base).href;
  } catch {
    return pathOrUrl;
  }
}

export function normalizeContentUrlKey(href: string): string {
  if (!href) return '';
  try {
    const base = typeof window !== 'undefined' ? window.location.href : 'http://localhost/';
    const u = new URL(href, base);
    const path = u.pathname.replace(/\/+$/, '') || '/';
    return (u.origin + path + u.search).toLowerCase();
  } catch {
    return String(href).toLowerCase();
  }
}

export function urlsMatchForRouting(a: string, b: string): boolean {
  if (!a || !b) return false;
  const ka = normalizeContentUrlKey(a);
  const kb = normalizeContentUrlKey(b);
  if (ka === kb) return true;
  if (ka.startsWith(kb + '/') || kb.startsWith(ka + '/')) return true;
  return false;
}

/** 同源菜单入口 → 门户 hash URL */
export function menuEntryToHostUrl(entryHref: string): string | null {
  if (!entryHref) return null;
  try {
    if (typeof beangle !== 'undefined' && beangle.history?.convertUrl) {
      return beangle.history.convertUrl(entryHref);
    }
    const u = new URL(entryHref, window.location.href);
    const path = u.pathname + u.search;
    return window.location.origin + window.location.pathname.replace(/[^/]+$/, '') + '#' + path;
  } catch {
    return null;
  }
}
