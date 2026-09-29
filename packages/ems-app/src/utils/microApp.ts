export interface WujieAppLifecycles {
  mount: () => void | Promise<void>
  unmount: () => void
}

declare global {
  interface Window {
    __POWERED_BY_WUJIE__?: boolean
    __WUJIE?: { mount?: () => void }
    __WUJIE_PUBLIC_PATH__?: string
    __WUJIE_MOUNT?: () => void | Promise<void>
    __WUJIE_UNMOUNT?: () => void
  }
}

/**
 * `?micro=1` 在模块加载时快照一次。
 *
 * 应用初始化常会用 `history.replaceState` 清掉一次性查询参数（如本地登录 token），
 * 之后再读 `location.search` 就看不到 `micro=1` 了；而组件 setup 发生在初始化之后，
 * 所以必须在模块加载时就记住它。
 */
const microFlagAtLoad =
  typeof window === 'undefined' ||
  new URLSearchParams(window.location.search).get('micro') === '1'

/** Returns true inside Wujie, or when `?micro=1` is used for local shell testing. */
export function isWujieMicroApp(): boolean {
  if (typeof window === 'undefined') return false
  if (window.__POWERED_BY_WUJIE__) return true
  if (window.__WUJIE) return true
  if (window.__WUJIE_PUBLIC_PATH__) return true
  if ((window as unknown as { $wujie?: unknown }).$wujie) return true
  if (microFlagAtLoad) return true
  // 兜底：客户端跳转到带 micro=1 的地址时也应生效。
  return new URLSearchParams(window.location.search).get('micro') === '1'
}

/** Returns true when actually inside a real Wujie sandbox (not `?micro=1` testing). */
function isRealWujie(): boolean {
  return !!(window.__POWERED_BY_WUJIE__ || window.__WUJIE || window.__WUJIE_PUBLIC_PATH__)
}

/** Registers Wujie lifecycle hooks and mounts immediately when running standalone. */
export function registerWujieApp(lifecycles: WujieAppLifecycles): void {
  if (typeof window === 'undefined') return
  if (isRealWujie()) {
    window.__WUJIE_MOUNT = lifecycles.mount
    window.__WUJIE_UNMOUNT = lifecycles.unmount
  } else {
    void lifecycles.mount()
  }
}
