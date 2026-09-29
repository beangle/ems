import { setEmsApiBaseUrl, type EmsQueryParams } from './request'

export interface EmsRuntimeConfigPayload {
  ems_base?: string
  cas_base?: string
  cas_auth_login_url?: string
  cas_login_url?: string
  cas_logout_url?: string
  /** Domain / org logo for SPA shell brand (e.g. from configs.json). */
  logoUrl?: string
}

export interface EmsThemeConfigPayload {
  searchBgColor?: string
  navbarBgColor?: string
  primaryColor?: string
  gridbarBgColor?: string
  gridBorderColor?: string
  [key: string]: unknown
}

export interface EmsDomainOrgConfig {
  code?: string | number
  name?: string
  id?: number
  shortName?: string
  wwwUrl?: string
  logoUrl?: string
}

/**
 * 应用域信息（来自平台 `domains.json`）。
 * `title` 可作为系统名，`logoUrl` 可作为品牌 logo。
 */
export interface EmsDomainConfigPayload {
  hostname?: string
  org?: EmsDomainOrgConfig
  name?: string
  id?: number
  title?: string
  logoUrl?: string
}

export interface EmsRuntimeConfigOptions {
  appBasePath: string
  apiBaseUrl?: string
  casBaseUrl?: string
  casAuthLoginUrl?: string
  casLoginUrl?: string
  casLogoutUrl?: string
}

export interface LoadEmsRuntimeConfigsOptions {
  appConfigPath?: string
  apiBaseUrl?: string
  /**
   * 主题配置路径；传 `null` 时跳过主题配置加载（如 Wujie 模式下主题由宿主门户同步）。
   * 不传时使用默认平台地址 `emsThemeConfigPath`。
   */
  themeConfigPath?: string | null
  fetcher?: typeof fetch
  warn?: (message: string, error: unknown) => void
}

export interface LoadedEmsRuntimeConfigs {
  appConfig: EmsRuntimeConfigPayload | null
  themeConfig: EmsThemeConfigPayload | null
}

export interface EmsRuntimeConfig {
  readonly appBasePath: string
  readonly apiBaseUrl: string
  readonly emsBaseUrl: string
  readonly logoUrl: string
  readonly domainConfig: EmsDomainConfigPayload | null
  readonly appTitle: string
  readonly themeConfig: EmsThemeConfigPayload | null
  readonly casServerBaseUrl: string
  readonly casAuthLoginUrl: string
  readonly casLoginUrl: string
  readonly casLogoutUrl: string
  applyConfig(config: EmsRuntimeConfigPayload): void
  setApiBaseUrl(url: string): void
  setEmsBaseUrl(url: string): void
  setCasBaseUrl(url: string): void
  setCasUrls(config: EmsRuntimeConfigPayload): void
  setLogoUrl(url: string): void
  setDomainConfig(config: EmsDomainConfigPayload | null): void
  setThemeConfig(config: EmsThemeConfigPayload | null): void
  getApiUrl(path: string, urpSid?: string | null, query?: EmsQueryParams): string
  getCasUrl(path: string): string
  getCasAuthLoginUrl(): string
  getCasLoginUrl(): string
  getCasLogoutUrl(): string
  isCrossOriginUrl(url: string): boolean
  sharesSessionHost(url: string): boolean
}

export const emsThemeConfigPath = '/api/platform/config/themes.json'
export const emsDomainsConfigPath = '/api/platform/config/domains.json'

export async function loadEmsRuntimeConfigs(options: LoadEmsRuntimeConfigsOptions = {}): Promise<LoadedEmsRuntimeConfigs> {
  const fetcher = options.fetcher ?? ((input, init) => globalThis.fetch(input, init))
  const apiBaseUrl = normalizeUrl(options.apiBaseUrl || getCurrentOrigin())
  const result: LoadedEmsRuntimeConfigs = {
    appConfig: null,
    themeConfig: null,
  }

  if (options.appConfigPath) {
    try {
      result.appConfig = await fetchJson<EmsRuntimeConfigPayload>(fetcher, joinUrl(apiBaseUrl, options.appConfigPath))
    } catch (error) {
      options.warn?.('运行时配置加载失败，继续使用构建配置', error)
    }
  }

  if (options.themeConfigPath !== null) {
    try {
      result.themeConfig = await fetchJson<EmsThemeConfigPayload>(fetcher, joinUrl(apiBaseUrl, options.themeConfigPath || emsThemeConfigPath))
    } catch (error) {
      options.warn?.('平台主题配置加载失败，继续使用默认主题', error)
    }
  }

  return result
}

export interface LoadEmsDomainConfigOptions {
  emsBaseUrl?: string
  fetcher?: typeof fetch
  warn?: (message: string, error: unknown) => void
}

/**
 * 从平台加载当前应用的域信息：`{emsBase}/api/platform/config/domains.json`。
 * 返回的 `title` 可作为系统名，`logoUrl` 可作为品牌 logo。
 * 加载失败返回 null，不抛异常。
 */
export async function loadEmsDomainConfig(options: LoadEmsDomainConfigOptions = {}): Promise<EmsDomainConfigPayload | null> {
  const fetcher = options.fetcher ?? ((input, init) => globalThis.fetch(input, init))
  const baseUrl = normalizeUrl(options.emsBaseUrl || getCurrentOrigin())
  try {
    return await fetchJson<EmsDomainConfigPayload>(fetcher, joinUrl(baseUrl, emsDomainsConfigPath))
  } catch (error) {
    options.warn?.('平台域名配置加载失败，使用本地系统名', error)
    return null
  }
}

export function createEmsRuntimeConfig(options: EmsRuntimeConfigOptions): EmsRuntimeConfig {
  let apiBaseUrl = normalizeUrl(options.apiBaseUrl || getCurrentOrigin())
  let emsBaseUrl = apiBaseUrl
  let logoUrl = ''
  let domainConfig: EmsDomainConfigPayload | null = null
  let themeConfig: EmsThemeConfigPayload | null = null
  let casServerBaseUrl = normalizeUrl(options.casBaseUrl || '')
  let casAuthLoginUrl = options.casAuthLoginUrl || ''
  let casLoginUrl = options.casLoginUrl || ''
  let casLogoutUrl = options.casLogoutUrl || ''

  const runtimeConfig: EmsRuntimeConfig = {
    get appBasePath() {
      return options.appBasePath
    },

    get apiBaseUrl() {
      return apiBaseUrl
    },

    get emsBaseUrl() {
      return emsBaseUrl
    },

    get logoUrl() {
      return logoUrl
    },

    get domainConfig() {
      return domainConfig
    },

    get appTitle() {
      return domainConfig?.title || ''
    },

    get themeConfig() {
      return themeConfig
    },

    get casServerBaseUrl() {
      return casServerBaseUrl
    },

    get casAuthLoginUrl() {
      return casAuthLoginUrl
    },

    get casLoginUrl() {
      return casLoginUrl
    },

    get casLogoutUrl() {
      return casLogoutUrl
    },

    applyConfig(config: EmsRuntimeConfigPayload): void {
      if (config.ems_base) runtimeConfig.setEmsBaseUrl(config.ems_base)
      if (config.logoUrl) runtimeConfig.setLogoUrl(config.logoUrl)
      else if (domainConfig?.logoUrl) runtimeConfig.setLogoUrl(domainConfig.logoUrl)
      runtimeConfig.setCasUrls(config)
    },

    setApiBaseUrl(url: string): void {
      apiBaseUrl = normalizeUrl(url || getCurrentOrigin())
    },

    setEmsBaseUrl(url: string): void {
      emsBaseUrl = normalizeUrl(url)
      if (!options.casBaseUrl) casServerBaseUrl = joinUrl(emsBaseUrl, '/cas')
    },

    setCasBaseUrl(url: string): void {
      casServerBaseUrl = normalizeUrl(url)
    },

    setCasUrls(config: EmsRuntimeConfigPayload): void {
      if (config.cas_base) runtimeConfig.setCasBaseUrl(config.cas_base)
      if (config.cas_auth_login_url) casAuthLoginUrl = config.cas_auth_login_url
      if (config.cas_login_url) casLoginUrl = config.cas_login_url
      if (config.cas_logout_url) casLogoutUrl = config.cas_logout_url
    },

    setLogoUrl(url: string): void {
      const trimmed = String(url || '').trim()
      logoUrl = /^file:/i.test(trimmed) ? '' : trimmed
    },

    setDomainConfig(config: EmsDomainConfigPayload | null): void {
      domainConfig = config
      if (config?.title && !logoUrl && config.logoUrl) {
        runtimeConfig.setLogoUrl(config.logoUrl)
      }
    },

    setThemeConfig(config: EmsThemeConfigPayload | null): void {
      themeConfig = config
    },

    getApiUrl(path: string, urpSid?: string | null, query?: EmsQueryParams): string {
      const url = appendQuery(joinUrl(apiBaseUrl, path), query)
      return appendUrpSidIfNeeded(url, urpSid)
    },

    getCasUrl(path: string): string {
      return joinUrl(casServerBaseUrl || joinUrl(emsBaseUrl, '/cas'), path)
    },

    getCasAuthLoginUrl(): string {
      return casAuthLoginUrl || runtimeConfig.getCasUrl('/auth/login.json')
    },

    getCasLoginUrl(): string {
      return casLoginUrl || runtimeConfig.getCasUrl('/login')
    },

    getCasLogoutUrl(): string {
      return casLogoutUrl || runtimeConfig.getCasUrl('/logout')
    },

    isCrossOriginUrl(url: string): boolean {
      return isCrossOriginUrl(url)
    },

    sharesSessionHost(url: string): boolean {
      return sharesSessionHost(url)
    },
  }

  setEmsApiBaseUrl(() => runtimeConfig.apiBaseUrl)
  return runtimeConfig
}

export const normalizeUrl = (url: string): string => url.replace(/\/+$/, '')

export const getCurrentOrigin = (): string => {
  if (typeof window === 'undefined') return ''
  return window.location.origin
}

export const joinUrl = (baseUrl: string, path: string): string => {
  if (!baseUrl) return path
  return `${normalizeUrl(baseUrl)}/${path.replace(/^\/+/, '')}`
}

export const isCrossOriginUrl = (url: string): boolean => {
  if (typeof window === 'undefined') return false
  return new URL(url, window.location.origin).origin !== window.location.origin
}

/**
 * 会话共享判定：cookie 不按端口隔离（RFC 6265），SameSite 也只按 scheme + 站点计算。
 * 因此同协议 + 同主机名（端口可不同）的地址与当前页面共用同一 cookie jar，
 * 无需把 URP_SID 放进 URL。协议不同仍需附加（Secure cookie 不会走 http）。
 */
export const sharesSessionHost = (url: string): boolean => {
  if (typeof window === 'undefined') return false
  const target = new URL(url, window.location.origin)
  const current = new URL(window.location.origin)
  return target.protocol === current.protocol && target.hostname === current.hostname
}

export const appendUrpSidIfNeeded = (url: string, urpSid?: string | null): string => {
  if (!urpSid || (sharesSessionHost(url) && !url.startsWith('/'))) return url
  return appendQuery(url, { URP_SID: urpSid })
}

const appendQuery = (url: string, params?: EmsQueryParams): string => {
  if (!params) return url
  const nextUrl = new URL(url, typeof window === 'undefined' ? 'http://localhost' : window.location.origin)
  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined) nextUrl.searchParams.set(key, String(value))
  })
  return url.startsWith('http') ? nextUrl.toString() : `${nextUrl.pathname}${nextUrl.search}`
}

async function fetchJson<T>(fetcher: typeof fetch, url: string): Promise<T | null> {
  const response = await fetcher(url, {
    method: 'GET',
    headers: { Accept: 'application/json' },
    cache: 'no-store',
  })
  if (!response.ok) return null
  return response.json() as Promise<T>
}
