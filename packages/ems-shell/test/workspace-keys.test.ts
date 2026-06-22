import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import {
  normalizeNavOpenMode,
  buildNavTabDedupeKey,
  formatNavGroupAttr,
  parseNavGroupAttr,
  buildMicroAppName,
} from '../src/js/tab-keys.js';
import { NAV_OPENMODE_AJAX, NAV_OPENMODE_IFRAME, NAV_OPENMODE_WUJIE } from '../src/js/constants.js';

describe('workspace keys', () => {
  it('normalizeNavOpenMode maps unknown to wujie', () => {
    assert.equal(normalizeNavOpenMode(NAV_OPENMODE_IFRAME), NAV_OPENMODE_IFRAME);
    assert.equal(normalizeNavOpenMode(NAV_OPENMODE_AJAX), NAV_OPENMODE_AJAX);
    assert.equal(normalizeNavOpenMode('other'), NAV_OPENMODE_WUJIE);
  });

  it('buildNavTabDedupeKey combines path and mode', () => {
    assert.equal(
      buildNavTabDedupeKey('/portal/a', NAV_OPENMODE_IFRAME),
      '/portal/a\niframe'
    );
  });

  it('formatNavGroupAttr and parseNavGroupAttr round-trip', () => {
    var raw = formatNavGroupAttr('12', '34');
    assert.equal(raw, '12@34');
    assert.deepEqual(parseNavGroupAttr(raw), { appId: '12', groupId: '34' });
    assert.deepEqual(parseNavGroupAttr('onlyApp'), { appId: 'onlyApp', groupId: '' });
  });

  it('buildMicroAppName sanitizes ids', () => {
    assert.equal(buildMicroAppName('7', 'ems_tab_3'), 'app_7_tab_3');
  });
});
