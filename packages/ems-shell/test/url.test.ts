import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import {
  hostName,
  sameDomain,
  navUrlToPath,
  resolveNavAbsoluteUrl,
  urlsMatchForRouting,
} from '../src/js/url.js';

describe('url', () => {
  it('hostName extracts host from absolute URL', () => {
    assert.equal(hostName('https://portal.example.com:8443/app'), 'portal.example.com');
    assert.equal(hostName('http://local.openurp.net/portal'), 'local.openurp.net');
  });

  it('sameDomain compares hosts on absolute URLs', () => {
    assert.equal(
      sameDomain('http://local.openurp.net/portal', 'http://local.openurp.net/app'),
      true
    );
    assert.equal(
      sameDomain('http://a.example.com', 'http://b.example.com'),
      false
    );
  });

  it('navUrlToPath keeps pathname+search without origin', () => {
    assert.equal(
      navUrlToPath('http://local.openurp.net/portal/user/todo?x=1'),
      '/portal/user/todo?x=1'
    );
    assert.equal(navUrlToPath('#/portal/welcome'), '/portal/welcome');
  });

  it('resolveNavAbsoluteUrl resolves relative paths', () => {
    var abs = resolveNavAbsoluteUrl('/portal/welcome');
    assert.match(abs, /\/portal\/welcome$/);
  });

  it('urlsMatchForRouting ignores trailing slash differences', () => {
    assert.equal(
      urlsMatchForRouting('http://a/b/c', 'http://a/b/c/'),
      true
    );
  });
});
