import { describe, expect, it, vi } from 'vitest'
import {
  createEmsRuntimeConfig,
  loadEmsRuntimeConfigs,
  emsThemeConfigPath,
  sharesSessionHost,
} from '../src/api/runtimeConfig'

const ok = (body: unknown) =>
  vi.fn(async () => new Response(JSON.stringify(body), { status: 200, headers: { 'Content-Type': 'application/json' } }))

describe('loadEmsRuntimeConfigs', () => {
  it('加载 appConfig 与默认主题配置', async () => {
    const appFetcher = ok({ ems_base: 'http://ems.example.com' })
    const themeFetcher = ok({ primaryColor: '#fff' })
    let themeUrl = ''
    const fetcher = vi.fn(async (input: string) => {
      if (input.includes('/configs.json')) return appFetcher(input)
      themeUrl = input
      return themeFetcher(input)
    })

    const result = await loadEmsRuntimeConfigs({
      appConfigPath: '/api/edu/learning/configs.json',
      apiBaseUrl: 'http://api.example.com',
      fetcher: fetcher as unknown as typeof fetch,
    })
    expect(result.appConfig).toEqual({ ems_base: 'http://ems.example.com' })
    expect(result.themeConfig).toEqual({ primaryColor: '#fff' })
    expect(themeUrl).toBe(`http://api.example.com${emsThemeConfigPath}`)
  })

  it('themeConfigPath 为 null 时跳过主题加载', async () => {
    const calls: string[] = []
    const fetcher = vi.fn(async (input: string) => {
      calls.push(input)
      return new Response(JSON.stringify({}), { status: 200 })
    })

    const result = await loadEmsRuntimeConfigs({
      appConfigPath: '/api/edu/learning/configs.json',
      apiBaseUrl: 'http://api.example.com',
      themeConfigPath: null,
      fetcher: fetcher as unknown as typeof fetch,
    })
    expect(result.themeConfig).toBeNull()
    expect(calls).toEqual(['http://api.example.com/api/edu/learning/configs.json'])
  })

  it('非 2xx 时降级为 null 且不告警', async () => {
    const warn = vi.fn()
    const fetcher = vi.fn(async () => new Response('', { status: 500 }))
    const result = await loadEmsRuntimeConfigs({
      appConfigPath: '/configs.json',
      apiBaseUrl: 'http://api.example.com',
      fetcher: fetcher as unknown as typeof fetch,
      warn,
    })
    expect(result.appConfig).toBeNull()
    expect(warn).not.toHaveBeenCalled()
  })

  it('网络异常时降级为 null 并告警', async () => {
    const warn = vi.fn()
    const fetcher = vi.fn(async () => {
      throw new Error('network down')
    })
    const result = await loadEmsRuntimeConfigs({
      appConfigPath: '/configs.json',
      apiBaseUrl: 'http://api.example.com',
      fetcher: fetcher as unknown as typeof fetch,
      warn,
    })
    expect(result.appConfig).toBeNull()
    expect(warn).toHaveBeenCalled()
  })
})

describe('getApiUrl', () => {
  const runtimeWith = (apiBaseUrl: string) =>
    createEmsRuntimeConfig({ appBasePath: '/edu/learning/', apiBaseUrl })

  it('第三个参数 query 与 path 自带查询串合并，再追加 URP_SID', () => {
    const runtime = runtimeWith('http://api.example.com')
    expect(runtime.getApiUrl('/api/x.json?apply.id=1', 'sid-1', { format: 'jsonapi' })).toBe(
      'http://api.example.com/api/x.json?apply.id=1&format=jsonapi&URP_SID=sid-1',
    )
  })

  it('不传 query 时保持原有行为', () => {
    const runtime = runtimeWith('http://api.example.com')
    expect(runtime.getApiUrl('/api/x.json', 'sid-1')).toBe('http://api.example.com/api/x.json?URP_SID=sid-1')
  })

  it('query 忽略 null/undefined，number/boolean 转为字符串', () => {
    const runtime = runtimeWith('http://api.example.com')
    expect(
      runtime.getApiUrl('/api/x.json', null, { page: 1, flush: false, a: null, b: undefined }),
    ).toBe('http://api.example.com/api/x.json?page=1&flush=false')
  })

  it('同源绝对地址不追加 URP_SID，但仍合并 query', () => {
    const runtime = runtimeWith('http://localhost:3000')
    expect(runtime.getApiUrl('/api/x.json', 'sid-1', { page: 1 })).toBe(
      'http://localhost:3000/api/x.json?page=1',
    )
  })

  it('同主机不同端口共享会话，不追加 URP_SID', () => {
    const runtime = runtimeWith('http://localhost:8080')
    expect(runtime.getApiUrl('/api/x.json', 'sid-1', { page: 1 })).toBe(
      'http://localhost:8080/api/x.json?page=1',
    )
  })

  it('同主机但协议不同仍追加 URP_SID', () => {
    const runtime = runtimeWith('https://localhost:8443')
    expect(runtime.getApiUrl('/api/x.json', 'sid-1')).toBe('https://localhost:8443/api/x.json?URP_SID=sid-1')
  })
})

describe('sharesSessionHost', () => {
  it('同主机忽略端口，视为共享会话', () => {
    expect(sharesSessionHost('http://localhost:8080/api/x.json')).toBe(true)
  })

  it('不同主机名不共享会话', () => {
    expect(sharesSessionHost('http://api.example.com/x.json')).toBe(false)
  })

  it('同主机但协议不同不共享会话', () => {
    expect(sharesSessionHost('https://localhost:8443/x.json')).toBe(false)
  })

  it('相对路径按当前页面解析，视为共享会话', () => {
    expect(sharesSessionHost('/api/x.json')).toBe(true)
  })
})
