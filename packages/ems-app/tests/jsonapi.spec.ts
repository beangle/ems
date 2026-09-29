import { describe, expect, it, vi } from 'vitest'
import { EmsJsonApiClient, JsonApiRequestError } from '../src/api/jsonapi'
import type { EmsJsonApiSession } from '../src/api/jsonapi'

const session = (): EmsJsonApiSession & { login: ReturnType<typeof vi.fn> } => ({
  getAuthHeaders: (contentType = 'application/vnd.api+json') => ({
    Accept: 'application/vnd.api+json',
    ...(contentType ? { 'Content-Type': contentType } : {}),
    Authorization: 'Bearer tok-1',
  }),
  login: vi.fn(),
})

const lastInit = (fetcher: ReturnType<typeof vi.fn>): RequestInit =>
  fetcher.mock.calls[fetcher.mock.calls.length - 1][1] as RequestInit

const headersOf = (init: RequestInit): Record<string, string> =>
  Object.fromEntries(new Headers(init.headers).entries())

describe('EmsJsonApiClient', () => {
  it('resolveUrl 拼接 basePath 并构建 JSON:API 查询参数', () => {
    const client = new EmsJsonApiClient({ basePath: '/api/edu/grade' })
    expect(
      client.resolveUrl('/std-gpas/search', {
        pageNumber: 2,
        pageSize: 20,
        sort: '-gpa',
        query: { keyword: 'a', empty: '' },
        filters: { projectId: 7 },
      }),
    ).toBe('/api/edu/grade/std-gpas/search?page%5Bnumber%5D=2&page%5Bsize%5D=20&sort=-gpa&keyword=a&filter%5BprojectId%5D=7')
  })

  it('同源请求带会话认证头与 JSON:API 内容协商', async () => {
    const fetcher = vi.fn(async () => new Response(JSON.stringify({ data: { ok: true } }), { status: 200 }))
    const client = new EmsJsonApiClient({ basePath: '/api/edu/grade', session: session(), fetcher })

    const doc = await client.post('/x', { a: 1 })

    expect(doc).toEqual({ data: { ok: true } })
    const init = lastInit(fetcher)
    const headers = headersOf(init)
    expect(headers.authorization).toBe('Bearer tok-1')
    expect(headers.accept).toBe('application/vnd.api+json')
    expect(headers['content-type']).toBe('application/vnd.api+json')
    expect(init.credentials).toBe('include')
    expect(init.redirect).toBe('manual')
  })

  it('跨域请求不带 Authorization（不泄露 token）', async () => {
    const fetcher = vi.fn(async () => new Response(JSON.stringify({ data: [] }), { status: 200 }))
    const client = new EmsJsonApiClient({ baseUrl: 'http://api.example.com', session: session(), fetcher })

    await client.get('/x')

    const headers = headersOf(lastInit(fetcher))
    expect(headers.authorization).toBeUndefined()
    expect(headers.accept).toBe('application/vnd.api+json')
  })

  it('postForm 使用表单内容类型并覆盖 JSON:API 默认值', async () => {
    const fetcher = vi.fn(async () => new Response(JSON.stringify({ data: [] }), { status: 200 }))
    const client = new EmsJsonApiClient({ basePath: '/api/edu/grade', session: session(), fetcher })

    await client.postForm('/x/search', new URLSearchParams({ 'page[number]': '1' }))

    const headers = headersOf(lastInit(fetcher))
    expect(headers['content-type']).toBe('application/x-www-form-urlencoded;charset=UTF-8')
  })

  it('204 返回空文档', async () => {
    const fetcher = vi.fn(async () => new Response(null, { status: 204 }))
    const client = new EmsJsonApiClient({ basePath: '/api/edu/grade', fetcher })
    await expect(client.delete('/x/1')).resolves.toBeUndefined()
  })

  it('非 2xx 抛 JsonApiRequestError 并带 errors', async () => {
    const fetcher = vi.fn(
      async () => new Response(JSON.stringify({ errors: [{ status: '422', title: '校验失败' }] }), { status: 422 }),
    )
    const client = new EmsJsonApiClient({ basePath: '/api/edu/grade', fetcher })

    await expect(client.get('/x')).rejects.toMatchObject({
      name: 'JsonApiRequestError',
      status: 422,
      errors: [{ status: '422', title: '校验失败' }],
    })
  })

  it('401 触发会话跳登录并抛错', async () => {
    const fetcher = vi.fn(
      async () => new Response(JSON.stringify({ redirectUrl: '/cas/login' }), { status: 401 }),
    )
    const s = session()
    const client = new EmsJsonApiClient({ basePath: '/api/edu/grade', session: s, fetcher })

    await expect(client.get('/x')).rejects.toBeInstanceOf(JsonApiRequestError)
    expect(s.login).toHaveBeenCalledWith('/cas/login')
  })

  it('postFormBlob 返回 Blob', async () => {
    const fetcher = vi.fn(async () => new Response('file-bytes', { status: 200 }))
    const client = new EmsJsonApiClient({ basePath: '/api/edu/grade', session: session(), fetcher })

    const blob = await client.postFormBlob('/x/export', new URLSearchParams())

    expect(blob.size).toBe(Buffer.byteLength('file-bytes'))
    expect(await blob.text()).toBe('file-bytes')
  })

  it('getPlain 不做 JSON:API 包装', async () => {
    const fetcher = vi.fn(async () => new Response(JSON.stringify({ plain: true }), { status: 200 }))
    const client = new EmsJsonApiClient({ basePath: '/api/edu/grade', session: session(), fetcher })

    await expect(client.getPlain('/x')).resolves.toEqual({ plain: true })
    expect(headersOf(lastInit(fetcher)).accept).toBe('application/json')
  })
})
