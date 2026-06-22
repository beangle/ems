import type { NAV_OPENMODE_AJAX, NAV_OPENMODE_IFRAME, NAV_OPENMODE_WUJIE } from './constants.js';

export type NavOpenMode =
  | typeof NAV_OPENMODE_AJAX
  | typeof NAV_OPENMODE_IFRAME
  | typeof NAV_OPENMODE_WUJIE;

export interface NavGroup {
  id: string | number;
  title: string;
  name?: string;
}

export interface NavApp {
  id?: string | number;
  name: string;
  title?: string;
  base?: string;
  url?: string;
  group?: NavGroup;
  navStyle?: string;
  openMode?: NavOpenMode | string;
  embeddable?: boolean;
}

export interface NavMenu {
  id?: string | number;
  title: string;
  entry?: string;
  target?: string;
  openMode?: NavOpenMode | string;
  fonticon?: string;
  children?: NavMenu[];
  navGroupId?: string | number;
  navAppId?: string | number;
  menus?: NavMenu[];
  app?: NavApp;
}

export interface AppMenus {
  app: NavApp;
  menus: NavMenu[];
}

export interface GroupMenus {
  group: NavGroup;
  appMenus: AppMenus[];
}

export interface DomainMenus {
  groups: GroupMenus[];
}

export interface NavProfile {
  id: string | number;
  name: string;
  url?: string;
  [key: string]: unknown;
}

export interface NavTheme {
  primaryColor: string;
  navbarBgColor: string;
  searchBgColor: string;
  gridbarBgColor: string;
  gridBorderColor: string;
}

export interface WorkspaceTab {
  id: string;
  url: string;
  title: string;
  openMode: NavOpenMode;
  navGroup?: string;
  navLink?: HTMLElement;
  panel?: HTMLElement;
  iframeEl?: HTMLIFrameElement;
  ajaxMainId?: string;
  appName?: string;
  seq?: number;
  homeTab?: boolean;
  pinned?: boolean;
  wujieStartOpts?: Record<string, unknown>;
}

/** 多标签工作台运行时状态（挂载在 nav.workspace，非源码模块 nav/workspace.ts） */
export interface NavWorkspace {
  rootId: string;
  scrollId: string;
  listId: string;
  bodyId: string;
  homeTabId: string;
  homeMainId: string;
  tabSeed: number;
  tabs: Record<string, WorkspaceTab>;
  tabByUrl: Record<string, string>;
  activeTabId: string | null;
  openSeq: number;
  maxTabCount: number;
}

/** sessionStorage（NAV_TABS_SESSION_KEY）中持久化的标签快照结构 */
export interface NavTabSnapshot {
  activeTabId: string | null;
  tabs: Array<{
    id: string;
    url: string;
    title?: string;
    openMode?: NavOpenMode | string;
    navGroup?: string;
    pinned?: boolean;
  }>;
}

export interface NavParams {
  menuDomId?: string;
  navDomId?: string;
  sysName?: string;
  maxTopItem?: number | string;
  initialGroupId?: string | number;
  multiTab?: boolean | string | number;
  [key: string]: unknown;
}

export interface SearchMenuResult {
  name: string;
  link: string;
  path: string[];
  target?: string;
  openMode?: NavOpenMode | string;
  navGroupAttr?: string;
}

export interface NavGroupAttr {
  appId: string;
  groupId: string;
}

/** Nav 运行时实例（侧栏 + 工作台）；方法见 nav/menu.ts、workspace.ts、tabs.ts */
export interface NavInstance {
  portal: NavApp;
  app: NavApp;
  apps: NavApp[];
  groups: NavGroup[];
  groupMenus: GroupMenus[];
  menuDomId: string;
  navDomId: string;
  sysName: string | null;
  params: Record<string, unknown>;
  maxTopItem: number;
  welcomeUrl: string | null;
  workspace: NavWorkspace;
  multiTab: boolean;
  initialGroupId: string | number | null;
  theme?: NavTheme;
  searchInputId?: string;
  _displayedGroupId?: string;
  _displayedAppId?: string;

  ensureNavTabHistoryListener(): void;
  addTopGroups(el: JQuery): void;
  bindTopGroupNav(): void;
  activate(): void;
  bindSearchMenuOpen(): void;
  displayGroupMenus(
    groupId: string | number,
    appId?: string | number,
    menuObj?: unknown,
    navOpts?: { highlightOnly?: boolean }
  ): void;
  setWelcomeUrl(url: string): void;
  openMenu(obj: unknown): boolean;
  toggleSearchResults(): void;
  closeSearchResults(): void;
  search(name: string, limit: number): void;
  activateNavTab(tabId: string, opts?: { skipHistory?: boolean; replaceState?: boolean }): void;
  teardownNavWorkspace(targetEle: HTMLElement | null): void;
  processUrl(url: string): string;

  [key: string]: unknown;
}
