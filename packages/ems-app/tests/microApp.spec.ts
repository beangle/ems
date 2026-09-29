import { afterEach, describe, expect, it, vi } from 'vitest'

describe('microApp', () => {
  afterEach(() => {
    vi.resetModules()
    window.history.replaceState({}, '', '/')
    delete window.__POWERED_BY_WUJIE__
  })

  it('模块加载时带 micro=1 即生效，即使之后查询串被清掉', async () => {
    window.history.replaceState({}, '', '/edu/learning/courses?micro=1')
    const { isWujieMicroApp } = await import('../src/utils/microApp')
    // 模拟应用初始化用 replaceState 清掉一次性参数
    window.history.replaceState({}, '', '/edu/learning/courses')
    expect(window.location.search).toBe('')
    expect(isWujieMicroApp()).toBe(true)
  })

  it('没有 micro=1 时为 false，加载后再跳转到 micro=1 也能识别', async () => {
    window.history.replaceState({}, '', '/edu/learning/courses')
    const { isWujieMicroApp } = await import('../src/utils/microApp')
    expect(isWujieMicroApp()).toBe(false)
    window.history.replaceState({}, '', '/edu/learning/courses?micro=1')
    expect(isWujieMicroApp()).toBe(true)
  })

  it('micro 值不是 1 时不算微前端', async () => {
    window.history.replaceState({}, '', '/edu/learning/courses?micro=0')
    const { isWujieMicroApp } = await import('../src/utils/microApp')
    expect(isWujieMicroApp()).toBe(false)
  })

  it('真实 Wujie 环境下为 true', async () => {
    window.history.replaceState({}, '', '/edu/learning/courses')
    window.__POWERED_BY_WUJIE__ = true
    const { isWujieMicroApp } = await import('../src/utils/microApp')
    expect(isWujieMicroApp()).toBe(true)
  })
})
