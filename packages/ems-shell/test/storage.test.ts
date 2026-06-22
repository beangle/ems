import { describe, it, beforeEach } from 'node:test';
import assert from 'node:assert/strict';
import {
  resolveMultiTabParam,
  getMultiTabPreference,
  setMultiTabPreference,
} from '../src/js/storage.js';
import { NAV_MULTI_TAB_STORAGE_KEY } from '../src/js/constants.js';

describe('storage preferences', () => {
  beforeEach(() => {
    global.localStorage = {
      _data: {},
      getItem(k) {
        return this._data[k] ?? null;
      },
      setItem(k, v) {
        this._data[k] = String(v);
      },
      removeItem(k) {
        delete this._data[k];
      },
    };
  });

  it('resolveMultiTabParam prefers explicit server value', () => {
    assert.equal(resolveMultiTabParam('false'), 'false');
    assert.equal(resolveMultiTabParam('true'), 'true');
    assert.equal(resolveMultiTabParam('0'), 'false');
  });

  it('resolveMultiTabParam falls back to localStorage', () => {
    setMultiTabPreference(false);
    assert.equal(resolveMultiTabParam(undefined), 'false');
    setMultiTabPreference(true);
    assert.equal(resolveMultiTabParam(null), 'true');
  });

  it('getMultiTabPreference defaults to true', () => {
    assert.equal(getMultiTabPreference(), true);
    localStorage.setItem(NAV_MULTI_TAB_STORAGE_KEY, '0');
    assert.equal(getMultiTabPreference(), false);
  });
});
