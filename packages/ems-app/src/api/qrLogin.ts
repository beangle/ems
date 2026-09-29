/**
 * EMS CAS 扫码登录（kiosk）API，见 ems-cas.md §5。
 *
 * - 创建二维码：POST {casBase}/qrcode/create?service={service}&name={name}&lastQrcodeId={id?}
 *   返回 { qrcodeId, secret, expireAt, scanUrl }
 *   `name` 须与 EMS 管理端登记的 App.name 一致；`lastQrcodeId` 传上次码时旧码立即作废。
 * - 订阅状态（推荐）：GET {casBase}/qrcode/stream?qrcodeId={id}&secret={secret}
 *   标准 EventSource，命名事件 pending/scanned/confirmed/cancelled/expired，
 *   confirmed 事件 data 附带一次性 authToken；终态后服务端关闭连接。
 * - 查询状态（降级/调试）：GET {casBase}/qrcode/status?qrcodeId={id}&secret={secret}
 * - 设备登录：GET {casBase}/qrcode/login?qrcodeId={id}&authToken={token}&service={service}
 *   设备浏览器整页跳转，CAS 302 回 service（同域写会话 cookie，跨域回传 URP_SID）。
 */

export type QrLoginStatus = 'pending' | 'scanned' | 'confirmed' | 'cancelled' | 'expired'

export interface QrLoginSession {
  qrcodeId: string
  /** 轮询/订阅身份认证密钥，仅设备保存，不得写入二维码或日志 */
  secret: string
  /** 过期时间（Unix 秒） */
  expireAt: number
  /** 二维码渲染内容：手机扫开后进入确认页 */
  scanUrl: string
}

export interface QrLoginStatusResult {
  status: QrLoginStatus
  /** confirmed 时返回，用于换取设备会话 */
  authToken?: string
}

export interface QrLoginApiOptions {
  /** CAS 地址解析函数，如 runtimeConfig.getCasUrl；未传时用同源 `/cas` 前缀。 */
  getCasUrl?: (path: string) => string
  fetcher?: typeof fetch
}

const QRCODE_PATHS = {
  create: '/qrcode/create',
  stream: '/qrcode/stream',
  status: '/qrcode/status',
  login: '/qrcode/login',
} as const

function resolveCasUrl(path: string, options: QrLoginApiOptions): string {
  return options.getCasUrl ? options.getCasUrl(path) : `/cas${path}`
}

async function requestJson<T>(path: string, options: QrLoginApiOptions, init?: RequestInit): Promise<T> {
  const fetcher = options.fetcher ?? ((input: RequestInfo | URL, init?: RequestInit) => globalThis.fetch(input, init))
  const response = await fetcher(path, {
    credentials: 'include',
    headers: { Accept: 'application/json' },
    cache: 'no-store',
    ...init,
  })
  if (!response.ok) {
    throw new Error(`扫码登录请求失败 (${response.status})`)
  }
  return response.json() as Promise<T>
}

/** 创建二维码；`lastQrcodeId` 传上次码时旧码立即作废（刷新场景）。 */
export async function createQrLogin(
  service: string,
  name: string,
  lastQrcodeId?: string,
  options: QrLoginApiOptions = {},
): Promise<QrLoginSession> {
  const url = new URL(resolveCasUrl(QRCODE_PATHS.create, options), window.location.origin)
  url.searchParams.set('service', service)
  url.searchParams.set('name', name)
  if (lastQrcodeId) url.searchParams.set('lastQrcodeId', lastQrcodeId)
  return requestJson<QrLoginSession>(url.toString(), options, { method: 'POST' })
}

/** SSE 订阅地址：设备浏览器用 EventSource 连接，接收命名状态事件。 */
export function buildQrStreamUrl(qrcodeId: string, secret: string, options: QrLoginApiOptions = {}): string {
  const url = new URL(resolveCasUrl(QRCODE_PATHS.stream, options), window.location.origin)
  url.searchParams.set('qrcodeId', qrcodeId)
  url.searchParams.set('secret', secret)
  return url.toString()
}

/** 单次查询状态（SSE 不可用时降级轮询）。 */
export async function fetchQrLoginStatus(
  qrcodeId: string,
  secret: string,
  options: QrLoginApiOptions = {},
): Promise<QrLoginStatusResult> {
  const url = new URL(resolveCasUrl(QRCODE_PATHS.status, options), window.location.origin)
  url.searchParams.set('qrcodeId', qrcodeId)
  url.searchParams.set('secret', secret)
  return requestJson<QrLoginStatusResult>(url.toString(), options)
}

/** 设备登录跳转地址：confirmed + authToken 后整页跳转，换取设备会话。 */
export function buildQrLoginUrl(qrcodeId: string, authToken: string, service: string, options: QrLoginApiOptions = {}): string {
  const url = new URL(resolveCasUrl(QRCODE_PATHS.login, options), window.location.origin)
  url.searchParams.set('qrcodeId', qrcodeId)
  url.searchParams.set('authToken', authToken)
  url.searchParams.set('service', service)
  return url.toString()
}
