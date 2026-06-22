// @ts-nocheck
/**
 * 工作台 DOM 与 URL 路由 proto（workspaceProto → Nav.prototype）。
 *
 * 职责：创建/升级嵌入工作台容器，挂载 iframe/ajax 面板，首页标签，
 * URL 解析与标签键委托（见 tab-keys.ts、url.ts），快照与内容 URL 匹配。
 */
import { NAV_OPENMODE_AJAX, NAV_WORKSPACE_HOME_TAB_ID } from '../constants.js';
import {
  menuEntryToHostUrl as menuEntryToHostUrlFn,
  navUrlToPath as navUrlToPathFn,
  normalizeContentUrlKey as normalizeContentUrlKeyFn,
  resolveNavAbsoluteUrl as resolveNavAbsoluteUrlFn,
  urlsMatchForRouting as urlsMatchForRoutingFn,
} from '../url.js';
import {
  buildMicroAppName as buildMicroAppNameFn,
  buildNavTabDedupeKey,
  formatNavGroupAttr as formatNavGroupAttrFn,
  normalizeNavOpenMode as normalizeNavOpenModeFn,
  parseNavGroupAttr as parseNavGroupAttrFn,
} from '../tab-keys.js';

/** 工作台 DOM、URL 委托与路由匹配 */
export const workspaceProto = {
    // --- 工作台容器与挂载 ---
    /**
     * 保证嵌入导航工作台 DOM（多标签 + 工具栏 + 内容区）已挂载。
     * 若工作台根节点（this.workspace.rootId，如 #ems_nav_workspace）已存在：按需写入 session 快照、同步壳层可见性后返回该节点。
     * 否则：在 mainWrapper 下创建整块结构、可选恢复 targetEle 显示、绑定标签右键菜单一次，再持久化与同步。
     * @param {HTMLElement} mainWrapper 工作台根节点挂载的父元素
     * @param {HTMLElement|null} targetEle 与 iframe 并列的宿主区域（创建时会 display=""，一般为 #main）
     * @param {{skipPersist?:boolean}} [opts] skipPersist 为 true 时不调用 persistNavTabsSession
     */
    ensureNavWorkspace: function(mainWrapper, targetEle, opts) {
      opts = opts || {};
      var skipPersist = !!opts.skipPersist;
      var rootEl = document.getElementById(this.workspace.rootId);
      if (rootEl) {
        if (!skipPersist) this.persistNavTabsSession();
        this.syncNavWorkspaceChrome();
        var tabListExisting = document.getElementById(this.workspace.listId);
        if (tabListExisting) {
          tabListExisting.setAttribute("role", "tablist");
          tabListExisting.setAttribute("aria-orientation", "horizontal");
        }
        this.repairNavTabsA11y(this.workspace.activeTabId);
        this.bindWorkspaceTabListKeyboard();
        this.ensureWorkspaceHomeTab(targetEle);
        this.upgradeNavWorkspaceToolbar(rootEl);
        return rootEl;
      }
      var sideH = document.getElementById("main_siderbar");
      var minH = sideH ? sideH.offsetHeight + "px" : "480px";
      rootEl = document.createElement("div");
      rootEl.id = this.workspace.rootId;
      rootEl.className = "ems-nav-workspace";
      rootEl.style.display = "none";
      rootEl.style.width = "100%";
      rootEl.style.minHeight = minH;
      var cardCls = "card card-outline card-secondary ems-nav-card mb-0 border-top-0 rounded-0 shadow-none";
      var bodyCls = "card-body p-0 ems-nav-body";
      var bodyMinAttr = ' style="min-height:' + minH + ';"';
      rootEl.innerHTML = '<div class="' + cardCls + '"><div class="card-header ems-nav-toolbar p-0 d-flex align-items-stretch ems-nav-toolbar-strip"><div class="ems-nav-tabs-scroll overflow-auto" id="' + this.workspace.scrollId + '"><ul class="nav ems-nav-tabs ems-nav-tabs--workspace flex-nowrap" id="' + this.workspace.listId + '" role="tablist" aria-orientation="horizontal"></ul></div><div class="ems-nav-tab-actions"><div class="dropdown ems-nav-tab-actions-menu"><button type="button" class="ems-nav-tab-action-btn ems-nav-tab-more-btn" title="\u6807\u7B7E\u64CD\u4F5C" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><i class="fas fa-chevron-down" aria-hidden="true"></i></button><div class="dropdown-menu dropdown-menu-right" id="ems_nav_tab_actions_menu" role="menu"></div></div><button type="button" class="ems-nav-tab-action-btn ems-nav-tab-fullscreen-btn" title="\u5168\u5C4F\u663E\u793A"><i class="fas fa-expand" aria-hidden="true"></i></button></div></div><div class="' + bodyCls + '" id="' + this.workspace.bodyId + '"' + bodyMinAttr + "></div></div>";
      mainWrapper.appendChild(rootEl);
      if (targetEle) targetEle.style.display = "";
      var that = this;
      var tgtId = targetEle && targetEle.id ? targetEle.id : "main";
      rootEl.setAttribute("data-ems-nav-target", tgtId);
      if (!rootEl.getAttribute("data-ems-toolbar-bound")) {
        rootEl.setAttribute("data-ems-toolbar-bound", "1");
        that.bindWorkspaceTabToolbar(rootEl);
        that.bindWorkspaceTabContextMenu(rootEl);
      }
      if (!skipPersist) this.persistNavTabsSession();
      this.syncNavWorkspaceChrome();
      this.ensureWorkspaceHomeTab(targetEle);
      return rootEl;
    },

    upgradeNavWorkspaceToolbar: function(rootEl) {
      if (!rootEl) return;
      var strip = rootEl.querySelector(".ems-nav-toolbar-strip");
      if (!strip || strip.querySelector(".ems-nav-tab-actions")) return;
      var actionsHtml = '<div class="ems-nav-tab-actions"><div class="dropdown ems-nav-tab-actions-menu"><button type="button" class="ems-nav-tab-action-btn ems-nav-tab-more-btn" title="\u6807\u7B7E\u64CD\u4F5C" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><i class="fas fa-chevron-down" aria-hidden="true"></i></button><div class="dropdown-menu dropdown-menu-right" id="ems_nav_tab_actions_menu" role="menu"></div></div><button type="button" class="ems-nav-tab-action-btn ems-nav-tab-fullscreen-btn" title="\u5168\u5C4F\u663E\u793A"><i class="fas fa-expand" aria-hidden="true"></i></button></div>';
      strip.insertAdjacentHTML("beforeend", actionsHtml);
      this.bindWorkspaceTabToolbar(rootEl);
    },

    getNavWorkspaceTargetEl: function() {
      var w = document.getElementById(this.workspace.rootId);
      if (!w) return null;
      var tid = w.getAttribute("data-ems-nav-target");
      return tid ? document.getElementById(tid) : null;
    },
    /**
     * 在工作台标签栏与内容区追加一条标签 Shell DOM（li + panel 容器，不含 iframe/wujie 子应用内容）。
     * @param {string} tabId 标签 id（用于 aria-controls / panel id）
     * @param {{ prepend?: boolean, omitClose?: boolean, homeTab?: boolean }} [shellOpts]
     * @returns {{ navItem: HTMLElement, navLink: HTMLElement, panel: HTMLElement, closeBtn: HTMLElement|null }}
     */
    navTabButtonId: function(tabId) {
      return "ems_nav_tab_btn_" + String(tabId).replace(/[^a-zA-Z0-9_-]/g, "_");
    },

    navTabPanelId: function(tabId) {
      return "ems_nav_tab_panel_" + String(tabId).replace(/[^a-zA-Z0-9_-]/g, "_");
    },

    /** 绑定 tab / tabpanel 的 ARIA 关联（role 已在 DOM 创建时设置） */
    wireNavTabA11y: function(tabId, navLink, panel, title, isSelected) {
      if (!navLink || !panel) return;
      var btnId = this.navTabButtonId(tabId);
      var panelId = this.navTabPanelId(tabId);
      navLink.id = btnId;
      navLink.setAttribute("aria-controls", panelId);
      navLink.setAttribute("aria-selected", isSelected ? "true" : "false");
      navLink.setAttribute("tabindex", isSelected ? "0" : "-1");
      if (title) navLink.setAttribute("aria-label", title);
      panel.id = panelId;
      panel.setAttribute("role", "tabpanel");
      panel.setAttribute("aria-labelledby", btnId);
      panel.setAttribute("aria-hidden", isSelected ? "false" : "true");
    },

    appendWorkspaceTabShell: function(navBar, tabBody, tabId, title, absoluteUrl, shellOpts) {
      shellOpts = shellOpts || {};
      var navItem = document.createElement("li");
      navItem.className = "nav-item ems-nav-tab";
      if (shellOpts.homeTab) navItem.classList.add("ems-nav-tab--home");
      if (shellOpts.pinned || shellOpts.homeTab) navItem.classList.add("ems-nav-tab--pinned");
      var navLink = document.createElement("a");
      navLink.className = "nav-link ems-nav-tab-link d-flex align-items-center";
      navLink.href = "javascript:void(0)";
      navLink.setAttribute("role", "tab");
      var titleSpan = document.createElement("span");
      titleSpan.className = "ems-nav-tab-title text-truncate";
      titleSpan.textContent = title;
      titleSpan.title = title + "\n" + absoluteUrl;
      if (shellOpts.homeTab) {
        var homeIcon = document.createElement("i");
        homeIcon.className = "fas fa-home ems-nav-tab-icon flex-shrink-0";
        homeIcon.setAttribute("aria-hidden", "true");
        navLink.appendChild(homeIcon);
      }
      navLink.appendChild(titleSpan);
      var trailBtn = null;
      if (!shellOpts.omitClose) {
        trailBtn = document.createElement("button");
        trailBtn.type = "button";
        trailBtn.className = "btn btn-tool btn-sm ems-nav-tab-trail ems-nav-tab-close ml-1 flex-shrink-0";
        trailBtn.title = "\u5173\u95ED";
        trailBtn.setAttribute("aria-label", "\u5173\u95ED\u6807\u7B7E");
        trailBtn.innerHTML = "&times;";
        navLink.appendChild(trailBtn);
      }
      navItem.appendChild(navLink);
      if (shellOpts.prepend && navBar.firstChild) {
        navBar.insertBefore(navItem, navBar.firstChild);
      } else {
        navBar.appendChild(navItem);
      }
      var panel = document.createElement("div");
      panel.className = "ems-nav-tab-panel";
      if (shellOpts.homeTab) panel.classList.add("ems-nav-tab-panel--home");
      var sideBar = document.getElementById("main_siderbar");
      var minH = sideBar ? sideBar.offsetHeight + "px" : "480px";
      panel.style.minHeight = minH;
      panel.style.width = "100%";
      panel.style.display = "none";
      if (shellOpts.prepend && tabBody.firstChild) {
        tabBody.insertBefore(panel, tabBody.firstChild);
      } else {
        tabBody.appendChild(panel);
      }
      this.wireNavTabA11y(tabId, navLink, panel, title, false);
      return { navItem, navLink, panel, closeBtn: trailBtn };
    },
    /** 在 panel 内挂载 iframe 子页面（与快照恢复、菜单打开共用）。
     * tab.url / 标题不随 iframe 内跳转更新，见 docs/workspace-tabs.md */
    mountIframeInWorkspacePanel: function(panel, tabId, absoluteUrl) {
      var sideBar = document.getElementById("main_siderbar");
      var minH = sideBar ? sideBar.offsetHeight + "px" : "480px";
      var iframeEl = document.createElement("iframe");
      iframeEl.classList.add("autoadapt");
      iframeEl.setAttribute("width", "100%");
      iframeEl.setAttribute("height", "100%");
      iframeEl.setAttribute("scrolling", "auto");
      iframeEl.setAttribute("frameborder", "0");
      iframeEl.style.minHeight = minH;
      iframeEl.style.width = "100%";
      iframeEl.style.border = "0";
      iframeEl.setAttribute("name", tabId);
      iframeEl.src = absoluteUrl;
      panel.appendChild(iframeEl);
      return iframeEl;
    },

    buildWorkspaceAjaxMainId: function(tabId) {
      return "ems_ajax_" + String(tabId).replace(/[^a-zA-Z0-9_-]/g, "_");
    },
    /** 在工作台 panel 内挂载 ajax 片段区（与首页 welcome、菜单 ajax 共用）。 */
    mountAjaxInWorkspacePanel: function(panel, tabId, linkOrUrl) {
      var sideBar = document.getElementById("main_siderbar");
      var minH = sideBar ? sideBar.offsetHeight + "px" : "480px";
      var innerId = this.buildWorkspaceAjaxMainId(tabId);
      var inner = document.createElement("div");
      inner.id = innerId;
      inner.className = "ajax_container";
      inner.style.minHeight = minH;
      inner.style.width = "100%";
      panel.appendChild(inner);
      try {
        if (typeof linkOrUrl === "object" && linkOrUrl != null && linkOrUrl.tagName) {
          beangle.Go(linkOrUrl, innerId);
        } else if (linkOrUrl) {
          beangle.Go(linkOrUrl, innerId);
        }
      } catch (eGo) {
        if (window.console && console.warn) console.warn("[ems] mountAjaxInWorkspacePanel Go failed", eGo);
      }
      return inner;
    },

    // --- 首页标签与显隐 ---

    /**
     * 多标签 + 已配置 welcomeUrl：保证首枚「首页」标签存在（无关闭钮、ajax 加载 welcome），并插在列表最前。
     */
    ensureWorkspaceHomeTab: function(targetEle) {
      if (!this.multiTab || !this.welcomeUrl) return;
      if (this.workspace.tabs[this.workspace.homeTabId]) {
        this.ensureWorkspaceHomeTabIcon();
        return;
      }
      var navBar = document.getElementById(this.workspace.listId);
      var tabBody = document.getElementById(this.workspace.bodyId);
      if (!navBar || !tabBody) return;
      var te = targetEle || this.getNavWorkspaceTargetEl();
      var urlPath = this.navUrlToPath(this.welcomeUrl);
      var absoluteUrl = this.resolveNavAbsoluteUrl(urlPath);
      var shell2 = this.appendWorkspaceTabShell(navBar, tabBody, this.workspace.homeTabId, "\u9996\u9875", absoluteUrl, {
        prepend: true,
        omitClose: true,
        homeTab: true
      });
      var sideBar = document.getElementById("main_siderbar");
      var minH = sideBar ? sideBar.offsetHeight + "px" : "480px";
      var inner = document.createElement("div");
      inner.id = this.workspace.homeMainId;
      inner.className = "ajax_container";
      inner.style.minHeight = minH;
      inner.style.width = "100%";
      shell2.panel.appendChild(inner);
      var seq = ++this.workspace.openSeq;
      var tab = {
        id: this.workspace.homeTabId,
        url: urlPath,
        title: "\u9996\u9875",
        openMode: NAV_OPENMODE_AJAX,
        navLink: shell2.navLink,
        panel: shell2.panel,
        seq,
        homeTab: true,
        pinned: true
      };
      this.workspace.tabs[this.workspace.homeTabId] = tab;
      this.bindWorkspaceTabShellEvents(this.workspace.homeTabId, te, shell2.navLink, shell2.closeBtn);
      this.applyNavTabPinnedDom(this.workspace.homeTabId);
      try {
        if (typeof bg !== "undefined" && bg.Go) {
          bg.Go(this.welcomeUrl, this.workspace.homeMainId);
        } else if (typeof beangle !== "undefined" && beangle.Go) {
          beangle.Go(this.welcomeUrl, this.workspace.homeMainId);
        }
      } catch (eH) {
        if (window.console && console.warn) console.warn("[ems] ensureWorkspaceHomeTab Go failed", eH);
      }
    },

    /** 首页标签 navLink 补全 house 图标（DOM 升级场景） */
    ensureWorkspaceHomeTabIcon: function() {
      var tab = this.workspace.tabs[this.workspace.homeTabId];
      if (!tab || !tab.navLink) return;
      if (tab.navLink.querySelector(".ems-nav-tab-icon")) return;
      var homeIcon = document.createElement("i");
      homeIcon.className = "fas fa-home ems-nav-tab-icon flex-shrink-0";
      homeIcon.setAttribute("aria-hidden", "true");
      var titleSpan = tab.navLink.querySelector(".ems-nav-tab-title");
      if (titleSpan) {
        tab.navLink.insertBefore(homeIcon, titleSpan);
      } else {
        tab.navLink.insertBefore(homeIcon, tab.navLink.firstChild);
      }
    },

    /** 显示嵌入工作台、隐藏并列的 #main 宿主区 */
    showNavWorkspace: function(targetEle) {
      var rootEl = document.getElementById(this.workspace.rootId);
      if (rootEl) rootEl.style.display = "";
      if (targetEle) targetEle.style.display = "none";
    },

    /** 隐藏嵌入工作台、恢复 #main 宿主区 */
    hideNavWorkspace: function(targetEle) {
      var rootEl = document.getElementById(this.workspace.rootId);
      if (rootEl) rootEl.style.display = "none";
      if (targetEle) targetEle.style.display = "";
    },

    // --- 内容 URL 与快照匹配 ---

    /** 内存 tab 是否对应当前目标地址（同源：比对 tabUrl 与 hash 形式的 menuEntryToHostUrl(tabUrl)） */
    tabEntryMatchesContentUrl: function(targetHref, tabUrl) {
      if (!targetHref) return false;
      if (tabUrl && this.urlsMatchForRouting(targetHref, tabUrl)) return true;
      var menuHostUrl = tabUrl ? this.menuEntryToHostUrl(tabUrl) : null;
      if (menuHostUrl) {
        if (this.urlsMatchForRouting(targetHref, menuHostUrl)) return true;
        try {
          var hashIdx = menuHostUrl.indexOf("#");
          if (hashIdx >= 0) {
            var frag = menuHostUrl.substring(hashIdx + 1);
            if (frag.startsWith("/")) {
              var synthetic = new URL(frag, window.location.origin).href;
              if (this.urlsMatchForRouting(targetHref, synthetic)) return true;
            }
          }
        } catch (eH) {
        }
      }
      return false;
    },
    /** preferredOpenMode：与当前菜单 openMode 一致（wujie|iframe），混合挂载时避免同 URL 串到另一类标签 */
    findSnapshotTabIdForUrl: function(snapshot, targetHref, preferredOpenMode) {
      if (!snapshot || !snapshot.tabs || !targetHref) return null;
      var tabs = snapshot.tabs;
      var want = preferredOpenMode != null && preferredOpenMode !== "" ? this.normalizeNavOpenMode(preferredOpenMode) : null;
      for (var i = 0; i < tabs.length; i++) {
        var s = tabs[i];
        if (!s || !s.id) continue;
        if (want != null && this.normalizeNavOpenMode(s.openMode) !== want) continue;
        if (this.tabEntryMatchesContentUrl(targetHref, s.url)) return s.id;
      }
      return null;
    },

    // --- URL/标签键委托（薄封装 → tab-keys.ts、url.ts）---

    normalizeNavOpenMode: function (k) {
      return normalizeNavOpenModeFn(k);
    },
    navTabDedupeKey: function (urlPath, openMode) {
      return buildNavTabDedupeKey(String(urlPath ?? ''), openMode);
    },
    formatNavGroupAttr: function (appId, groupId) {
      return formatNavGroupAttrFn(appId, groupId);
    },
    parseNavGroupAttr: function (raw) {
      return parseNavGroupAttrFn(raw);
    },
    buildMicroAppName: function (appId, tabId) {
      return buildMicroAppNameFn(appId, tabId);
    },
    menuEntryToHostUrl: function (entryHref) {
      return menuEntryToHostUrlFn(entryHref);
    },
    navUrlToPath: function (href) {
      return navUrlToPathFn(href);
    },
    resolveNavAbsoluteUrl: function (pathOrUrl) {
      return resolveNavAbsoluteUrlFn(pathOrUrl);
    },
    normalizeContentUrlKey: function (href) {
      return normalizeContentUrlKeyFn(href);
    },
    urlsMatchForRouting: function (a, b) {
      return urlsMatchForRoutingFn(a, b);
    }
};
