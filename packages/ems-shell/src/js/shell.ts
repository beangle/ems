/**
 * 门户壳层：布局、主题、消息、个人资料切换与 restoreNav 入口。
 * 业务导航逻辑委托 nav（factory 单例）上的 Nav 实例方法。
 */
import { NAV_STICKY_HEADER_STORAGE_KEY } from './constants.js';
import { config } from './config.js';
import {
  clearAllLocalStorage,
  clearContextLocalStorage,
  clearThemeFromLocal,
  fontSizeToCss,
  getLocal,
  getMultiTabPreference,
  getStoredFontSize,
  getStoredThemeMode,
  loadThemeFromLocal,
  saveThemeToLocal,
  setMultiTabPreference,
  setStoredFontSizeAndNotify,
  setStoredLocaleAndNotify,
  setStoredThemeModeAndNotify,
} from './storage.js';
import { applyThemeVars } from './theme.js';
import { sameDomain } from './url.js';
import { nav } from './nav/factory.js';
import type { NavInstance, NavTheme } from './types.js';
import {
  closeMobileSidebar,
  ensureMobileHeaderBrand,
  ensureMobileSidebarOverlay,
  isMobileSidebarLayout,
  shellWrapper,
} from './layout.js';

// --- 布局 ---

export { closeMobileSidebar, isMobileSidebarLayout, openMobileSidebar } from './layout.js';

export function shell() {
  return shellWrapper();
}

/** 以顶栏实测高度更新 --ems-header-height，使 brand-link 与 main-header 对齐 */
export function syncBrandHeaderHeight(): void {
  const header = document.getElementById('main_header');
  if (!header) return;
  document.documentElement.style.setProperty('--ems-header-height', header.offsetHeight + 'px');
}

export function initShellLayout() {
  ensureMobileSidebarOverlay();
  ensureMobileHeaderBrand();
  jQuery(document).off('click.emsShell');
  jQuery(document).on('click.emsShell', '[data-ems-pushmenu]', function (e) {
    e.preventDefault();
    if (isMobileSidebarLayout()) {
      ensureMobileSidebarOverlay();
      shell().toggleClass('sidebar-open');
    } else {
      shell().toggleClass('sidebar-collapse');
    }
  });
  jQuery(document).on('click.emsShell', '.main-header', function (e) {
    if (!isMobileSidebarLayout() || !shell().hasClass('sidebar-open')) return;
    const target = e.target;
    if (!(target instanceof Element)) return;
    if (target.closest('[data-ems-pushmenu]') || target.closest('.main-sidebar')) return;
    closeMobileSidebar();
  });
  jQuery(window).off('resize.emsShell').on('resize.emsShell', function () {
    if (!isMobileSidebarLayout()) {
      shell().removeClass('sidebar-open');
    }
  });
  jQuery(document).on('click.emsShell', '[data-ems-control-sidebar]', function (e) {
    e.preventDefault();
    shell().toggleClass('control-sidebar-slide-open');
  });
  jQuery(document).on('click.emsShell', '[data-ems-fullscreen]', function (e) {
    e.preventDefault();
    const docEl = document.documentElement;
    if (!document.fullscreenElement && docEl.requestFullscreen) {
      docEl.requestFullscreen();
    } else if (document.exitFullscreen) {
      document.exitFullscreen();
    }
  });
  jQuery(document).on('click.emsShell', '.control-sidebar-bg', function (e) {
    e.preventDefault();
    shell().removeClass('control-sidebar-slide-open');
  });
}

/** 顶栏月亮/太阳图标：浅白显示月亮（切暗黑），暗黑显示太阳（切浅白） */
function syncThemeToggleIcon(mode: string): void {
  const icon = mode === 'dark' ? 'fas fa-sun' : 'fas fa-moon';
  const title = mode === 'dark' ? '切换浅白' : '切换暗黑';
  jQuery('[data-ems-theme-toggle]').attr('title', title).find('i').attr('class', icon);
}

/** 切换侧栏浅白/暗黑（顶栏月亮/太阳按钮） */
export function toggleNavSidebarTheme(): void {
  const current = getStoredThemeMode() || 'light';
  changeNavSidebarTheme(current === 'dark' ? 'light' : 'dark');
}

export function applyStickyHeader(enabled: boolean): void {
  if (enabled) {
    shell().addClass('layout-navbar-fixed');
  } else {
    shell().removeClass('layout-navbar-fixed');
  }
}

// --- 站内消息 ---

export function fetchMessages(params: Record<string, string>): void {
  if (!sameDomain(window.location.href, params['webapp'])) {
    return;
  }
  jQuery.ajax({
    url: params['webapp'] + '/portal/user/message/newly?callback=emsShell.messageCallBack',
    cache: false,
    type: 'GET',
    dataType: 'html',
    complete(jqXHR) {
      try {
        jQuery('#newly-message').html(jqXHR.responseText);
      } catch (e) {
        alert(e);
      }
    },
  });
  jQuery.ajax({
    url: params['webapp'] + '/portal/user/todo/newly?callback=emsShell.taskCallBack',
    cache: false,
    type: 'GET',
    dataType: 'html',
    complete(jqXHR) {
      try {
        jQuery('#newly-task').html(jqXHR.responseText);
      } catch (e) {
        alert(e);
      }
    },
  });
}

// --- 个人资料 ---

export function createProfileNav(): void {
  const profiles = config.profiles;
  if (profiles.length === 0 || !config.profile) return;

  const profile = config.profile;
  const host = jQuery('.main-header > .ml-auto');
  if (profiles.length === 1) {
    // 仅一个 profile：显示名称，不可切换
    host.prepend(
      '<li class="nav-item">' +
        '<span class="nav-link" id="profile_switcher" title="当前业务场景">' +
        String(profile.name) +
        '</span>' +
        '</li>'
    );
    return;
  }

  const profileSelectTemplate =
    '<li class="nav-item dropdown">' +
    '<a class="dropdown-toggle nav-link" data-toggle="dropdown" href="#" id="profile_switcher" aria-expanded="false">{first}</a> ' +
    '<div class="dropdown-menu">{list}</div>' +
    '</li>';
  const profileTemplate = '<a href="{profile.url}" class="dropdown-item">{profile.name}</a>';
  let profilehtml = profileSelectTemplate.replace('{first}', String(profile.name));
  let list = '';
  for (let i = 0; i < profiles.length; i++) {
    if (profiles[i].id != profile.id) {
      let profileItem = profileTemplate.replace('{profile.url}', String(profiles[i].url ?? ''));
      profileItem = profileItem.replace('{profile.name}', String(profiles[i].name));
      list += profileItem;
    }
  }
  profilehtml = profilehtml.replace('{list}', list);
  host.prepend(profilehtml);
  jQuery('#profile_switcher')
    .parent()
    .find('.dropdown-item')
    .on('click', () => {
      clearContextLocalStorage();
    });
}

// --- 主题 ---

/**
 * 导航风格（侧栏浅白/暗黑）。写入 beangle.ui.theme-mode，更新门户侧栏 DOM，并通知无界子应用。
 */
export function changeNavSidebarTheme(theme: string): void {
  if (theme === '--') return;
  const mode = setStoredThemeModeAndNotify(theme);
  if (!mode) return;
  jQuery('#nav_siderbar_theme_' + mode).prop('checked', true);
  const root = document.documentElement;
  root.setAttribute('data-theme', mode);
  root.setAttribute('data-bs-theme', mode);
  if (mode === 'dark') {
    root.classList.add('theme-dark');
    root.classList.remove('theme-light');
    jQuery('#main_siderbar').removeClass('sidebar-light-lightblue').addClass('sidebar-dark-primary');
    jQuery('#control_sidebar').removeClass('control-sidebar-light').addClass('control-sidebar-dark');
  } else {
    root.classList.add('theme-light');
    root.classList.remove('theme-dark');
    jQuery('#main_siderbar').removeClass('sidebar-dark-primary').addClass('sidebar-light-lightblue');
    jQuery('#control_sidebar').removeClass('control-sidebar-dark').addClass('control-sidebar-light');
  }
  syncThemeToggleIcon(mode);
}

/**
 * 界面语言切换：写入 beangle.ui.locale 并通知子应用；调用方再执行 ?request_locale= 跳转。
 */
export function changeLocale(locale: string): void {
  setStoredLocaleAndNotify(locale);
}

/**
 * 更改根字体大小。接受语义档位 small|medium|large，或侧栏 radio 的 CSS 值；
 * 写入 beangle.ui.font-size 并通知微应用。
 */
export function changeFontSize(fontSize: string): void {
  if (fontSize === '--') return;
  const mode = setStoredFontSizeAndNotify(fontSize);
  if (!mode) return;
  const css = fontSizeToCss(mode);
  jQuery('#control_sidebar input[name=root_font_size]').each(function (_i, a) {
    if (jQuery(a).val() == css) jQuery(a).prop('checked', true);
  });
  document.documentElement.style.setProperty('font-size', css);
  syncBrandHeaderHeight();
}

export function changeTheme(theme?: NavTheme | string | null): void {
  if (theme) {
    const parsed = typeof theme === 'string' ? (JSON.parse(theme) as NavTheme) : theme;
    applyTheme(parsed);
    saveThemeToLocal(parsed);
  } else if (nav.theme) {
    applyTheme(nav.theme);
    clearThemeFromLocal();
  }
}

export function applyTheme(theme: NavTheme | string): void {
  const t = typeof theme === 'string' ? (JSON.parse(theme) as NavTheme) : theme;
  applyThemeVars(t);
  jQuery('#theme_primaryColor').val(t.primaryColor);
  jQuery('#theme_navbarBgColor').val(t.navbarBgColor);
  jQuery('#theme_searchBgColor').val(t.searchBgColor);
  jQuery('#theme_gridbarBgColor').val(t.gridbarBgColor);
  jQuery('#theme_gridHeaderBgColor').val(t.gridHeaderBgColor);
  jQuery('#theme_gridBorderColor').val(t.gridBorderColor);
}

export { applyStoredThemeIfPresent, applyThemeVars } from './theme.js';

// --- 初始化 ---

export function setup(theme: NavTheme, params: Record<string, string>): void {
  nav.theme = theme;
  initShellLayout();
  shell().addClass('sidebar-mini layout-fixed');
  jQuery('body').addClass('text-sm');
  document.documentElement.style.setProperty('scrollbar-width', 'thin');
  fetchMessages(params);
  const stickyHeader = getLocal(NAV_STICKY_HEADER_STORAGE_KEY, '1');
  if (stickyHeader == '1') {
    applyStickyHeader(true);
    jQuery('#sticky_header').prop('checked', true);
  }

  jQuery('#control_sidebar input[name=root_font_size]').on('click', function (e) {
    changeFontSize(String(jQuery(e.target as HTMLElement).val()));
  });
  jQuery('#sticky_header').on('click', function (this: HTMLInputElement) {
    applyStickyHeader(!!this.checked);
    if (localStorage) localStorage.setItem(NAV_STICKY_HEADER_STORAGE_KEY, this.checked ? '1' : '0');
  });

  const multiTabPref = getMultiTabPreference();
  jQuery('#nav_multi_tab').prop('checked', multiTabPref);
  jQuery('#nav_multi_tab').on('click', function (this: HTMLInputElement) {
    setMultiTabPreference(!!this.checked);
  });

  changeNavSidebarTheme(getStoredThemeMode() || 'light');
  changeFontSize(getStoredFontSize() || 'medium');
  // 服务端主题优先：navContext 传入的 theme 直接覆盖本地缓存；
  // 仅当服务端无主题时才回退到 localStorage 中保存的主题
  const hasServerTheme = !!theme && Object.values(theme).some(v => !!v);
  const resolvedTheme = hasServerTheme ? theme : loadThemeFromLocal(theme);
  applyTheme(resolvedTheme);
  saveThemeToLocal(resolvedTheme);

  const pageSize = beangle.cookie?.get('pageSize');
  if (pageSize) jQuery('#page_size_selector').val(pageSize);
  jQuery('#page_size_selector').on('change', function (this: HTMLSelectElement) {
    beangle.cookie?.set('pageSize', this.value, '/', 10 * 365);
  });
  syncBrandHeaderHeight();
  jQuery(document).ready(restoreNav);
}

export function enableSearch(searchInputId: string): void {
  nav.searchInputId = searchInputId;
  const searchDom = jQuery('#' + searchInputId).parent().parent();
  searchDom.show();
  searchDom.removeClass('sidebar-search-open');
  searchDom.find('.input-group-append .btn').click(function (event) {
    event.preventDefault();
    nav.toggleSearchResults();
  });
  jQuery(document).on('keyup', '#' + searchInputId, function () {
    setTimeout(function () {
      const searchValue = String(jQuery('#' + nav.searchInputId!).val()).toLowerCase();
      if (!searchValue || searchValue.length < 2) {
        nav.closeSearchResults();
      } else {
        nav.search(searchValue, 7);
      }
    }, 100);
  });
}

export function openMenu(obj: unknown): boolean {
  return nav.openMenu(obj);
}

// --- 导航恢复 ---

/** 导航恢复：阶段一 phase1ResolvePortalBootstrap；阶段二渲染侧边菜单与工作台多标签。 */
export function restoreNav(): void {
  const n = nav as Record<string, (...args: unknown[]) => unknown> & NavInstance;
  if (!nav || typeof n.phase1ResolvePortalBootstrap !== 'function') {
    return;
  }
  const boot = n.phase1ResolvePortalBootstrap() as {
    initMenuLoc?: { app?: { app?: { id?: string | number }; id?: string | number }; menu?: { openMode?: string } };
    menuHref?: string;
    menuHighlightOnly?: boolean;
    initialGroupId: string | number;
    initialAppId?: string | number | null;
    navTabsSnapshot: { activeTabId: string | null; tabs: unknown[] };
  };

  function phase2Render(): void {
    let openAppId: string | number | undefined;
    if (boot.initMenuLoc) {
      const ae =
        boot.initMenuLoc.app && boot.initMenuLoc.app.app
          ? boot.initMenuLoc.app.app
          : boot.initMenuLoc.app;
      openAppId = ae && ae.id != null ? ae.id : undefined;
    } else if (boot.initialAppId != null && boot.initialAppId !== '') {
      openAppId = boot.initialAppId;
    }
    let menuObj = boot.initMenuLoc ? boot.initMenuLoc.menu : null;
    if (
      boot.navTabsSnapshot.tabs.length > 0 &&
      menuObj &&
      menuObj.openMode === 'ajax' &&
      !boot.menuHighlightOnly
    ) {
      menuObj = null;
    }
    const hlOnly = !!(
      boot.menuHighlightOnly &&
      menuObj &&
      (menuObj.openMode === 'wujie' ||
        menuObj.openMode === 'iframe' ||
        menuObj.openMode === 'ajax')
    );

    function syncHashHistory(): void {
      if (!boot.menuHref || !boot.navTabsSnapshot.activeTabId) return;
      const tid = boot.navTabsSnapshot.activeTabId;
      if (!nav.workspace.tabs[tid]) return;
      const hostU = n.menuEntryToHostUrl(boot.menuHref) as string | null | undefined;
      if (hostU) {
        try {
          window.history.replaceState({ emsNavTab: tid }, '', hostU);
        } catch {
          /* ignore */
        }
      }
    }

    function finishSidebar(): void {
      nav.displayGroupMenus(boot.initialGroupId, openAppId, menuObj, { highlightOnly: hlOnly });
      syncHashHistory();
    }

    const snap0 = boot.navTabsSnapshot;
    if (
      snap0.tabs.length > 0 ||
      (nav.multiTab && snap0.activeTabId === nav.workspace.homeTabId)
    ) {
      const rp = n.restoreNavTabsFromSession(snap0, {
        preferredTabId: snap0.activeTabId,
        skipSidebarInFinalize: true,
      });
      Promise.resolve(rp).then(finishSidebar).catch(function (eRn) {
        if (typeof console !== 'undefined' && console.warn) console.warn('[ems] restoreNav phase2', eRn);
        finishSidebar();
      });
    } else {
      if (nav.groups?.length > 0) {
        nav.displayGroupMenus(boot.initialGroupId, openAppId, menuObj, { highlightOnly: false });
      }
      if (nav.welcomeUrl && !(boot.menuHref && boot.initMenuLoc)) {
        if (nav.multiTab) {
          const mainEl0 = document.getElementById('main');
          if (mainEl0?.parentNode) {
            n.ensureNavWorkspace(mainEl0.parentNode as HTMLElement, mainEl0, { skipPersist: true });
            n.showNavWorkspace(mainEl0);
            if (nav.workspace.tabs[nav.workspace.homeTabId]) {
              n.activateNavTab(nav.workspace.homeTabId, { replaceState: true });
            }
          }
        } else {
          bg.Go(nav.welcomeUrl, 'main');
        }
      }
    }
  }

  phase2Render();
}

export function clearNavState(): void {
  nav.teardownNavWorkspace(document.getElementById('main'));
  sessionStorage.clear();
}

/** 退出登录：清工作台会话 + sessionStorage；localStorage 清除但保留 beangle.ui.* */
export function clearNavStateOnLogout(): void {
  clearNavState();
  clearAllLocalStorage();
}
