import { describe, expect, it, vi } from 'vitest'
import { createEmsPermissions } from '../src/api/permissions'

const F = (body: unknown) =>
  vi.fn(async () => new Response(JSON.stringify(body), { status: 200, headers: { 'Content-Type': 'application/json' } }))

describe('createEmsPermissions', () => {
  it('扁平资源数组：/{studentId}/{ws} 按路径尾部识别路由与资源', async () => {
    const permissions = createEmsPermissions({
      permissionsPath: '/permissions.json',
      getApiUrl: (path) => path,
      fetcher: F([
        '/{studentId}/course-type-changes',
        '/{studentId}/courses',
        '/{studentId}/exams',
        '/{studentId}/exam-defers',
      ]),
    })
    await permissions.initialize()

    expect(permissions.canRoute('/courses')).toBe(true)
    expect(permissions.canRoute('/course-type-changes')).toBe(true)
    expect(permissions.canRoute('/exams')).toBe(true)
    expect(permissions.canRoute('/transcripts')).toBe(false)
    // 资源为全局授权：/exams 页面上的缓考按钮依赖独立资源 exam-defers
    expect(permissions.can('/exams', 'exam-defers')).toBe(true)
    expect(permissions.can('/exams', 'exams')).toBe(true)
    expect(permissions.can('/courses', 'courses')).toBe(true)
  })

  it('路由结构：can 按 route 资源集合判定，resources 同名可推导路由', async () => {
    const permissions = createEmsPermissions({
      permissionsPath: '/permissions.json',
      getApiUrl: (path) => path,
      fetcher: F([
        { route: '/programs', resources: ['/course-type-changes', '/programs', '/alternatives'] },
        { route: '/exams', resources: ['exams'] },
      ]),
    })
    await permissions.initialize()

    expect(permissions.canRoute('/programs')).toBe(true)
    expect(permissions.canRoute('/alternatives')).toBe(true)
    expect(permissions.canRoute('/course-type-changes')).toBe(true)
    expect(permissions.can('/programs', 'alternatives')).toBe(true)
    expect(permissions.can('/exams', 'exams')).toBe(true)
    expect(permissions.can('/exams', 'alternatives')).toBe(false)
  })

  it('resourcePrefix 作为防御性清理：带完整前缀的资源仍可匹配', async () => {
    const permissions = createEmsPermissions({
      permissionsPath: '/permissions.json',
      resourcePrefix: '/api/edu/learning',
      getApiUrl: (path) => path,
      fetcher: F(['/api/edu/learning/{studentId}/courses']),
    })
    await permissions.initialize()
    expect(permissions.canRoute('/courses')).toBe(true)
  })

  it('routeAliases 把子路由归一到权限路由', async () => {
    const permissions = createEmsPermissions({
      permissionsPath: '/permissions.json',
      routeAliases: [[/^\/clazz\//, '/courses']],
      getApiUrl: (path) => path,
      fetcher: F(['/{studentId}/courses']),
    })
    await permissions.initialize()
    expect(permissions.canRoute('/clazz/593811')).toBe(true)
  })

  it('grantAll（mock/调试）直接放行', async () => {
    const permissions = createEmsPermissions({
      permissionsPath: '/permissions.json',
      grantAll: true,
      getApiUrl: (path) => path,
    })
    await permissions.initialize()
    expect(permissions.canRoute('/anything')).toBe(true)
    expect(permissions.can('/anything', 'whatever')).toBe(true)
  })

  it('请求失败时 fail-open，不隐藏功能', async () => {
    const permissions = createEmsPermissions({
      permissionsPath: '/permissions.json',
      getApiUrl: (path) => path,
      fetcher: vi.fn(async () => new Response('', { status: 500 })),
    })
    await permissions.initialize()
    expect(permissions.canRoute('/courses')).toBe(true)
    expect(permissions.can('/exams', 'exams')).toBe(true)
  })

  it('401 时触发 requireLogin', async () => {
    const requireLogin = vi.fn()
    const permissions = createEmsPermissions({
      permissionsPath: '/permissions.json',
      getApiUrl: (path) => path,
      requireLogin,
      fetcher: vi.fn(async () => new Response('', { status: 401 })),
    })
    await permissions.initialize()
    expect(requireLogin).toHaveBeenCalled()
    expect(permissions.canRoute('/courses')).toBe(true) // fail-open
  })

  it('ensureLoaded 幂等，只加载一次', async () => {
    const fetcher = F(['/{studentId}/courses'])
    const permissions = createEmsPermissions({
      permissionsPath: '/permissions.json',
      getApiUrl: (path) => path,
      fetcher,
    })
    await permissions.ensureLoaded()
    await permissions.ensureLoaded()
    expect(fetcher).toHaveBeenCalledTimes(1)
  })
})
