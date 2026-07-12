import { describe, it, beforeEach } from 'node:test';
import assert from 'node:assert/strict';
import {
  clearAllLocalStorage,
  clearContextLocalStorage,
  loadThemeFromLocal,
  resolveMultiTabParam,
  getMultiTabPreference,
  saveThemeToLocal,
  setMultiTabPreference,
} from '../src/js/storage.js';
import { EMS_CONTEXT_STORAGE_PREFIX, NAV_MULTI_TAB_STORAGE_KEY, THEME_STORAGE_KEY } from '../src/js/constants.js';

describe('storage preferences', () => {
  beforeEach(() => {
    global.localStorage = {
      _data: {},
      get length() {
        return Object.keys(this._data).length;
      },
      key(i) {
        return Object.keys(this._data)[i] ?? null;
      },
      getItem(k) {
        return this._data[k] ?? null;
      },
      setItem(k, v) {
        this._data[k] = String(v);
      },
      removeItem(k) {
        delete this._data[k];
      },
      clear() {
        this._data = {};
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

  it('clearContextLocalStorage removes only beangle.ems.context.* keys', () => {
    localStorage.setItem(EMS_CONTEXT_STORAGE_PREFIX + 'edu.semester', '1');
    localStorage.setItem(EMS_CONTEXT_STORAGE_PREFIX + 'portal.search', 'x');
    localStorage.setItem(THEME_STORAGE_KEY, '{}');
    localStorage.setItem('other.app', 'keep');
    clearContextLocalStorage();
    assert.equal(localStorage.getItem(EMS_CONTEXT_STORAGE_PREFIX + 'edu.semester'), null);
    assert.equal(localStorage.getItem(EMS_CONTEXT_STORAGE_PREFIX + 'portal.search'), null);
    assert.equal(localStorage.getItem(THEME_STORAGE_KEY), '{}');
    assert.equal(localStorage.getItem('other.app'), 'keep');
  });

  it('clearAllLocalStorage clears entire storage', () => {
    localStorage.setItem(THEME_STORAGE_KEY, '{}');
    localStorage.setItem('other.app', 'x');
    clearAllLocalStorage();
    assert.equal(localStorage.getItem(THEME_STORAGE_KEY), null);
    assert.equal(localStorage.getItem('other.app'), null);
  });

  it('saveThemeToLocal and loadThemeFromLocal use beangle.ui.theme', () => {
    const fallback = {
      primaryColor: '#111',
      navbarBgColor: '#222',
      searchBgColor: '#333',
      gridbarBgColor: '#444',
      gridBorderColor: '#555',
    };
    saveThemeToLocal({
      primaryColor: '#0076ff',
      navbarBgColor: '#001',
      searchBgColor: '#002',
      gridbarBgColor: '#003',
      gridBorderColor: '#004',
    });
    assert.equal(localStorage.getItem(THEME_STORAGE_KEY), JSON.stringify({
      primaryColor: '#0076ff',
      navbarBgColor: '#001',
      searchBgColor: '#002',
      gridbarBgColor: '#003',
      gridBorderColor: '#004',
    }));
    const loaded = loadThemeFromLocal(fallback);
    assert.equal(loaded.primaryColor, '#0076ff');
    assert.equal(loaded.gridBorderColor, '#004');
  });
});
