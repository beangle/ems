import { onBeforeUnmount, onMounted, ref } from 'vue'
import {
  buildQrLoginUrl,
  buildQrStreamUrl,
  createQrLogin,
  fetchQrLoginStatus,
  type QrLoginApiOptions,
  type QrLoginSession,
  type QrLoginStatus,
  type QrLoginStatusResult,
} from '../api/qrLogin'

export interface UseQrLoginOptions {
  /** 应用名，须与 EMS 管理端登记的 App.name 一致 */
  name: string
  /** 登录成功后的回跳地址（service 参数） */
  service: string
  /** 组件挂载后自动创建并订阅二维码；默认 true。CAS 回跳等场景可置 false 手动 refresh */
  autoStart?: boolean
  /** 二维码过期后自动生成新码；默认 true */
  autoRefreshOnExpired?: boolean
  /** 轮询降级间隔（毫秒），默认 2000 */
  pollInterval?: number
  /** confirmed 回调：携带 qrcodeId 与 authToken，供调用方跳转换取会话 */
  onConfirmed?: (qrcodeId: string, authToken: string) => void
  /** cancelled 回调 */
  onCancelled?: () => void
  api?: QrLoginApiOptions
}

export interface UseQrLogin {
  status: ReturnType<typeof ref<QrLoginStatus>>
  /** 当前有效二维码的 scanUrl（变化时调用方据此生成二维码图） */
  scanUrl: ReturnType<typeof ref<string>>
  loadFailed: ReturnType<typeof ref<boolean>>
  errorMsg: ReturnType<typeof ref<string>>
  /** 当前二维码标识，可用于刷新 */
  qrcodeId: string
  /** 刷新二维码（携带 lastQrcodeId 作废旧码） */
  refresh: () => Promise<void>
  /** 主动停止订阅/轮询 */
  stop: () => void
}

export function useQrLogin(options: UseQrLoginOptions): UseQrLogin {
  const { name, service, autoStart = true, autoRefreshOnExpired = true, pollInterval = 2000, onConfirmed, onCancelled, api } = options

  const status = ref<QrLoginStatus>('pending')
  const scanUrl = ref('')
  const loadFailed = ref(false)
  const errorMsg = ref('')

  let qrcodeId = ''
  let secret = ''
  let eventSource: EventSource | null = null
  let pollTimer: number | undefined
  let expireTimer: number | undefined
  let mounted = true
  /** 刷新序号：并发刷新时仅最后一次生效，避免旧请求的异步结果创建多余 stream */
  let refreshSeq = 0

  function unsubscribeStream() {
    eventSource?.close()
    eventSource = null
  }

  function stopPolling() {
    if (pollTimer) {
      window.clearInterval(pollTimer)
      pollTimer = undefined
    }
  }

  function clearExpireTimer() {
    if (expireTimer) {
      window.clearTimeout(expireTimer)
      expireTimer = undefined
    }
  }

  function stopAllListeners() {
    unsubscribeStream()
    stopPolling()
    clearExpireTimer()
  }

  function completeLogin(authToken: string) {
    stopAllListeners()
    status.value = 'confirmed'
    onConfirmed?.(qrcodeId, authToken)
  }

  function handleStatus(result: QrLoginStatusResult) {
    if (!mounted) return
    status.value = result.status
    if (result.status === 'confirmed' && result.authToken) {
      completeLogin(result.authToken)
    } else if (result.status === 'cancelled') {
      stopAllListeners()
      onCancelled?.()
    } else if (result.status === 'expired') {
      stopAllListeners()
      if (autoRefreshOnExpired) {
        void refresh()
      }
    }
  }

  function onStreamEvent(event: MessageEvent) {
    if (!mounted) return
    let result: QrLoginStatusResult
    try {
      result = JSON.parse(event.data) as QrLoginStatusResult
    } catch {
      return
    }
    handleStatus(result)
  }

  async function pollStatus() {
    if (!qrcodeId || !secret || !mounted) return
    try {
      const result = await fetchQrLoginStatus(qrcodeId, secret, api)
      handleStatus(result)
    } catch {
      // 轮询失败不打断，下次重试
    }
  }

  function startPolling() {
    stopPolling()
    pollTimer = window.setInterval(() => {
      void pollStatus()
    }, pollInterval)
  }

  /** 订阅状态：优先 SSE（EventSource），不可用时降级轮询。 */
  function subscribeStatus() {
    stopAllListeners()
    if (typeof EventSource === 'undefined') {
      startPolling()
      return
    }
    const es = new EventSource(buildQrStreamUrl(qrcodeId, secret, api))
    eventSource = es
    ;(['pending', 'scanned', 'confirmed', 'cancelled', 'expired'] as const).forEach((name) => {
      es.addEventListener(name, onStreamEvent)
    })
  }

  async function refresh(): Promise<void> {
    const seq = ++refreshSeq
    stopAllListeners()
    status.value = 'pending'
    loadFailed.value = false
    errorMsg.value = ''
    try {
      const session = await createQrLogin(service, name, qrcodeId || undefined, api)
      if (seq !== refreshSeq) return // 已有更新的刷新，丢弃本次结果
      applySession(session)
      subscribeStatus()

      const expireAt = new Date(session.expireAt * 1000).getTime()
      if (!Number.isNaN(expireAt)) {
        const wait = Math.max(expireAt - Date.now(), 1000)
        expireTimer = window.setTimeout(() => {
          if (status.value === 'pending' || status.value === 'scanned') {
            status.value = 'expired'
            stopAllListeners()
            // 本地定时器到期也自动刷新（SSE 未推送 expired 的兜底路径）
            if (autoRefreshOnExpired) {
              void refresh()
            }
          }
        }, wait)
      }
    } catch (error) {
      if (seq !== refreshSeq) return
      console.warn('[useQrLogin] 创建登录二维码失败', error)
      loadFailed.value = true
      errorMsg.value = error instanceof Error ? error.message : '生成登录二维码失败'
    }
  }

  function applySession(session: QrLoginSession) {
    qrcodeId = session.qrcodeId
    secret = session.secret
    scanUrl.value = session.scanUrl
  }

  onMounted(() => {
    mounted = true
    if (autoStart) void refresh()
  })

  onBeforeUnmount(() => {
    mounted = false
    stopAllListeners()
  })

  return {
    status,
    scanUrl,
    loadFailed,
    errorMsg,
    get qrcodeId() {
      return qrcodeId
    },
    refresh,
    stop: stopAllListeners,
  }
}

export { buildQrLoginUrl }
