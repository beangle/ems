export type EmsQueryParams = Record<string, string | number | boolean | null | undefined>

export interface EmsRequestOptions extends RequestInit {
  query?: EmsQueryParams
}

export interface EmsApiClientOptions {
  baseUrl?: string
  headers?: HeadersInit
  fetcher?: typeof fetch
}

type RequestHeadersProvider = () => HeadersInit | undefined
type UrpSidProvider = () => string | null | undefined
type ApiBaseUrlProvider = () => string | null | undefined

let requestHeadersProvider: RequestHeadersProvider | undefined
let urpSidProvider: UrpSidProvider | undefined
let apiBaseUrlProvider: ApiBaseUrlProvider | undefined

export function setEmsRequestHeaders(provider: RequestHeadersProvider | undefined) {
  requestHeadersProvider = provider
}

export function setEmsUrpSid(provider: UrpSidProvider | undefined) {
  urpSidProvider = provider
}

export function setEmsApiBaseUrl(provider: ApiBaseUrlProvider | undefined) {
  apiBaseUrlProvider = provider
}

export class EmsApiClient {
  private readonly baseUrl: string
  private readonly headers?: HeadersInit
  private readonly fetcher: typeof fetch

  constructor(options: EmsApiClientOptions = {}) {
    this.baseUrl = normalizeUrl(options.baseUrl ?? apiBaseUrlProvider?.() ?? getDefaultApiBaseUrl())
    this.headers = options.headers
    this.fetcher = options.fetcher ?? ((input, init) => globalThis.fetch(input, init))
  }

  async request<T>(path: string, options: EmsRequestOptions = {}): Promise<T> {
    const { query, headers, ...init } = options
    const response = await this.fetcher(this.resolveUrl(path, query), {
      credentials: 'include',
      ...init,
      headers: this.buildHeaders(headers),
    })

    if (!response.ok) {
      throw new EmsApiError(response.status, response.statusText)
    }

    try {
      return (await response.json()) as T
    } catch (error) {
      // 空体 / 非法 JSON（如网关返回 HTML）时给出可捕获的业务错误，而非裸 SyntaxError。
      throw new EmsApiError(response.status, `Invalid JSON response: ${String(error)}`)
    }
  }

  /** Build a request URL (path + query + optional URP_SID for cross-origin or relative path). */
  resolveUrl(path: string, query?: EmsRequestOptions['query']): string {
    return this.buildUrl(path, query)
  }

  private buildUrl(path: string, query?: EmsRequestOptions['query']) {
    const fallbackOrigin = typeof window === 'undefined' ? 'http://localhost' : window.location.origin
    const url = new URL(path, this.baseUrl || fallbackOrigin)

    Object.entries(query ?? {}).forEach(([key, value]) => {
      if (value !== null && value !== undefined) {
        url.searchParams.set(key, String(value))
      }
    })

    const relative = !this.baseUrl && path.startsWith('/')
    const urpSid = urpSidProvider?.()
    if (urpSid && (relative || isCrossOrigin(url))) {
      url.searchParams.set('URP_SID', urpSid)
    }

    if (relative) return `${url.pathname}${url.search}${url.hash}`

    return url.toString()
  }

  private buildHeaders(headers?: HeadersInit) {
    const result = new Headers({ Accept: 'application/json' })
    new Headers(requestHeadersProvider?.()).forEach((value, key) => result.set(key, value))
    new Headers(this.headers).forEach((value, key) => result.set(key, value))
    new Headers(headers).forEach((value, key) => result.set(key, value))
    return result
  }
}

function getDefaultApiBaseUrl() {
  return import.meta.env?.VITE_API_BASE || ''
}

function normalizeUrl(url: string) {
  return url.replace(/\/+$/, '')
}

function isCrossOrigin(url: URL) {
  if (typeof window === 'undefined') return false
  return url.origin !== window.location.origin
}

export class EmsApiError extends Error {
  constructor(
    readonly status: number,
    readonly statusText: string,
  ) {
    super(`OpenURP API request failed: ${status} ${statusText}`)
    this.name = 'EmsApiError'
  }
}
