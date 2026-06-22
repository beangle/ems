import type { EmsConfig } from './config.js';
import { config, init } from './config.js';
import { hostName, sameDomain } from './url.js';
import { resolveWujieRuntime } from './wujie.js';
import {
  getMultiTabPreference,
  resolveMultiTabParam,
  setMultiTabPreference,
} from './storage.js';
import { createNav, changeGroup, nav } from './nav/factory.js';
import {
  createProfileNav,
  fetchMessages,
  setup,
  enableSearch,
  openMenu,
  changeNavSidebarTheme,
  changeFontSize,
  changeTheme,
  clearNavState,
} from './shell.js';

export interface EmsShellApi {
  config: EmsConfig;
  hostName: typeof hostName;
  sameDomain: typeof sameDomain;
  init: typeof init;
  createNav: typeof createNav;
  changeGroup: typeof changeGroup;
  createProfileNav: typeof createProfileNav;
  fetchMessages: typeof fetchMessages;
  setup: typeof setup;
  enableSearch: typeof enableSearch;
  openMenu: typeof openMenu;
  changeNavSidebarTheme: typeof changeNavSidebarTheme;
  changeFontSize: typeof changeFontSize;
  clearNavState: typeof clearNavState;
  setWelcomeUrl: (url: string) => void;
  ensureWujieRuntime: typeof resolveWujieRuntime;
  getNav: () => typeof nav;
  changeTheme: typeof changeTheme;
  getMultiTabPreference: typeof getMultiTabPreference;
  setMultiTabPreference: typeof setMultiTabPreference;
  resolveMultiTabParam: typeof resolveMultiTabParam;
  messageCallBack: (count: string | number) => void;
  taskCallBack: (count: string | number) => void;
}

export function createApi(): EmsShellApi {
  return {
    config,
    hostName,
    sameDomain,
    init,
    createNav,
    changeGroup,
    createProfileNav,
    fetchMessages,
    setup,
    enableSearch,
    openMenu,
    changeNavSidebarTheme,
    changeFontSize,
    clearNavState,
    setWelcomeUrl(url: string) {
      nav.setWelcomeUrl(url);
    },
    ensureWujieRuntime: resolveWujieRuntime,
    getNav() {
      return nav;
    },
    changeTheme,
    getMultiTabPreference,
    setMultiTabPreference,
    resolveMultiTabParam,
    messageCallBack(c) {
      jQuery('#newly-message-count').text(String(c));
    },
    taskCallBack(c) {
      jQuery('#newly-task-count').text(String(c));
    },
  };
}

export type { NavProfile, NavTheme, NavOpenMode, DomainMenus, NavApp } from './types.js';
