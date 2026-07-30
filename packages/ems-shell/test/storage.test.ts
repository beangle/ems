import { describe, it, beforeEach } from 'node:test';
import assert from 'node:assert/strict';
import {
  clearAllLocalStorage,
  clearContextLocalStorage,
  fontSizeToCss,
  loadThemeFromLocal,
  normalizeUiFontSize,
  normalizeUiLocale,
  normalizeUiThemeMode,
  resolveMultiTabParam,
  getMultiTabPreference,
  getStoredFontSize,
  getStoredLocale,
  getStoredThemeMode,
  saveThemeToLocal,
  setMultiTabPreference,
  setStoredFontSizeAndNotify,
  setStoredLocaleAndNotify,
  setStoredThemeModeAndNotify,
} from '../src/js/storage.js';
import {
  EMS_CONTEXT_STORAGE_PREFIX,
  FONT_SIZE_STORAGE_KEY,
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

  it('clearAllLocalStorage removes non-ui keys but keeps beangle.ui.*', () => {
    localStorage.setItem(THEME_STORAGE_KEY, '{}');
    localStorage.setItem(LOCALE_STORAGE_KEY, 'zh-CN');
    localStorage.setItem(FONT_SIZE_STORAGE_KEY, 'large');
    localStorage.setItem(THEME_MODE_STORAGE_KEY, 'dark');
    localStorage.setItem('beangle.ems.multi_tab', '1');
    localStorage.setItem('other.app', 'x');
    clearAllLocalStorage();
    assert.equal(localStorage.getItem(THEME_STORAGE_KEY), '{}');
    assert.equal(localStorage.getItem(LOCALE_STORAGE_KEY), 'zh-CN');
    assert.equal(localStorage.getItem(FONT_SIZE_STORAGE_KEY), 'large');
    assert.equal(localStorage.getItem(THEME_MODE_STORAGE_KEY), 'dark');
    assert.equal(localStorage.getItem('beangle.ems.multi_tab'), null);
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

  it('normalizeUiFontSize maps semantic and legacy CSS', () => {
    assert.equal(normalizeUiFontSize('small'), 'small');
    assert.equal(normalizeUiFontSize('medium'), 'medium');
    assert.equal(normalizeUiFontSize('large'), 'large');
    assert.equal(normalizeUiFontSize('sm'), 'small');
    assert.equal(normalizeUiFontSize('0.9286em'), 'small');
    assert.equal(normalizeUiFontSize('1em'), 'medium');
    assert.equal(normalizeUiFontSize('1.07143em'), 'large');
    assert.equal(normalizeUiFontSize('--'), null);
  });

  it('fontSizeToCss maps portal root sizes', () => {
    assert.equal(fontSizeToCss('small'), '0.9286em');
    assert.equal(fontSizeToCss('medium'), '1em');
    assert.equal(fontSizeToCss('large'), '1.07143em');
  });

  it('setStoredFontSizeAndNotify writes beangle.ui.font-size only', () => {
    assert.equal(setStoredFontSizeAndNotify('large'), 'large');
    assert.equal(localStorage.getItem(FONT_SIZE_STORAGE_KEY), 'large');
    assert.equal(getStoredFontSize(), 'large');
    // Must not overwrite color JSON theme (beangle.ui.theme).
    assert.equal(localStorage.getItem(THEME_STORAGE_KEY), null);
    assert.equal(localStorage.getItem('beangle.ems.root_font_size'), null);
  });
});
