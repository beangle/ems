import { describe, it, beforeEach } from 'node:test';
import assert from 'node:assert/strict';
import {
  clearAllLocalStorage,
  clearContextLocalStorage,
  loadThemeFromLocal,
  normalizeUiLocale,
  normalizeUiThemeMode,
  resolveMultiTabParam,
  getMultiTabPreference,
  getStoredLocale,
  getStoredThemeMode,
  saveThemeToLocal,
  setMultiTabPreference,
  setStoredLocaleAndNotify,
  setStoredThemeModeAndNotify,
} from '../src/js/storage.js';
import {
  EMS_CONTEXT_STORAGE_PREFIX,
  LOCALE_STORAGE_KEY,
  NAV_MULTI_TAB_STORAGE_KEY,
  THEME_MODE_STORAGE_KEY,
  THEME_STORAGE_KEY,
} from '../src/js/constants.js';

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

  it('normalizeUiLocale maps portal request_locale tags', () => {
    assert.equal(normalizeUiLocale('zh_CN'), 'zh-CN');
    assert.equal(normalizeUiLocale('en_US'), 'en-US');
    assert.equal(normalizeUiLocale('en'), 'en-US');
    assert.equal(normalizeUiLocale(''), null);
  });

  it('normalizeUiThemeMode maps light/dark nav styles', () => {
    assert.equal(normalizeUiThemeMode('light'), 'light');
    assert.equal(normalizeUiThemeMode('dark'), 'dark');
    assert.equal(normalizeUiThemeMode('--'), null);
  });

  it('setStoredThemeModeAndNotify writes beangle.ui.theme-mode only', () => {
    assert.equal(setStoredThemeModeAndNotify('dark'), 'dark');
    assert.equal(localStorage.getItem(THEME_MODE_STORAGE_KEY), 'dark');
    assert.equal(getStoredThemeMode(), 'dark');
    // Must not overwrite color JSON theme (beangle.ui.theme).
    assert.equal(localStorage.getItem(THEME_STORAGE_KEY), null);
  });

  it('setStoredLocaleAndNotify writes beangle.ui.locale', () => {
    assert.equal(setStoredLocaleAndNotify('zh_CN'), 'zh-CN');
    assert.equal(localStorage.getItem(LOCALE_STORAGE_KEY), 'zh-CN');
    assert.equal(getStoredLocale(), 'zh-CN');
    assert.equal(setStoredLocaleAndNotify('en_US'), 'en-US');
    assert.equal(localStorage.getItem(LOCALE_STORAGE_KEY), 'en-US');
  });
});
