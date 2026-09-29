/**
 * JSON:API 客户端：Beangle/OpenURP 后端（plur-seo / she `JsonApiWS`）统一使用
 * `application/vnd.api+json` 媒体类型与 JSON:API 文档结构。
 *
 * 本模块在 `EmsApiClient`（base URL / `URP_SID` / 全局 headder 提供者）之上补齐协议语义：
 * 内容协商、`page[...]` / `filter[...]` 查询构建、文档与错误类型、401/302 → 跳登录、blob 下载。
 *
 * 认证与会话通过构造参数 `session` 注入（`createEmsAuth` 的 `sessionService` 结构兼容），
 * 因此本模块不直接依赖任何应用级配置。
 */
import { resolveAuthRedirectUrl, type AuthRedirectPayload } from './auth'
import { EmsApiClient } from './request'
import { isCrossOriginUrl } from './runtimeConfig'

/** JSON:API 媒体类型。 */
export const jsonApiContentType = 'application/vnd.api+json'

export interface JsonApiError {
  status?: string
  code?: string
  title?: string
  detail?: string
  source?: {
    pointer?: string
    parameter?: string
  }
}

export interface JsonApiMeta {
  page?: {
    number: number
    size: number
    total: number
  }
  [key: string]: unknown
}

export interface JsonApiResourceIdentifier {
  type: string
  id: string
}

export interface JsonApiRelationship {
  data?: JsonApiResourceIdentifier | JsonApiResourceIdentifier[] | null
  links?: Record<string, string>
}

export interface JsonApiResource<TAttributes = Record<string, unknown>> {
  type: string
  id: string
  attributes?: TAttributes
  relationships?: Record<string, JsonApiRelationship>
}

export interface JsonApiDocument<T> {
  data?: T
  errors?: JsonApiError[]
  meta?: JsonApiMeta
  links?: Record<string, string>
  included?: JsonApiResource[]
}

export class JsonApiRequestError extends Error {
  readonly status: number
  readonly errors: JsonApiError[]

  constructor(status: number, errors: JsonApiError[]) {
    super(errors[0]?.detail || errors[0]?.title || `Request failed with status ${status}`)
    this.name = 'JsonApiRequestError'
    this.status = status
    this.errors = errors
  }
}

/** JSON:API 列表查询参数：`page[number]` / `page[size]` / `sort` / `filter[...]`。 */
export interface EmsJsonApiListParams {
  pageNumber?: number
  pageSize?: number
  sort?: string
  query?: Record<string, string | number | boolean | undefined | null>
  filters?: Record<string, string | number | boolean | undefined | null>
}

/** 会话最小依赖：取认证头 + 未登录跳转（`createEmsAuth` 的 `sessionService` 结构兼容）。 */
export interface EmsJsonApiSession {
  getAuthHeaders(contentType?: string): Record<string, string>
  login(redirectUrl?: string): void
}

export interface EmsJsonApiClientOptions {
  /** 应用接口前缀（相对路径），如 `/api/edu/grade`；拼在每个请求 path 前，默认空。 */
  basePath?: string
  /** 接口源（绝对地址）。缺省使用 `createEmsRuntimeConfig` 注册的 apiBaseUrl。 */
  baseUrl?: string
  /** 提供 Authorization 并处理 401 跳登录；缺省不发认证头、未登录时仅抛错。 */
  session?: EmsJsonApiSession
  fetcher?: typeof fetch
  /** 自定义未登录跳转目标解析；默认取响应体 `redirectUrl` / `errors[].meta.redirectUrl` / `Location`。 */
  resolveRedirectUrl?: (payload: unknown, response: Response) => string | undefined
}

export class EmsJsonApiClient {
  private readonly basePath: string
  private readonly http: EmsApiClient
  private readonly session?: EmsJsonApiSession
  private readonly resolveRedirectUrl: (payload: unknown, response: Response) => string | undefined
  private readonly fetcher: typeof fetch

  constructor(options: EmsJsonApiClientOptions = {}) {
    this.basePath = (options.basePath ?? '').replace(/\/+$/, '')
    this.http = new EmsApiClient({ baseUrl: options.baseUrl })
    this.session = options.session
    this.resolveRedirectUrl =
      options.resolveRedirectUrl ?? ((payload, response) => resolveAuthRedirectUrl(payload as AuthRedirectPayload, response))
    this.fetcher = options.fetcher ?? ((input, init) => globalThis.fetch(input, init))
  }

  /** 构建请求地址（含 JSON:API 查询参数与按需追加的 `URP_SID`）。 */
  resolveUrl(path: string, params?: EmsJsonApiListParams): string {
    return this.url(path, params)
  }

  get<T>(path: string, params?: EmsJsonApiListParams): Promise<JsonApiDocument<T>> {
    return this.request<T>(this.url(path, params), { method: 'GET' })
  }

  getPlain<T>(path: string, params?: EmsJsonApiListParams): Promise<T> {
    return this.requestPlain<T>(this.url(path, params), { method: 'GET' })
  }

  post<T>(path: string, body: unknown): Promise<JsonApiDocument<T>> {
    return this.request<T>(this.url(path), { method: 'POST', body: JSON.stringify(body) })
  }

  postForm<T>(path: string, body: URLSearchParams): Promise<JsonApiDocument<T>> {
    return this.request<T>(this.url(path), {
      method: 'POST',
      body,
      headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
    })
  }

  postFormBlob(path: string, body: URLSearchParams): Promise<Blob> {
    return this.requestBlob(this.url(path), {
      method: 'POST',
      body,
      headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
    })
  }

  patch<T>(path: string, body: unknown): Promise<JsonApiDocument<T>> {
    return this.request<T>(this.url(path), { method: 'PATCH', body: JSON.stringify(body) })
  }

  async delete(path: string): Promise<void> {
    await this.request<void>(this.url(path), { method: 'DELETE' })
  }

  private async request<T>(url: string, init: RequestInit): Promise<JsonApiDocument<T>> {
    const response = await this.send(url, init, jsonApiHeaders(this.session, url, init))
    if (response.status === 204) return {} as JsonApiDocument<T>
    if (isUnauthorized(response)) {
      await this.handleUnauthorized(response)
      throw new JsonApiRequestError(401, [{ status: '401', title: '请先登录' }])
    }

    const payload = await readJson<JsonApiDocument<T>>(response)
    if (!response.ok) {
      const errors = payload?.errors ?? [{ status: String(response.status), title: response.statusText }]
      throw new JsonApiRequestError(response.status, errors)
    }
    return payload ?? ({} as JsonApiDocument<T>)
  }

  private async requestPlain<T>(url: string, init: RequestInit): Promise<T> {
    const response = await this.send(url, init, plainHeaders(this.session, url, init))
    if (isUnauthorized(response)) {
      await this.handleUnauthorized(response)
      throw new JsonApiRequestError(401, [{ status: '401', title: '请先登录' }])
    }

    const payload = await readJson<T>(response)
    if (!response.ok) {
      throw new JsonApiRequestError(response.status, [{ status: String(response.status), title: response.statusText }])
    }
    return payload as T
  }

  private async requestBlob(url: string, init: RequestInit): Promise<Blob> {
    const response = await this.send(url, init, blobHeaders(this.session, url, init))
    if (isUnauthorized(response)) {
      await this.handleUnauthorized(response)
      throw new JsonApiRequestError(401, [{ status: '401', title: '请先登录' }])
    }
    if (!response.ok) {
      const message = await response.text().catch(() => response.statusText)
      throw new JsonApiRequestError(response.status, [
        { status: String(response.status), title: message || response.statusText },
      ])
    }
    return response.blob()
  }

  private async send(url: string, init: RequestInit, headers: HeadersInit): Promise<Response> {
    return this.fetcher(url, {
      credentials: 'include',
      redirect: 'manual',
      ...init,
      headers,
    })
  }

  private async handleUnauthorized(response: Response): Promise<void> {
    if (!this.session) return
    const payload = await readJson<unknown>(response)
    this.session.login(this.resolveRedirectUrl(payload, response))
  }

  private url(path: string, params?: EmsJsonApiListParams): string {
    const full = `${this.basePath}${path.startsWith('/') ? path : `/${path}`}`
    const query: Record<string, string | number | boolean> = {}
    if (params?.pageNumber) query['page[number]'] = params.pageNumber
    if (params?.pageSize) query['page[size]'] = params.pageSize
    if (params?.sort) query['sort'] = params.sort
    assignParams(query, params?.query)
    Object.entries(params?.filters ?? {}).forEach(([key, value]) => {
      if (isPresent(value)) query[`filter[${key}]`] = value as string | number | boolean
    })
    return this.http.resolveUrl(full, query)
  }
}

function jsonApiHeaders(session: EmsJsonApiSession | undefined, url: string, init: RequestInit): Headers {
  const hasBody = init.body !== undefined && init.body !== null
  const headers = new Headers()
  if (isCrossOriginUrl(url)) {
    headers.set('Accept', jsonApiContentType)
    if (hasBody) headers.set('Content-Type', jsonApiContentType)
  } else {
    mergeInto(headers, session?.getAuthHeaders(hasBody ? jsonApiContentType : ''))
  }
  mergeInto(headers, init.headers)
  return headers
}

function plainHeaders(session: EmsJsonApiSession | undefined, url: string, init: RequestInit): Headers {
  const headers = new Headers()
  if (isCrossOriginUrl(url)) {
    headers.set('Accept', 'application/json')
  } else {
    mergeInto(headers, session?.getAuthHeaders(''))
    headers.set('Accept', 'application/json')
  }
  mergeInto(headers, init.headers)
  return headers
}

function blobHeaders(session: EmsJsonApiSession | undefined, url: string, init: RequestInit): Headers {
  const headers = new Headers()
  if (isCrossOriginUrl(url)) {
    headers.set('Accept', '*/*')
  } else {
    mergeInto(headers, session?.getAuthHeaders(''))
    headers.set('Accept', '*/*')
  }
  mergeInto(headers, init.headers)
  return headers
}

/** 以 Headers.set 合并（键大小写不敏感），后写覆盖先写。 */
function mergeInto(target: Headers, source?: HeadersInit): void {
  new Headers(source).forEach((value, key) => {
    target.set(key, value)
  })
}

function isUnauthorized(response: Response): boolean {
  return response.status === 0 || response.status === 302 || response.status === 401
}

function isPresent(value: unknown): value is string | number | boolean {
  return value !== undefined && value !== null && value !== ''
}

function assignParams(
  target: Record<string, string | number | boolean>,
  source?: Record<string, string | number | boolean | undefined | null>,
): void {
  Object.entries(source ?? {}).forEach(([key, value]) => {
    if (isPresent(value)) target[key] = value as string | number | boolean
  })
}

async function readJson<T>(response: Response): Promise<T | null> {
  try {
    return (await response.json()) as T
  } catch {
    return null
  }
}
