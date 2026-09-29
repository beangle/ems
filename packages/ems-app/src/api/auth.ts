import { onMounted, ref, type Ref } from 'vue'
import { EMS_STORAGE_KEYS, EMS_URL_PARAMS } from '../contract'
import { setEmsUrpSid } from './request'

export interface EmsCurrentUser {
  name: string
  code: string
}

export interface CasUserResponse<TUser extends EmsCurrentUser = EmsCurrentUser> {
  authenticated?: boolean
  success?: boolean
  user?: TUser
  token?: string
  redirectUrl?: string
  error?: string
}

export interface AuthRedirectPayload {
  redirectUrl?: string
  errors?: Array<{ meta?: { redirectUrl?: string } }>
}

export interface EmsAuthOptions {
  appBasePath: string | (() => string)
  casAuthLoginUrl: string | (() => string)
  casLoginUrl: string | (() => string)
  casLogoutUrl: string | (() => string)
  tokenKey?: string
  userInfoKey?: string
  clearLocalStorageOnLogout?: boolean
  sidName?: string
  logoutUrl?: string | (() => string)
  fetcher?: typeof fetch
}

export interface EmsSessionService<TUser extends EmsCurrentUser = EmsCurrentUser> {
  setToken(token: string): void
  getToken(): string | null
  setUserInfo(userInfo: TUser): void
  getUserInfo(): TUser | null
  clearAuth(): void
  getAuthHeaders(contentType?: string): Record<string, string>
  login(redirectUrl?: string): void
  requireLogin(redirectUrl?: string): void
  logout(service?: string): Promise<void>
  getUserInfoBySid(sid: string): Promise<TUser | null>
  fetchCasUser(params?: Record<string, string>): Promise<TUser | null>
}

export interface EmsAuth<TUser extends EmsCurrentUser = EmsCurrentUser> {
  currentUser: Ref<TUser | null>
  authReady: Ref<boolean>
  sessionService: EmsSessionService<TUser>
  initializeAuth(): Promise<TUser | null>
  useAuth(): {
    currentUser: Ref<TUser | null>
    authReady: Ref<boolean>
    handleLogin: () => void
    handleLogout: (service?: string) => Promise<void>
  }
}

const defaultTokenKey = EMS_STORAGE_KEYS.authToken
const defaultUserInfoKey = EMS_STORAGE_KEYS.userInfo
const defaultSidName = EMS_URL_PARAMS.sid

export function createEmsAuth<TUser extends EmsCurrentUser = EmsCurrentUser>(options: EmsAuthOptions): EmsAuth<TUser> {
  const fetcher = options.fetcher ?? ((input, init) => globalThis.fetch(input, init))
  const sidName = options.sidName || defaultSidName

  const normalizePath = (path: string): string => {
    if (!path) return '/'
    const prefixed = path.startsWith('/') ? path : `/${path}`
    return prefixed.endsWith('/') ? prefixed : `${prefixed}/`
  }

  const getServiceUrl = (): string => `${window.location.origin}${normalizePath(readValue(options.appBasePath))}`

  const appendServiceParams = (url: string): string => {
    const nextUrl = new URL(url, window.location.origin)
    nextUrl.searchParams.set('service', getServiceUrl())
    nextUrl.searchParams.set('sid_name', sidName)
    return nextUrl.toString()
  }

  const sessionService: EmsSessionService<TUser> = {
    setToken(token: string): void {
      if (typeof localStorage === 'undefined') return
      localStorage.setItem(options.tokenKey || defaultTokenKey, token)
    },

    getToken(): string | null {
      if (typeof localStorage === 'undefined') return null
      return localStorage.getItem(options.tokenKey || defaultTokenKey)
    },

    setUserInfo(userInfo: TUser): void {
      if (typeof localStorage === 'undefined') return
      localStorage.setItem(options.userInfoKey || defaultUserInfoKey, JSON.stringify(userInfo))
    },

    getUserInfo(): TUser | null {
      const key = options.userInfoKey || defaultUserInfoKey
      if (typeof localStorage === 'undefined') return null
      try {
        const userInfo = localStorage.getItem(key)
        return userInfo ? JSON.parse(userInfo) as TUser : null
      } catch {
        // 共享 key（beangle.ems.user_info）可能被其他应用/旧版本写入非法 JSON，
        // 解析失败时清理并视为未登录，避免应用启动崩溃。
        try {
          localStorage.removeItem(key)
        } catch {
          /* ignore */
        }
        return null
      }
    },

    clearAuth(): void {
      if (typeof localStorage === 'undefined') return
      if (options.clearLocalStorageOnLogout) {
        localStorage.clear()
        return
      }
      localStorage.removeItem(options.tokenKey || defaultTokenKey)
      localStorage.removeItem(options.userInfoKey || defaultUserInfoKey)
    },

    getAuthHeaders(contentType = 'application/vnd.api+json'): Record<string, string> {
      const token = this.getToken()
      const headers: Record<string, string> = {
        Accept: 'application/vnd.api+json',
      }
      if (contentType) headers['Content-Type'] = contentType
      if (token) headers.Authorization = `Bearer ${token}`
      return headers
    },

    login(redirectUrl?: string): void {
      window.location.href = appendServiceParams(redirectUrl || readValue(options.casLoginUrl))
    },

    requireLogin(redirectUrl?: string): void {
      this.clearAuth()
      this.login(redirectUrl)
    },

    async logout(service?: string): Promise<void> {
      try {
        const logoutUrl = options.logoutUrl ? readValue(options.logoutUrl) : ''
        if (logoutUrl) {
          const token = this.getToken()
          const headers: Record<string, string> = { 'Content-Type': 'application/json' }
          if (token) headers.Authorization = `Bearer ${token}`

          await fetcher(logoutUrl, {
            method: 'POST',
            credentials: 'include',
            headers,
            cache: 'no-store',
          })
        }
      } catch {
        // Ignore logout transport errors and continue local cleanup.
      } finally {
        this.clearAuth()
        const redirectUrl = encodeURIComponent(service || getServiceUrl())
        window.location.href = `${readValue(options.casLogoutUrl)}?service=${redirectUrl}`
      }
    },

    async getUserInfoBySid(sid: string): Promise<TUser | null> {
      return this.fetchCasUser({ [sidName]: sid })
    },

    async fetchCasUser(params: Record<string, string> = {}): Promise<TUser | null> {
      try {
        const query = new URLSearchParams(params)
        const token = this.getToken()
        const loginUrl = readValue(options.casAuthLoginUrl)
        if (token && isCrossOriginUrl(loginUrl) && !query.has(sidName)) query.set(sidName, token)
        const queryString = query.toString()

        const response = await fetcher(`${loginUrl}${queryString ? `?${queryString}` : ''}`, {
          method: 'GET',
          credentials: 'include',
          redirect: 'manual',
          headers: { Accept: 'application/vnd.api+json' },
          cache: 'no-store',
        })

        if (response.status === 0 || response.status === 302 || response.status === 401) {
          this.requireLogin()
          return null
        }

        if (!response.ok) return this.getUserInfo()

        const data = (await response.json()) as CasUserResponse<TUser>
        if ((data.authenticated || data.success) && data.user) {
          if (data.token) this.setToken(data.token)
          this.setUserInfo(data.user)
          return data.user
        }

        this.requireLogin()
        return null
      } catch {
        return this.getUserInfo()
      }
    },
  }
  setEmsUrpSid(() => sessionService.getToken())

  const currentUser = ref<TUser | null>(sessionService.getUserInfo()) as Ref<TUser | null>
  const authReady = ref(false)
  let authInitPromise: Promise<TUser | null> | null = null

  const clearLoginQuery = () => {
    window.history.replaceState(null, '', `${window.location.origin}${window.location.pathname}${window.location.hash}`)
  }

  async function doInitializeAuth(): Promise<TUser | null> {
    const urlParams = new URLSearchParams(window.location.search)
    const urpSid = urlParams.get(sidName)
    const token = urlParams.get(EMS_URL_PARAMS.token)
    const userName = urlParams.get(EMS_URL_PARAMS.userName)
    const userCode = urlParams.get(EMS_URL_PARAMS.userCode)

    if (urpSid) {
      currentUser.value = await sessionService.getUserInfoBySid(urpSid)
      clearLoginQuery()
      return currentUser.value
    }

    if (token && userName && userCode) {
      sessionService.setToken(token)
      currentUser.value = { name: userName, code: userCode } as TUser
      sessionService.setUserInfo(currentUser.value)
      clearLoginQuery()
      return currentUser.value
    }

    currentUser.value = await sessionService.fetchCasUser()
    return currentUser.value
  }

  async function initializeAuth(): Promise<TUser | null> {
    if (authReady.value) return currentUser.value
    if (!authInitPromise) {
      authInitPromise = doInitializeAuth().finally(() => {
        authReady.value = true
      })
    }
    return authInitPromise
  }

  function useAuth() {
    const initAuth = async () => {
      await initializeAuth()
      authReady.value = true
    }

    const handleLogin = () => {
      sessionService.login()
    }

    const handleLogout = async (service?: string) => {
      await sessionService.logout(service)
    }

    onMounted(() => {
      if (!authReady.value) initAuth()
    })

    return {
      currentUser,
      authReady,
      handleLogin,
      handleLogout,
    }
  }

  return {
    currentUser,
    authReady,
    sessionService,
    initializeAuth,
    useAuth,
  }
}

export function resolveAuthRedirectUrl(payload: AuthRedirectPayload, response: Response): string | undefined {
  return payload.redirectUrl || payload.errors?.find((error) => error.meta?.redirectUrl)?.meta?.redirectUrl || response.headers.get('Location') || undefined
}

function readValue(value: string | (() => string)): string {
  return typeof value === 'function' ? value() : value
}

function isCrossOriginUrl(url: string): boolean {
  if (typeof window === 'undefined') return false
  return new URL(url, window.location.origin).origin !== window.location.origin
}
