import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createEmsAuth, type EmsCurrentUser } from '../src/api/auth'

/** CAS 会话接口固定返回 401（未登录）。 */
const unauthorizedFetcher = vi.fn(async () => new Response('{}', { status: 401 }))

function createAuth(options: { requireLoginOnInit?: boolean } = {}) {
  return createEmsAuth<EmsCurrentUser>({
    appBasePath: '/std/spa/m/',
    casAuthLoginUrl: '/cas/auth/login.json',
    casLoginUrl: '/cas/login',
    casLogoutUrl: '/cas/logout',
    fetcher: unauthorizedFetcher,
    ...options,
  })
}

describe('createEmsAuth 初始化', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.restoreAllMocks()
  })

  it('默认未登录时跳转登录页', async () => {
    const auth = createAuth()
    const requireLogin = vi.spyOn(auth.sessionService, 'requireLogin').mockImplementation(() => {})

    expect(await auth.initializeAuth()).toBeNull()
    expect(requireLogin).toHaveBeenCalled()
    expect(auth.authReady.value).toBe(true)
  })

  it('requireLoginOnInit=false 时只清空会话、不跳转', async () => {
    localStorage.setItem('beangle.ems.auth_token', 'stale-token')
    localStorage.setItem('beangle.ems.user_info', JSON.stringify({ name: '过期用户', code: 'xh0001' }))

    const auth = createAuth({ requireLoginOnInit: false })
    const requireLogin = vi.spyOn(auth.sessionService, 'requireLogin').mockImplementation(() => {})

    expect(await auth.initializeAuth()).toBeNull()
    expect(requireLogin).not.toHaveBeenCalled()
    expect(auth.sessionService.getToken()).toBeNull()
    expect(auth.currentUser.value).toBeNull()
  })
})
