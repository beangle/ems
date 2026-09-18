import { afterEach, describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { isWujieEntryRequest } from '../src/js/wujie.js';
import { tabsProto } from '../src/js/nav/tabs.js';

describe('isWujieEntryRequest', () => {
  const entry = 'http://localhost:8080/edu/learning/clazzes?_=1726620000000';

  it('入口文档及其强刷参数视为入口请求', () => {
    assert.equal(isWujieEntryRequest(entry, entry), true);
    assert.equal(
      isWujieEntryRequest('http://localhost:8080/edu/learning/clazzes', entry),
      true
    );
    assert.equal(
      isWujieEntryRequest('http://localhost:8080/edu/learning/clazzes?_=1#top', entry),
      true
    );
    assert.equal(isWujieEntryRequest('/edu/learning/clazzes', '/edu/learning/clazzes'), true);
  });

  it('Request 对象按其 url 判定', () => {
    assert.equal(
      isWujieEntryRequest({ url: 'http://localhost:8080/edu/learning/clazzes' }, entry),
      true
    );
    assert.equal(
      isWujieEntryRequest({ url: 'http://localhost:8080/api/edu/learning/1/clazzes/1.msgpack' }, entry),
      false
    );
  });

  it('子应用自身的接口请求与静态资源不算入口请求', () => {
    assert.equal(
      isWujieEntryRequest('http://localhost:8080/api/edu/learning/1/clazzes/1.msgpack', entry),
      false
    );
    assert.equal(
      isWujieEntryRequest('http://localhost:8080/edu/learning/assets/index-DzWVormp.js', entry),
      false
    );
    assert.equal(
      isWujieEntryRequest('http://api.example.com/edu/learning/clazzes', entry),
      false
    );
  });

  it('空值不误判为入口请求', () => {
    assert.equal(isWujieEntryRequest('', entry), false);
    assert.equal(isWujieEntryRequest(entry, ''), false);
    assert.equal(isWujieEntryRequest(undefined, entry), false);
    assert.equal(isWujieEntryRequest(null, null), false);
  });
});

/** 启动一个 tab 的 wujie 装载，返回壳层传给无界的 startApp 选项与底层 fetch 记录 */
async function startTab(params: Record<string, unknown>) {
  const g = globalThis as Record<string, unknown>;
  const calls: Array<{ url: unknown; init: Record<string, unknown> }> = [];
  const startOptsList: Array<Record<string, unknown>> = [];
  g.window = {
    location: { href: 'http://portal.example.com/index' },
    wujie: {
      startApp: (opts: Record<string, unknown>) => {
        startOptsList.push(opts);
        return Promise.resolve();
      },
      destroyApp: () => undefined,
    },
    dispatchEvent: () => true,
  };
  g.document = { body: { contains: () => true } };
  g.fetch = (url: unknown, init: Record<string, unknown>) => {
    calls.push({ url, init });
    return Promise.resolve({ ok: true });
  };
  const that = { params, workspace: { tabs: { t1: { seq: 1, panel: { innerHTML: '' } } } } };
  await tabsProto.startWujieAppForTab(
    that,
    't1',
    1,
    'app_x_tab_1',
    'http://app.example.com/edu/learning/clazzes'
  );
  return { startOpts: startOptsList[0], calls };
}

describe('startWujieAppForTab 的 fetch 策略', () => {
  afterEach(() => {
    const g = globalThis as Record<string, unknown>;
    delete g.window;
    delete g.document;
    delete g.fetch;
  });

  it('入口强刷只打入口文档，子应用请求保留自己的缓存策略', async () => {
    const { startOpts, calls } = await startTab({ wujieFetchNoStore: 'true' });
    assert.match(String(startOpts.url), /\?_=\d+/);
    const wujieFetch = startOpts.fetch as (input: unknown, init?: unknown) => Promise<unknown>;

    await wujieFetch('http://app.example.com/api/edu/learning/1/clazzes/1.msgpack', {
      headers: { Accept: 'application/x-msgpack' },
    });
    assert.equal(calls[0].init.cache, undefined);
    assert.equal(calls[0].init.credentials, 'include');
    assert.equal(calls[0].init.mode, 'cors');

    await wujieFetch(String(startOpts.url));
    assert.equal(calls[1].init.cache, 'no-store');
  });

  it('关闭入口强刷后入口文档也不加 no-store', async () => {
    const { startOpts, calls } = await startTab({});
    assert.doesNotMatch(String(startOpts.url), /_=/);
    const wujieFetch = startOpts.fetch as (input: unknown, init?: unknown) => Promise<unknown>;

    await wujieFetch(String(startOpts.url));
    await wujieFetch('http://app.example.com/edu/learning/assets/index-DzWVormp.js');
    assert.equal(calls[0].init.cache, undefined);
    assert.equal(calls[1].init.cache, undefined);
  });

  it('子应用显式传入的 cache 不再被覆盖', async () => {
    const { startOpts, calls } = await startTab({ wujieFetchNoStore: 'true' });
    const wujieFetch = startOpts.fetch as (input: unknown, init?: unknown) => Promise<unknown>;

    await wujieFetch('http://app.example.com/api/edu/learning/1/clazzes/1.msgpack', {
      cache: 'force-cache',
    });
    assert.equal(calls[0].init.cache, 'force-cache');
  });
});
