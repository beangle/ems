// @ts-nocheck
/**
 * 多标签工作台 proto（tabsProto → Nav.prototype）。
 *
 * 职责：标签增删改、session 快照、浏览器历史、无界子应用、
 * 刷新恢复（phase1/phase2）、菜单 openMenu 入口。
 */
import {
  NAV_OPENMODE_AJAX,
  NAV_OPENMODE_IFRAME,
  NAV_OPENMODE_WUJIE,
  NAV_TABS_SESSION_KEY,
  NAV_WORKSPACE_HOME_TAB_ID,
} from '../constants.js';
import { resolveWujieRuntime } from '../wujie.js';

/** 多标签生命周期、恢复、打开菜单、无界挂载 */
export const tabsProto = {

    // --- 标签数量与 session 快照 ---

    /** 当前内存中标签总数（含首页） */
    workspaceTabCount: function() {
      return Object.keys(this.workspace.tabs).length;
    },
    /** multiTab=false 时 session 快照只保留一条（优先 activeTabId，否则首条） */
    trimNavTabsSnapshotIfSingleMode: function(data) {
      if (!data || !Array.isArray(data.tabs)) return false;
      if (this.multiTab || data.tabs.length <= 1) return false;
      var keep = null;
      if (data.activeTabId) {
        for (var i = 0; i < data.tabs.length; i++) {
          var row = data.tabs[i];
          if (row && row.id === data.activeTabId) {
            keep = row;
            break;
          }
        }
      }
      if (!keep) keep = data.tabs[0];
      data.tabs = [keep];
      data.activeTabId = keep.id;
      return true;
    },

    /** 按 multiTab 切换工作台根节点 CSS 类（单标签模式隐藏标签栏样式） */
    syncNavWorkspaceChrome: function() {
      var w = document.getElementById(this.workspace.rootId);
      if (!w) return;
      if (this.multiTab) {
        w.classList.remove("ems-nav--single-tab-mode");
      } else {
        w.classList.add("ems-nav--single-tab-mode");
      }
    },

    /** 工作台内容区高度对齐视口剩余空间（避免仅 min-height 导致下方空白、iframe 过小） */
    syncNavWorkspaceLayout: function() {
      var root = document.getElementById(this.workspace.rootId);
      if (!root || root.style.display === "none") return;
      var top = root.getBoundingClientRect().top;
      if (!(top >= 0)) top = 0;
      var avail = Math.max(320, window.innerHeight - top);
      root.style.minHeight = avail + "px";
    },

    /** 标签项滚入横向滚动区视口；已在视口内或无法滚动时不操作（behavior: auto，无 smooth 动画） */
    scrollNavTabIntoView: function(tabLi) {
      if (!tabLi || !tabLi.scrollIntoView) return;
      var scrollEl = document.getElementById(this.workspace.scrollId);
      if (scrollEl) {
        var liRect = tabLi.getBoundingClientRect();
        var scrollRect = scrollEl.getBoundingClientRect();
        if (liRect.left >= scrollRect.left && liRect.right <= scrollRect.right) return;
      }
      try {
        tabLi.scrollIntoView({ block: "nearest", inline: "nearest", behavior: "auto" });
      } catch (err) {
        tabLi.scrollIntoView(false);
      }
    },

    // --- 标签容量与溢出腾位 ---

    /** 标签栏最右一个 tab 的 id（溢出腾位）；DOM 对不上时用 seq 最大兜底。
     * @param {Array=} tabIdsPre 调用方已得到的 Object.keys(this.workspace.tabs)，可与外层共用避免重复遍历。 */
    pickLastNavTabIdForOverflowReplace: function(tabIdsPre) {
      var tabIds = tabIdsPre && tabIdsPre.length !== void 0 ? tabIdsPre : Object.keys(this.workspace.tabs);
      tabIds = tabIds.filter(function(id3) {
        var t0 = this.workspace.tabs[id3];
        return id3 !== NAV_WORKSPACE_HOME_TAB_ID && !this.isNavTabPinned(t0);
      }.bind(this));
      var navBar = document.getElementById(this.workspace.listId);
      if (navBar) {
        var lis = navBar.querySelectorAll("li.ems-nav-tab");
        for (var liIdx = lis.length - 1; liIdx >= 0; liIdx--) {
          var liPick = lis[liIdx];
          var tid = liPick.getAttribute("data-ems-tab-id");
          if (tid && tabIds.indexOf(tid) !== -1) return tid;
        }
      }
      var bestId = null;
      var bestSeq = -1;
      for (var si = 0; si < tabIds.length; si++) {
        var id2 = tabIds[si];
        var t2 = this.workspace.tabs[id2];
        var s = t2.seq != null ? t2.seq : 0;
        if (s >= bestSeq) {
          bestSeq = s;
          bestId = id2;
        }
      }
      return bestId;
    },
    /** 新开前已满则关掉最右标签 */
    ensureRoomForNewNavTab: function(targetEle) {
      if (!this.multiTab) return;
      var cap = this.workspace.maxTabCount;
      if (!(typeof cap === "number") || isNaN(cap) || cap < 1) return;
      var tabIdsEr = Object.keys(this.workspace.tabs).filter(function(id) {
        return id !== NAV_WORKSPACE_HOME_TAB_ID;
      });
      if (tabIdsEr.length < cap) return;
      var victim = this.pickLastNavTabIdForOverflowReplace(tabIdsEr);
      if (victim) this.closeNavTab(victim, targetEle || this.getNavWorkspaceTargetEl());
    },
    /** 恢复后若仍超过上限则反复去掉最右标签 */
    trimNavTabsDownToMaxCount: function(targetEle) {
      if (!this.multiTab) return;
      var cap = this.workspace.maxTabCount;
      if (!(typeof cap === "number") || isNaN(cap) || cap < 1) return;
      var te = targetEle || this.getNavWorkspaceTargetEl();
      var homeId = this.workspace.homeTabId;
      var guard = 0;
      while (guard < 128) {
        var tabIdsTrim = Object.keys(this.workspace.tabs);
        var embedOnly = tabIdsTrim.filter(function(id) {
          return id !== homeId;
        }).length;
        if (embedOnly <= cap) break;
        guard += 1;
        var victim = this.pickLastNavTabIdForOverflowReplace(tabIdsTrim);
        if (!victim) break;
        this.closeNavTab(victim, te);
      }
    },

    /** 关闭除 keepTabId 与固定标签外的所有标签 */
    closeOtherNavTabs: function(keepTabId, targetEle) {
      var te = targetEle || this.getNavWorkspaceTargetEl();
      var ids = Object.keys(this.workspace.tabs).filter(function(id) {
        return id !== keepTabId && !this.isNavTabPinned(this.workspace.tabs[id]);
      }.bind(this));
      for (var i = 0; i < ids.length; i++) this.closeNavTab(ids[i], te);
    },
    /** multiTab=false：下次打开 iframe/wujie 前清空嵌入实例与去重表，便于三种 openMode 切换 */
    clearNavTabsForSingleMenuSwitch: function(targetEle) {
      var ids = Object.keys(this.workspace.tabs);
      for (var i = 0; i < ids.length; i++) {
        this.closeNavTab(ids[i], targetEle);
      }
      this.workspace.tabByUrl = {};
      this.workspace.tabSeed = 0;
      if (targetEle) this.showNavWorkspace(targetEle);
    },

    // --- 标签创建、去重与刷新 ---

    buildNavTabId: function(url, openMode) {
      var path = this.navUrlToPath(url);
      var key = this.navTabDedupeKey(path, openMode != null ? openMode : NAV_OPENMODE_WUJIE);
      var reuseId = this.workspace.tabByUrl[key];
      if (reuseId && this.workspace.tabs[reuseId]) return reuseId;
      if (reuseId && !this.workspace.tabs[reuseId]) delete this.workspace.tabByUrl[key];
      this.workspace.tabSeed += 1;
      var tabId = "ems_tab_" + this.workspace.tabSeed;
      this.workspace.tabByUrl[key] = tabId;
      return tabId;
    },

    getNavTabTitle: function(obj, fallback) {
      if (typeof obj == "object" && obj.tagName && obj.tagName.toLowerCase() == "a") {
        var titleEle = obj.querySelector("p");
        if (titleEle && titleEle.textContent) return titleEle.textContent.trim();
        var searchTitle = obj.querySelector(".search-title");
        if (searchTitle && searchTitle.textContent) return searchTitle.textContent.trim();
      }
      return fallback || "\u5FAE\u524D\u7AEF";
    },
    /**
     * 工作台标签去重命中时已存在的 tab：激活并返回 true。
     */
    tryActivateExistingWorkspaceTab: function(urlPath, embedOpenMode) {
      var dedupe = this.navTabDedupeKey(urlPath, embedOpenMode);
      var existingId = this.workspace.tabByUrl[dedupe];
      if (existingId && !this.workspace.tabs[existingId]) {
        delete this.workspace.tabByUrl[dedupe];
        existingId = null;
      }
      if (existingId && this.workspace.tabs[existingId]) {
        this.activateNavTab(existingId);
        return true;
      }
      return false;
    },
    /**
     * 新开工作台标签前腾出槽位并分配 tabId；navBar/tabBody 未就绪时返回 null。
     */
    prepareNewWorkspaceTabSlot: function(targetEle, urlPath, embedOpenMode) {
      if (!this.multiTab) {
        this.clearNavTabsForSingleMenuSwitch(targetEle);
      } else {
        this.ensureRoomForNewNavTab(targetEle);
      }
      var tabId = this.buildNavTabId(urlPath, embedOpenMode);
      var navBar = document.getElementById(this.workspace.listId);
      var tabBody = document.getElementById(this.workspace.bodyId);
      if (!navBar || !tabBody) return null;
      return {
        navBar,
        tabBody,
        tabId,
        absoluteUrl: this.resolveNavAbsoluteUrl(urlPath)
      };
    },
    /** 刷新工作台标签内容：用打开标签时记录的 tab.url 重新加载 */
    refreshNavTab: function(tabId) {
      var tab = this.workspace.tabs[tabId];
      if (!tab || !tab.url) return;
      var url = this.resolveNavAbsoluteUrl(tab.url);
      var openMode = tab.openMode || NAV_OPENMODE_IFRAME;
      if (openMode === NAV_OPENMODE_IFRAME) {
        if (tab.iframeEl) {
          tab.iframeEl.src = url;
        } else if (tab.panel) {
          tab.panel.innerHTML = "";
          tab.iframeEl = this.mountIframeInWorkspacePanel(tab.panel, tabId, url);
        }
      } else if (openMode === NAV_OPENMODE_AJAX) {
        var targetId = tab.ajaxMainId || (tab.homeTab ? this.workspace.homeMainId : null);
        if (targetId) {
          try {
            if (typeof bg !== "undefined" && bg.Go) {
              bg.Go(url, targetId);
            } else if (typeof beangle !== "undefined" && beangle.Go) {
              beangle.Go(url, targetId);
            }
          } catch (eGo) {
            if (window.console && console.warn) console.warn("[ems] refreshNavTab Go failed", eGo);
          }
        }
      } else if (openMode === NAV_OPENMODE_WUJIE && tab.appName) {
        var seq = ++this.workspace.openSeq;
        tab.seq = seq;
        this.startWujieAppForTab(this, tabId, seq, tab.appName, url);
      }
      this.activateNavTab(tabId, { skipHistory: true });
    },

    // --- 标签栏 UI 与交互 ---

    /** 首页或用户固定的标签不可关闭、不参与溢出腾位 */
    isNavTabPinned: function(tabOrId) {
      var tab = typeof tabOrId === "string" ? this.workspace.tabs[tabOrId] : tabOrId;
      if (!tab) return false;
      return !!(tab.homeTab || tab.pinned);
    },

    applyNavTabPinnedDom: function(tabId) {
      var tab = this.workspace.tabs[tabId];
      if (!tab || !tab.navLink || tab.homeTab) return;
      var li = tab.navLink.parentElement;
      var pinned = this.isNavTabPinned(tab);
      if (li) li.classList.toggle("ems-nav-tab--pinned", pinned);
      var trailBtn = tab.navLink.querySelector(".ems-nav-tab-trail");
      if (!trailBtn) return;
      if (pinned) {
        trailBtn.className = "btn btn-tool btn-sm ems-nav-tab-trail ems-nav-tab-pin ml-1 flex-shrink-0";
        trailBtn.title = "\u53D6\u6D88\u56FA\u5B9A";
        trailBtn.setAttribute("aria-label", "\u53D6\u6D88\u56FA\u5B9A");
        trailBtn.innerHTML = '<i class="fas fa-thumbtack" aria-hidden="true"></i>';
      } else {
        trailBtn.className = "btn btn-tool btn-sm ems-nav-tab-trail ems-nav-tab-close ml-1 flex-shrink-0";
        trailBtn.title = "\u5173\u95ED";
        trailBtn.setAttribute("aria-label", "\u5173\u95ED\u6807\u7B7E");
        trailBtn.innerHTML = "&times;";
      }
    },

    /** 标签条 DOM 顺序：首页 → 已固定 → 未固定（各组内按 seq） */
    syncNavTabsDomOrder: function() {
      var navBar = document.getElementById(this.workspace.listId);
      var tabBody = document.getElementById(this.workspace.bodyId);
      if (!navBar || !tabBody) return;
      var homeId = this.workspace.homeTabId;
      var pinned = [];
      var unpinned = [];
      var tabIds = Object.keys(this.workspace.tabs);
      for (var i = 0; i < tabIds.length; i++) {
        var id = tabIds[i];
        if (id === homeId) continue;
        var t = this.workspace.tabs[id];
        if (!t) continue;
        if (this.isNavTabPinned(t)) pinned.push(t);
        else unpinned.push(t);
      }
      var bySeq = function(a, b) {
        return (a.seq != null ? a.seq : 0) - (b.seq != null ? b.seq : 0);
      };
      pinned.sort(bySeq);
      unpinned.sort(bySeq);
      var ordered = pinned.concat(unpinned);
      var homeTab = this.workspace.tabs[homeId];
      var anchorLi = null;
      var anchorPanel = null;
      if (homeTab && homeTab.navLink) {
        var homeLi = homeTab.navLink.parentElement;
        if (homeLi && navBar.firstElementChild !== homeLi) {
          navBar.insertBefore(homeLi, navBar.firstElementChild);
        }
        anchorLi = homeLi;
        if (homeTab.panel) {
          if (tabBody.firstElementChild !== homeTab.panel) {
            tabBody.insertBefore(homeTab.panel, tabBody.firstElementChild);
          }
          anchorPanel = homeTab.panel;
        }
      }
      for (var j = 0; j < ordered.length; j++) {
        var tab = ordered[j];
        var li = tab.navLink && tab.navLink.parentElement;
        if (li) {
          if (anchorLi) {
            var nextLi = anchorLi.nextElementSibling;
            if (li !== nextLi) navBar.insertBefore(li, nextLi);
          } else {
            navBar.insertBefore(li, navBar.firstElementChild);
          }
          anchorLi = li;
        }
        if (tab.panel) {
          if (anchorPanel) {
            var nextPanel = anchorPanel.nextElementSibling;
            if (tab.panel !== nextPanel) tabBody.insertBefore(tab.panel, nextPanel);
          } else {
            tabBody.insertBefore(tab.panel, tabBody.firstElementChild);
          }
          anchorPanel = tab.panel;
        }
      }
      if (this.workspace.activeTabId) {
        this.syncNavTabsA11y(this.workspace.activeTabId);
      }
    },

    toggleNavTabPinned: function(tabId) {
      if (tabId === this.workspace.homeTabId) return;
      var tab = this.workspace.tabs[tabId];
      if (!tab) return;
      tab.pinned = !tab.pinned;
      this.applyNavTabPinnedDom(tabId);
      this.syncNavTabsDomOrder();
      this.persistNavTabsSession();
      var ctxMenu = document.getElementById("ems_nav_tab_context_menu");
      if (ctxMenu) this.syncNavTabActionMenu(ctxMenu, tabId);
      var actionsMenu = document.getElementById("ems_nav_tab_actions_menu");
      if (actionsMenu) this.syncNavTabActionMenu(actionsMenu, tabId);
    },

    navTabActionMenuHtml: function() {
      return '<a href="#" class="dropdown-item ems-nav-ctx-refresh" role="menuitem" tabindex="-1"><i class="fa fa-sync-alt fa-fw" aria-hidden="true"></i>\u5237\u65B0</a><a href="#" class="dropdown-item ems-nav-ctx-open-new" role="menuitem" tabindex="-1"><i class="fa fa-external-link-alt fa-fw" aria-hidden="true"></i>\u5728\u65B0\u6807\u7B7E\u6253\u5F00</a><a href="#" class="dropdown-item ems-nav-ctx-pin" role="menuitem" tabindex="-1"><i class="fa fa-thumbtack fa-fw" aria-hidden="true"></i><span class="ems-nav-ctx-pin-label">\u56FA\u5B9A\u6807\u7B7E</span></a><div class="dropdown-divider"></div><a href="#" class="dropdown-item ems-nav-ctx-close" role="menuitem" tabindex="-1"><i class="fa fa-times fa-fw" aria-hidden="true"></i>\u5173\u95ED</a><a href="#" class="dropdown-item ems-nav-ctx-close-others" role="menuitem" tabindex="-1"><i class="fa fa-minus-circle fa-fw" aria-hidden="true"></i>\u5173\u95ED\u5176\u4ED6</a>';
    },

    syncNavTabActionMenu: function(menuEl, tabId) {
      if (!menuEl) return;
      menuEl.setAttribute("data-ems-tab-id", tabId || "");
      var tab = tabId ? this.workspace.tabs[tabId] : null;
      var isHome = tabId === NAV_WORKSPACE_HOME_TAB_ID;
      var pinned = tab ? this.isNavTabPinned(tab) : false;
      var pinItem = menuEl.querySelector(".ems-nav-ctx-pin");
      var pinLabel = menuEl.querySelector(".ems-nav-ctx-pin-label");
      var closeItem = menuEl.querySelector(".ems-nav-ctx-close");
      var closeOthersItem = menuEl.querySelector(".ems-nav-ctx-close-others");
      var otherCount = tabId ? Object.keys(this.workspace.tabs).filter(function(id) {
        return id !== tabId && !this.isNavTabPinned(this.workspace.tabs[id]);
      }.bind(this)).length : 0;
      if (pinItem) pinItem.style.display = !tabId || isHome ? "none" : "";
      if (pinLabel) pinLabel.textContent = pinned ? "\u53D6\u6D88\u56FA\u5B9A" : "\u56FA\u5B9A\u6807\u7B7E";
      if (closeItem) closeItem.style.display = !tabId || pinned ? "none" : "";
      if (closeOthersItem) closeOthersItem.style.display = otherCount > 0 ? "" : "none";
    },

    handleNavTabActionClick: function(item, tabId) {
      if (!item || !tabId) return;
      var te = this.getNavWorkspaceTargetEl();
      if (item.classList.contains("ems-nav-ctx-refresh")) {
        this.refreshNavTab(tabId);
      } else if (item.classList.contains("ems-nav-ctx-open-new")) {
        var tab = this.workspace.tabs[tabId];
        if (tab && tab.url) {
          var openUrl = this.resolveNavAbsoluteUrl(tab.url);
          if (openUrl) window.open(openUrl, "_blank", "noopener,noreferrer");
        }
      } else if (item.classList.contains("ems-nav-ctx-pin")) {
        this.toggleNavTabPinned(tabId);
      } else if (item.classList.contains("ems-nav-ctx-close-others")) {
        this.closeOtherNavTabs(tabId, te);
      } else if (item.classList.contains("ems-nav-ctx-close") && !this.isNavTabPinned(tabId)) {
        this.closeNavTab(tabId, te);
      }
    },

    bindNavTabActionMenu: function(menuEl, navSelf, opts) {
      opts = opts || {};
      if (!menuEl || menuEl.getAttribute("data-ems-action-bound")) return;
      menuEl.setAttribute("data-ems-action-bound", "1");
      menuEl.innerHTML = navSelf.navTabActionMenuHtml();
      menuEl.addEventListener("click", function(e) {
        var item = e.target.closest && e.target.closest(".dropdown-item");
        if (!item) return;
        e.preventDefault();
        var tabId = menuEl.getAttribute("data-ems-tab-id") || navSelf.workspace.activeTabId;
        navSelf.handleNavTabActionClick(item, tabId);
        if (opts.hideOnClick) {
          menuEl.style.display = "none";
          menuEl.removeAttribute("data-ems-tab-id");
        }
      });
    },

    bindWorkspaceTabToolbar: function(rootEl) {
      var navSelf = this;
      if (rootEl && rootEl.getAttribute("data-ems-tab-toolbar-bound")) return;
      if (rootEl) rootEl.setAttribute("data-ems-tab-toolbar-bound", "1");
      var actionsMenu = document.getElementById("ems_nav_tab_actions_menu");
      if (actionsMenu) {
        navSelf.bindNavTabActionMenu(actionsMenu, navSelf, { hideOnClick: false });
        jQuery(actionsMenu).closest(".ems-nav-tab-actions-menu").on("show.bs.dropdown", function() {
          var tabId = navSelf.workspace.activeTabId;
          navSelf.syncNavTabActionMenu(actionsMenu, tabId);
        });
      }
      var fsBtn = rootEl && rootEl.querySelector(".ems-nav-tab-fullscreen-btn");
      if (fsBtn && !fsBtn.getAttribute("data-ems-fs-bound")) {
        fsBtn.setAttribute("data-ems-fs-bound", "1");
        fsBtn.addEventListener("click", function(e) {
          e.preventDefault();
          var wsEl = document.getElementById(navSelf.workspace.rootId);
          if (!wsEl) return;
          if (!document.fullscreenElement) {
            var req = wsEl.requestFullscreen || wsEl.webkitRequestFullscreen;
            if (req) req.call(wsEl);
          } else if (document.fullscreenElement === wsEl && document.exitFullscreen) {
            document.exitFullscreen();
          }
        });
      }
      if (!window.__emsTabFullscreenIconBound) {
        window.__emsTabFullscreenIconBound = true;
        document.addEventListener("fullscreenchange", function() {
          var wsEl = document.getElementById(navSelf.workspace.rootId);
          var icon = document.querySelector(".ems-nav-tab-fullscreen-btn i");
          if (!icon) return;
          if (document.fullscreenElement && wsEl && document.fullscreenElement === wsEl) {
            icon.classList.remove("fa-expand");
            icon.classList.add("fa-compress");
            wsEl.classList.add("ems-nav-workspace--fullscreen");
          } else {
            icon.classList.remove("fa-compress");
            icon.classList.add("fa-expand");
            if (wsEl) wsEl.classList.remove("ems-nav-workspace--fullscreen");
          }
        });
      }
      navSelf.bindWorkspaceTabListKeyboard();
      if (!window.__emsNavWorkspaceLayoutBound) {
        window.__emsNavWorkspaceLayoutBound = true;
        var layoutTimer = null;
        window.addEventListener("resize", function() {
          if (layoutTimer) clearTimeout(layoutTimer);
          layoutTimer = setTimeout(function() {
            navSelf.syncNavWorkspaceLayout();
          }, 100);
        });
      }
      navSelf.syncNavWorkspaceLayout();
    },

    /** 标签栏方向键切换、Enter/Space 激活（WAI-ARIA tabs 模式） */
    listNavTabIdsInDomOrder: function() {
      var navBar = document.getElementById(this.workspace.listId);
      if (!navBar) return [];
      var lis = navBar.querySelectorAll("li.ems-nav-tab[data-ems-tab-id]");
      var ids = [];
      for (var i = 0; i < lis.length; i++) {
        var id = lis[i].getAttribute("data-ems-tab-id");
        if (id && this.workspace.tabs[id]) ids.push(id);
      }
      return ids;
    },

    syncNavTabsA11y: function(activeTabId) {
      var tabIds = Object.keys(this.workspace.tabs);
      for (var i = 0; i < tabIds.length; i++) {
        var id = tabIds[i];
        var t = this.workspace.tabs[id];
        if (!t || !t.navLink || !t.panel) continue;
        var on = id === activeTabId;
        if (!t.navLink.id || !t.panel.id) {
          this.wireNavTabA11y(id, t.navLink, t.panel, t.title || "", on);
        } else {
          t.navLink.setAttribute("aria-selected", on ? "true" : "false");
          t.navLink.setAttribute("tabindex", on ? "0" : "-1");
          t.panel.setAttribute("aria-hidden", on ? "false" : "true");
        }
      }
    },

    /** 为已存在 DOM 的标签补全 ARIA（升级/恢复场景） */
    repairNavTabsA11y: function(activeTabId) {
      this.syncNavTabsA11y(activeTabId);
    },

    /** 键盘浏览：仅移动焦点（manual activation），不写 history */
    focusNavTabButton: function(tabId) {
      var order = this.listNavTabIdsInDomOrder();
      for (var i = 0; i < order.length; i++) {
        var id = order[i];
        var link = this.workspace.tabs[id] && this.workspace.tabs[id].navLink;
        if (!link) continue;
        link.setAttribute("tabindex", id === tabId ? "0" : "-1");
      }
      var t = this.workspace.tabs[tabId];
      if (t && t.navLink && t.navLink.focus) t.navLink.focus();
    },

    bindWorkspaceTabListKeyboard: function() {
      var navSelf = this;
      var tabList = document.getElementById(this.workspace.listId);
      if (!tabList || tabList.getAttribute("data-ems-tab-kbd-bound")) return;
      tabList.setAttribute("data-ems-tab-kbd-bound", "1");
      tabList.addEventListener("keydown", function(e) {
        var key = e.key;
        if (key !== "ArrowLeft" && key !== "ArrowRight" && key !== "Home" && key !== "End" && key !== "Enter" && key !== " ") return;
        var tabEl = e.target.closest && e.target.closest('[role="tab"]');
        if (!tabEl || !tabList.contains(tabEl)) return;
        e.preventDefault();
        var li = tabEl.closest && tabEl.closest("li.ems-nav-tab");
        var tabIdFromTarget = li && li.getAttribute("data-ems-tab-id");
        var order = navSelf.listNavTabIdsInDomOrder();
        if (order.length === 0) return;
        var idx = tabIdFromTarget ? order.indexOf(tabIdFromTarget) : order.indexOf(navSelf.workspace.activeTabId);
        if (idx < 0) idx = 0;
        /* Enter/Space：手动激活，才切换内容与 pushState（方向键仅 roving focus，避免刷 history） */
        if (key === "Enter" || key === " ") {
          if (tabIdFromTarget && tabIdFromTarget !== navSelf.workspace.activeTabId) {
            navSelf.activateNavTab(tabIdFromTarget);
          }
          return;
        }
        var nextIdx = idx;
        if (key === "ArrowLeft") nextIdx = idx > 0 ? idx - 1 : order.length - 1;
        else if (key === "ArrowRight") nextIdx = idx < order.length - 1 ? idx + 1 : 0;
        else if (key === "Home") nextIdx = 0;
        else if (key === "End") nextIdx = order.length - 1;
        var nextId = order[nextIdx];
        if (nextId) navSelf.focusNavTabButton(nextId);
      });
    },

    openNavTabContextMenu: function(menu, tabId, clientX, clientY) {
      if (!menu || !tabId || !this.workspace.tabs[tabId]) return;
      menu.setAttribute("data-ems-tab-id", tabId);
      this.syncNavTabActionMenu(menu, tabId);
      this.activateNavTab(tabId, { skipHistory: true });
      menu.style.display = "block";
      menu.style.left = clientX + "px";
      menu.style.top = clientY + "px";
    },

    /** 工作台标签条：右键菜单（刷新 / 在新标签打开 / 关闭 / 关闭其他） */
    bindWorkspaceTabContextMenu: function(rootEl) {
      var navSelf = this;
      if (rootEl && rootEl.getAttribute("data-ems-tab-ctx-bound")) {
        return;
      }
      if (rootEl) rootEl.setAttribute("data-ems-tab-ctx-bound", "1");
      var menu = document.getElementById("ems_nav_tab_context_menu");
      if (!menu) {
        menu = document.createElement("div");
        menu.id = "ems_nav_tab_context_menu";
        menu.className = "ems-nav-tab-context-menu dropdown-menu";
        menu.setAttribute("role", "menu");
        document.body.appendChild(menu);
        navSelf.bindNavTabActionMenu(menu, navSelf, { hideOnClick: true });
        document.addEventListener("click", function() {
          menu.style.display = "none";
          menu.removeAttribute("data-ems-tab-id");
        });
        document.addEventListener("scroll", function() {
          menu.style.display = "none";
          menu.removeAttribute("data-ems-tab-id");
        }, true);
        document.addEventListener("keydown", function(e) {
          if (e.key === "Escape") {
            menu.style.display = "none";
            menu.removeAttribute("data-ems-tab-id");
          }
        });
      }
      var hideMenu = function() {
        menu.style.display = "none";
        menu.removeAttribute("data-ems-tab-id");
      };
      var tabStripEl = document.getElementById(this.workspace.scrollId);
      if (!tabStripEl) {
        var wsRoot = document.getElementById(this.workspace.rootId);
        tabStripEl = wsRoot && wsRoot.querySelector(".ems-nav-tabs-scroll");
      }
      if (tabStripEl && !tabStripEl.getAttribute("data-ems-tab-strip-ctx-bound")) {
        tabStripEl.setAttribute("data-ems-tab-strip-ctx-bound", "1");
        tabStripEl.addEventListener("contextmenu", function(e) {
          if (!navSelf.multiTab) return;
          var tabItem = e.target.closest && e.target.closest(".ems-nav-tab");
          if (!tabItem) return;
          var tabId = tabItem.getAttribute("data-ems-tab-id");
          if (!tabId) return;
          e.preventDefault();
          navSelf.openNavTabContextMenu(menu, tabId, e.clientX, e.clientY);
        });
      }
    },
    /** 工作台标签条 Shell（标题行 + panel 容器）上的切换 / 关闭 / 双击刷新交互。 */
    bindWorkspaceTabShellEvents: function(tabId, targetEle, navLink, closeBtn) {
      var navSelf = this;
      if (navLink.parentElement) {
        navLink.parentElement.setAttribute("data-ems-tab-id", tabId);
      }
      navLink.addEventListener("click", function(e) {
        if (e.target.closest && (e.target.closest(".ems-nav-tab-close") || e.target.closest(".ems-nav-tab-pin"))) return;
        /* 双击时第二下 click(detail=2) 跳过，由 dblclick 处理刷新；单击立即切换，不再等待 250ms */
        if (e.detail > 1) return;
        navSelf.activateNavTab(tabId);
      });
      navLink.addEventListener("dblclick", function(e) {
        if (e.target.closest && (e.target.closest(".ems-nav-tab-close") || e.target.closest(".ems-nav-tab-pin"))) return;
        e.preventDefault();
        navSelf.refreshNavTab(tabId);
      });
      if (closeBtn) {
        closeBtn.addEventListener("click", function(e) {
          e.preventDefault();
          e.stopPropagation();
          if (navSelf.isNavTabPinned(tabId)) {
            navSelf.toggleNavTabPinned(tabId);
          } else {
            navSelf.closeNavTab(tabId, targetEle);
          }
        });
      }
    },

    // --- 门户启动 phase1（快照与 URL/hash 对齐）---

    /**
     * 阶段一：解析 sessionStorage（NAV_TABS_SESSION_KEY）标签快照、URL 参数 group.id、hash 菜单；合并激活标签并写回 storage（如有变更）。
     * @returns {{ navTabsSnapshot: object, snapshotMutated: boolean, initialGroupId: *, initMenuLoc: object|null, menuHighlightOnly: boolean, menuHref: string|null }}
     */
    phase1ResolvePortalBootstrap: function() {
      var snapshotMutated = false;
      var data = { tabs: [], activeTabId: null };
      try {
        var raw = window.sessionStorage && sessionStorage.getItem(NAV_TABS_SESSION_KEY);
        if (raw) {
          var parsed = JSON.parse(raw);
          if (parsed && typeof parsed === "object") {
            data.tabs = Array.isArray(parsed.tabs) ? parsed.tabs.slice() : [];
            data.activeTabId = parsed.activeTabId != null ? parsed.activeTabId : null;
          }
        }
      } catch (e0) {
      }
      var validTabs = [];
      for (var vi = 0; vi < data.tabs.length; vi++) {
        var vt = data.tabs[vi];
        if (vt && vt.id && vt.url) {
          vt.url = this.navUrlToPath(vt.url);
          validTabs.push(vt);
        }
      }
      data.tabs = validTabs;
      var initialGroupId = this.initialGroupId != null && this.initialGroupId !== "" ? this.initialGroupId : null;
      var initMenuLoc = null;
      var menuHref = null;
      if (document.location.hash && document.location.hash.startsWith("#/")) {
        menuHref = document.location.origin + document.location.hash.substring(1);
      }
      if (menuHref) {
        initMenuLoc = this.locateMenu(menuHref);
        if (initMenuLoc) {
          initialGroupId = initMenuLoc.group.id;
        } else {
          initMenuLoc = null;
        }
      }
      var initMenu = initMenuLoc ? initMenuLoc.menu : null;
      var menuHighlightOnly = false;
      if (initMenu && initMenu.entry && (initMenu.openMode === NAV_OPENMODE_WUJIE || initMenu.openMode === NAV_OPENMODE_IFRAME || initMenu.openMode === NAV_OPENMODE_AJAX)) {
        var tid = this.findSnapshotTabIdForUrl({ tabs: data.tabs }, initMenu.entry, initMenu.openMode);
        if (tid) {
          if (data.activeTabId !== tid) {
            data.activeTabId = tid;
            snapshotMutated = true;
          }
          menuHighlightOnly = true;
        } else {
          var maxSeed = 0;
          for (var si = 0; si < data.tabs.length; si++) {
            var mx = /^ems_tab_(\d+)$/.exec(String(data.tabs[si].id || ""));
            if (mx) {
              var num = parseInt(mx[1], 10);
              if (num > maxSeed) maxSeed = num;
            }
          }
          var newId = "ems_tab_" + (maxSeed + 1);
          var seedRow = {
            id: newId,
            url: this.navUrlToPath(initMenu.entry),
            title: initMenu.title || "",
            openMode: this.normalizeNavOpenMode(initMenu.openMode)
          };
          var seedNg = this.formatNavGroupAttr(initMenu.navAppId, initMenu.navGroupId);
          if (seedNg) seedRow.navGroup = seedNg;
          data.tabs.push(seedRow);
          data.activeTabId = newId;
          snapshotMutated = true;
          menuHighlightOnly = true;
        }
      } else if (!initMenuLoc && data.tabs.length > 1) {
        var activeSnap = null;
        for (var ai = 0; ai < data.tabs.length; ai++) {
          if (data.tabs[ai].id === data.activeTabId) {
            activeSnap = data.tabs[ai];
            break;
          }
        }
        if (!activeSnap) activeSnap = data.tabs[0];
        initMenuLoc = this.locateMenu(activeSnap.url);
        initMenu = initMenuLoc ? initMenuLoc.menu : null;
        var activeNg = this.parseNavGroupAttr(activeSnap.navGroup);
        if (activeNg.groupId !== "") {
          initialGroupId = activeNg.groupId;
        } else if (initMenuLoc) {
          initialGroupId = initMenuLoc.group.id;
        }
        if (initMenu && (initMenu.openMode === NAV_OPENMODE_WUJIE || initMenu.openMode === NAV_OPENMODE_IFRAME || initMenu.openMode === NAV_OPENMODE_AJAX)) menuHighlightOnly = true;
      } else if (!initMenuLoc && data.tabs.length === 1) {
        var one = data.tabs[0];
        if (initialGroupId == null || initialGroupId === "") {
          var oneNg = this.parseNavGroupAttr(one.navGroup);
          if (oneNg.groupId !== "") {
            initialGroupId = oneNg.groupId;
          } else if (one.url) {
            var mlOne = this.locateMenu(one.url);
            if (mlOne) initialGroupId = mlOne.group.id;
          }
        }
      }
      if (initialGroupId == null || initialGroupId === "") {
        if (this.groups && this.groups.length > 0) {
          initialGroupId = this.groups[0].id;
        }
      }
      if (this.trimNavTabsSnapshotIfSingleMode(data)) {
        snapshotMutated = true;
      }
      if (snapshotMutated) {
        try {
          sessionStorage.setItem(NAV_TABS_SESSION_KEY, JSON.stringify({
            activeTabId: data.activeTabId,
            tabs: data.tabs
          }));
        } catch (eP) {
        }
      }
      return {
        navTabsSnapshot: data,
        snapshotMutated,
        initialGroupId,
        initMenuLoc,
        menuHighlightOnly,
        menuHref
      };
    },

    // --- session 持久化 ---

    /** 写入 sessionStorage（模块内 NAV_TABS_SESSION_KEY；多标签工作台快照） */
    persistNavTabsSession: function() {
      try {
        if (!window.sessionStorage) return;
        var ids = [];
        if (!this.multiTab) {
          if (this.workspace.activeTabId && this.workspace.tabs[this.workspace.activeTabId]) {
            ids.push(this.workspace.activeTabId);
          } else {
            var fallbackIds = Object.keys(this.workspace.tabs);
            if (fallbackIds.length > 0) ids.push(fallbackIds[0]);
          }
        } else {
          ids = Object.keys(this.workspace.tabs).filter(function(id) {
            return id !== this.workspace.homeTabId;
          }.bind(this));
        }
        var tabs = [];
        for (var ii = 0; ii < ids.length; ii++) {
          var tid = ids[ii];
          var t = this.workspace.tabs[tid];
          if (!t) continue;
          var row = {
            id: tid,
            url: t.url,
            title: t.title || "",
            openMode: this.normalizeNavOpenMode(t.openMode)
          };
          if (t.navGroup) row.navGroup = t.navGroup;
          if (t.pinned) row.pinned = true;
          tabs.push(row);
        }
        var activeTabId = !this.multiTab ? ids.length ? ids[0] : null : this.workspace.activeTabId;
        sessionStorage.setItem(NAV_TABS_SESSION_KEY, JSON.stringify({
          activeTabId,
          tabs
        }));
      } catch (e) {
        if (window.console && console.warn) console.warn("[ems] persistNavTabsSession failed", e);
      }
    },

    // --- 标签激活、关闭与浏览器历史 ---

    /** 根据 tab.navGroup 或 locateMenu(tab.url) 切换侧栏分组并高亮对应菜单项 */
    syncSidebarForWorkspaceTab: function(tab) {
      if (!tab || tab.homeTab) return;

      var ml = tab.url ? this.locateMenu(tab.url) : null;
      var menuObj = ml && ml.menu ? ml.menu : null;
      var navOpts = menuObj ? { highlightOnly: true } : {};

      var ng = this.parseNavGroupAttr(tab.navGroup);
      var gid = ng.groupId;
      var aid = ng.appId;
      if (gid !== void 0 && gid !== null && String(gid) !== "") {
        var openAppId = aid !== void 0 && aid !== null && String(aid) !== "" ? aid : void 0;
        if (openAppId !== void 0) {
          this.displayGroupMenus(gid, openAppId, menuObj || void 0, navOpts);
        } else {
          this.displayGroupMenus(gid, void 0, menuObj || void 0, navOpts);
        }
        return;
      }
      if (ml) {
        var entry = ml.app && ml.app.app ? ml.app.app : ml.app;
        var appOpenId = entry && entry.id != null ? entry.id : void 0;
        this.displayGroupMenus(ml.group.id, appOpenId, menuObj, navOpts);
      }
    },

    /** 切换激活标签、同步 DOM 与地址栏 history；opts.skipHistory 时跳过 pushState/replaceState */
    activateNavTab: function(tabId, opts) {
      opts = opts || {};
      var tab = this.workspace.tabs[tabId];
      if (!tab) return;
      /** 已激活标签再次激活：无 replaceState 时不写 history、不重复刷侧栏/session */
      if (tabId === this.workspace.activeTabId && !opts.replaceState) {
        return;
      }
      var tabList = document.getElementById(this.workspace.listId);
      var body = document.getElementById(this.workspace.bodyId);
      if (tabList && body) {
        var teShow = this.getNavWorkspaceTargetEl() || document.getElementById("main");
        if (teShow) this.showNavWorkspace(teShow);
        var tabIds = Object.keys(this.workspace.tabs);
        for (var ai = 0; ai < tabIds.length; ai++) {
          var id = tabIds[ai];
          var t = this.workspace.tabs[id];
          if (!t) continue;
          var on = id === tabId;
          t.navLink.classList.toggle("active", on);
          var liAct = t.navLink && t.navLink.parentElement;
          if (liAct) liAct.classList.toggle("ems-nav-tab--active", on);
          t.panel.style.display = on ? "flex" : "none";
        }
        this.syncNavTabsA11y(tabId);
        this.syncNavWorkspaceLayout();
      }
      this.workspace.activeTabId = tabId;
      var liScroll = tab.navLink && tab.navLink.parentElement;
      if (liScroll) this.scrollNavTabIntoView(liScroll);
      var hostUrl = this.menuEntryToHostUrl(tab.url);
      if (!opts.skipHistory && hostUrl) {
        try {
          var useReplace = !!(opts.replaceState || this.workspaceTabCount() <= 1);
          var method = useReplace ? "replaceState" : "pushState";
          window.history[method]({ emsNavTab: tabId }, "", hostUrl);
        } catch (eH) {
        }
      }
      if (!opts.skipHistory) {
        this.syncSidebarForWorkspaceTab(tab);
      }
      this.persistNavTabsSession();
    },

    closeNavTab: function(tabId, targetEle) {
      if (this.isNavTabPinned(this.workspace.tabs[tabId])) return;
      var tab = this.workspace.tabs[tabId];
      if (!tab) return;
      var te = targetEle || this.getNavWorkspaceTargetEl();
      var wujieRtClose = resolveWujieRuntime();
      if (tab.openMode !== NAV_OPENMODE_IFRAME && tab.appName && wujieRtClose && wujieRtClose.destroyApp) {
        try {
          wujieRtClose.destroyApp(tab.appName);
        } catch (e) {
          console.warn("[ems] wujie destroyApp", e);
        }
      }
      var liRm = tab.navLink && tab.navLink.parentElement;
      if (liRm && liRm.parentNode) liRm.parentNode.removeChild(liRm);
      if (tab.panel && tab.panel.parentNode) tab.panel.parentNode.removeChild(tab.panel);
      delete this.workspace.tabs[tabId];
      var dedupeK = this.navTabDedupeKey(tab.url, tab.openMode);
      if (this.workspace.tabByUrl[dedupeK] === tabId) delete this.workspace.tabByUrl[dedupeK];
      var remaining = Object.keys(this.workspace.tabs);
      var nextId = remaining.length > 0 ? remaining[remaining.length - 1] : null;
      if (nextId) {
        this.activateNavTab(nextId, { replaceState: true });
      } else {
        this.workspace.tabByUrl = {};
        if (this.workspace.tabs[this.workspace.homeTabId]) {
          this.activateNavTab(this.workspace.homeTabId, { replaceState: true });
        } else {
          this.workspace.activeTabId = null;
          this.hideNavWorkspace(te);
        }
      }
      this.persistNavTabsSession();
    },

    /** 清空非首页标签、移除 session 快照（保留首页时仅激活首页） */
    teardownNavWorkspace: function(targetEle) {
      var te = targetEle || this.getNavWorkspaceTargetEl();
      var homeId = this.workspace.homeTabId;
      var ids = Object.keys(this.workspace.tabs).filter(function(id) {
        return id !== homeId;
      });
      for (var i = 0; i < ids.length; i++) {
        this.closeNavTab(ids[i], te);
      }
      if (this.multiTab && this.workspace.tabs[homeId]) {
        this.activateNavTab(homeId, { replaceState: true });
      }
      this.workspace.tabByUrl = {};
      if (!this.multiTab || !this.workspace.tabs[homeId]) {
        this.workspace.tabs = {};
        this.workspace.activeTabId = null;
      }
      try {
        if (window.sessionStorage) sessionStorage.removeItem(NAV_TABS_SESSION_KEY);
      } catch (eR) {
      }
      this.persistNavTabsSession();
    },
    /** 注册 popstate：浏览器后退/前进时按 history.state.emsNavTab 切换标签并同步侧栏 */
    ensureNavTabHistoryListener: function() {
      if (window.__emsNavTabHistoryBound) return;
      window.__emsNavTabHistoryBound = true;
      window.addEventListener("popstate", function(ev) {
        var n = typeof window.emsShell !== "undefined" && window.emsShell.getNav ? window.emsShell.getNav() : null;
        if (!n || !n.workspace) return;
        var st = ev.state;
        if (st && st.emsNavTab && n.workspace.tabs[st.emsNavTab]) {
          n.activateNavTab(st.emsNavTab, { skipHistory: true });
          n.syncSidebarForWorkspaceTab(n.workspace.tabs[st.emsNavTab]);
        }
      });
    },

    // --- 无界（wujie）子应用 ---

    startWujieAppForTab: function(that, tabId, seq, tabAppName, absoluteUrl) {
      var wujieSync = false;
      if (that.params && (that.params["wujieSync"] === true || that.params["wujieSync"] === "true")) {
        wujieSync = true;
      }
      var wujieFiber = false;
      if (that.params && (that.params["wujieFiber"] === true || that.params["wujieFiber"] === "true")) {
        wujieFiber = true;
      }
      var wujieAlive = true;
      if (that.params && (that.params["wujieAlive"] === false || that.params["wujieAlive"] === "false")) {
        wujieAlive = false;
      }
      var wujieFetchNoStore = false;
      if (that.params && (that.params["wujieFetchNoStore"] === true || that.params["wujieFetchNoStore"] === "true")) {
        wujieFetchNoStore = true;
      }
      function wujieShouldBustEntryUrl() {
        return !wujieAlive || wujieFetchNoStore || !!(that.params && (that.params["wujieEntryReload"] === true || that.params["wujieEntryReload"] === "true"));
      }
      function wujieBustEntryUrl(href) {
        if (!href || !wujieShouldBustEntryUrl()) return href;
        try {
          var u = new URL(href, window.location.href);
          u.searchParams.set("_", String(Date.now()));
          return u.href;
        } catch (eB) {
          return href;
        }
      }
      var W = resolveWujieRuntime();
      var current = that.workspace.tabs[tabId];
      if (!W || !W.startApp) {
        var errMissing = new Error("\u65E0\u754C\u8FD0\u884C\u65F6\u672A\u5C31\u7EEA\uFF1A\u8BF7\u786E\u4FDD\u9875\u9762\u5DF2\u52A0\u8F7D\u65E0\u754C\u5E76\u5728 window.wujie \u4E0A\u63D0\u4F9B startApp\u3002");
        console.error("[ems] wujie startApp", errMissing);
        if (current && current.panel) {
          current.panel.innerHTML = '<div class="p-3 text-danger">\u5B50\u5E94\u7528\u52A0\u8F7D\u5931\u8D25\uFF1A' + errMissing.message + "</div>";
        }
        return Promise.reject(errMissing);
      }
      if (!current || current.seq !== seq) return Promise.resolve();
      if (!document.body.contains(current.panel)) return Promise.resolve();
      try {
        if (W.destroyApp) W.destroyApp(tabAppName);
      } catch (eDes) {
      }
      try {
        current.panel.innerHTML = "";
      } catch (eCl) {
      }
      var entryFetchUrl = wujieBustEntryUrl(absoluteUrl);
      var startOpts = {
        name: tabAppName,
        url: entryFetchUrl,
        el: current.panel,
        sync: wujieSync,
        alive: wujieAlive,
        fiber: wujieFiber,
        loadError: function(url, err) {
          console.error("[ems] wujie loadError", url, err);
        },
        fetch: function(input, init2) {
          init2 = Object.assign({ mode: "cors" }, init2 || {}, { credentials: "include" });
          if (wujieFetchNoStore || !wujieAlive) {
            init2.cache = "no-store";
          }
          return fetch(input, init2).catch(function() {
            return fetch(input, Object.assign({}, init2, { credentials: "omit" }));
          });
        }
      };
      var iframeSrcRaw = that.params && that.params["wujieIframeSrc"] || "";
      if (iframeSrcRaw) {
        try {
          startOpts.attrs = {
            src: new URL(iframeSrcRaw, window.location.href).href
          };
        } catch (eIs) {
          console.warn("[ems] wujie iframe \u521D\u59CB\u5730\u5740\u65E0\u6548:", iframeSrcRaw, eIs);
        }
      }
      startOpts.props = {
        jump: function(location2, query) {
          var urlRaw = "";
          if (typeof location2 === "string") {
            urlRaw = location2;
          } else if (location2 && typeof location2 === "object") {
            urlRaw = location2.href || location2.url || location2.path || "";
          }
          if (!urlRaw && query != null && query !== "") {
            urlRaw = typeof query === "string" ? query : String(query);
          }
          if (!urlRaw) {
            return;
          }
          var abs;
          try {
            abs = new URL(urlRaw, window.location.href).href;
          } catch (eU) {
            abs = urlRaw;
          }
          var t = that.workspace.tabs[tabId];
          if (!t || t.seq !== seq || !t.panel || !document.body.contains(t.panel)) {
            window.location.assign(abs);
            return;
          }
          if (that.workspace.activeTabId !== tabId) {
            window.location.assign(abs);
            return;
          }
          var prevPath = t.url;
          var nextPath = that.navUrlToPath(abs);
          t.url = nextPath;
          var prevKey = that.navTabDedupeKey(prevPath, NAV_OPENMODE_WUJIE);
          var nextKey = that.navTabDedupeKey(nextPath, NAV_OPENMODE_WUJIE);
          if (that.workspace.tabByUrl[prevKey] === tabId) delete that.workspace.tabByUrl[prevKey];
          that.workspace.tabByUrl[nextKey] = tabId;
          var opts = t.wujieStartOpts;
          if (!opts || !W.destroyApp || !W.startApp) {
            window.location.assign(abs);
            return;
          }
          Promise.resolve().then(function() {
            return W.destroyApp(tabAppName);
          }).then(function() {
            var jumpFetchUrl = wujieBustEntryUrl(abs);
            var next = Object.assign({}, opts, { url: jumpFetchUrl });
            return W.startApp(next);
          }).then(function() {
            var hostUrl = that.menuEntryToHostUrl(t.url);
            if (that.workspace.activeTabId === tabId && hostUrl) {
              try {
                window.history.pushState({ emsNavTab: tabId }, "", hostUrl);
              } catch (ePu) {
              }
            }
            that.persistNavTabsSession();
            try {
              window.dispatchEvent(new Event("resize"));
            } catch (eR) {
            }
          }).catch(function(err) {
            console.error("[ems] wujie props.jump", err);
            window.location.assign(abs);
          });
        }
      };
      current.wujieStartOpts = startOpts;
      return W.startApp(startOpts).then(function() {
        try {
          that.syncNavWorkspaceLayout();
          window.dispatchEvent(new Event("resize"));
        } catch (eR) {
        }
      }).catch(function(e) {
        console.error("[ems] wujie startApp", e);
        var curErr = that.workspace.tabs[tabId];
        if (curErr && curErr.panel) {
          var msg = e && e.message ? e.message : String(e);
          var hint = "";
          if (/NetworkError|Failed to fetch|fetch/i.test(msg)) {
            hint = '<p class="small text-muted mt-2 mb-0">\u8DE8\u57DF\u65F6\u65E0\u754C\u4F1A\u7528 fetch \u62C9\u53D6\u5B50\u5E94\u7528 HTML/\u8D44\u6E90\uFF0C\u5B50\u5E94\u7528\u9700\u5BF9\u95E8\u6237 Origin \u8FD4\u56DE CORS\uFF1B\u4F7F\u7528 Cookie \u65F6\u987B Access-Control-Allow-Credentials: true \u4E14 Allow-Origin \u4E0D\u80FD\u4E3A *\u3002\u95E8\u6237\u56FA\u5B9A\u5148\u6309 credentials=include \u8BF7\u6C42\uFF0C\u5931\u8D25\u540E\u4F1A\u81EA\u52A8\u7528 omit \u91CD\u8BD5\u4E00\u6B21\u3002</p>';
          }
          curErr.panel.innerHTML = '<div class="p-3 text-danger">\u5B50\u5E94\u7528\u52A0\u8F7D\u5931\u8D25\uFF1A' + msg + hint + "</div>";
        }
      });
    },

    // --- 刷新恢复（restoreNavTabsFromSession）---

    /**
     * 刷新后根据 sessionStorage（NAV_TABS_SESSION_KEY）重建多标签工作台；最后激活上次选中标签。
     * @param prefetched 若已由 restoreNav 解析好则可传入，避免重复读取；省略或传 null 时再读 sessionStorage。
     * @param opts.preferredTabId 若地址栏 hash 已指向某标签内容，可指定优先激活的标签 id（须在快照 tabs 内）。
     * @param opts.skipSidebarInFinalize 为 true 时不根据激活标签改左侧分组（由 restoreNav 阶段二统一 displayGroupMenus）。
     * @returns {Promise<void>} 无快照或无需异步链时返回 resolved Promise。
     */
    restoreNavTabsFromSession: function(prefetched, opts) {
      opts = opts || {};
      var targetEle = document.getElementById("main");
      if (!targetEle || !targetEle.parentNode) return Promise.resolve();
      var data = null;
      if (prefetched != null && typeof prefetched === "object") {
        data = prefetched;
      } else {
        var raw = null;
        try {
          raw = sessionStorage.getItem(NAV_TABS_SESSION_KEY);
        } catch (e) {
          return Promise.resolve();
        }
        if (!raw) return Promise.resolve();
        try {
          data = JSON.parse(raw);
        } catch (eJ) {
          return Promise.resolve();
        }
      }
      if (!Array.isArray(data.tabs)) {
        data.tabs = [];
      }
      this.trimNavTabsSnapshotIfSingleMode(data);
      var homeOnlyRestore = this.multiTab && data.tabs.length === 0 && data.activeTabId === this.workspace.homeTabId;
      if (data.tabs.length === 0 && !homeOnlyRestore) return Promise.resolve();
      var maxSeed = this.workspace.tabSeed;
      for (var si = 0; si < data.tabs.length; si++) {
        var sid = data.tabs[si].id || "";
        var mx = /^ems_tab_(\d+)$/.exec(sid);
        if (mx) {
          var num = parseInt(mx[1], 10);
          if (num > maxSeed) maxSeed = num;
        }
      }
      this.workspace.tabSeed = maxSeed;
      var that = this;
      var mainWrapper = targetEle.parentNode;
      this.ensureNavWorkspace(mainWrapper, targetEle, { skipPersist: true });
      this.showNavWorkspace(targetEle);
      var navBar = document.getElementById(this.workspace.listId);
      var tabBody = document.getElementById(this.workspace.bodyId);
      if (!navBar || !tabBody) return Promise.resolve();
      this.ensureWorkspaceHomeTab(targetEle);
      function appendRestoredTab(snap) {
        var tabId = snap.id;
        if (tabId === that.workspace.homeTabId) return Promise.resolve();
        var url = that.navUrlToPath(snap.url);
        if (!tabId || !url) return Promise.resolve();
        if (that.workspace.tabs[tabId]) return Promise.resolve();
        var snapNg = that.parseNavGroupAttr(snap.navGroup);
        var om = that.normalizeNavOpenMode(snap.openMode);
        var tabAppName = om === NAV_OPENMODE_WUJIE ? that.buildMicroAppName(snapNg.appId, tabId) : "";
        that.workspace.tabByUrl[that.navTabDedupeKey(url, om)] = tabId;
        var absoluteUrl = that.resolveNavAbsoluteUrl(url);
        var title = snap.title || absoluteUrl.replace(/^https?:\/\/[^/]+/, "") || "\u5FAE\u524D\u7AEF";
        var shell2 = that.appendWorkspaceTabShell(navBar, tabBody, tabId, title, absoluteUrl);
        var navLink = shell2.navLink;
        var panel = shell2.panel;
        var closeBtn = shell2.closeBtn;
        var navGroupStr = snap.navGroup != null && String(snap.navGroup).trim() !== "" ? String(snap.navGroup).trim() : that.formatNavGroupAttr(snapNg.appId, snapNg.groupId);
        var seq = ++that.workspace.openSeq;
        var tab = {
          id: tabId,
          url,
          title,
          openMode: om,
          navGroup: navGroupStr,
          navLink,
          panel,
          seq
        };
        if (snap.pinned) tab.pinned = true;
        if (om === NAV_OPENMODE_IFRAME) {
          tab.iframeEl = that.mountIframeInWorkspacePanel(panel, tabId, absoluteUrl);
        } else if (om === NAV_OPENMODE_AJAX) {
          tab.ajaxMainId = that.buildWorkspaceAjaxMainId(tabId);
          that.mountAjaxInWorkspacePanel(panel, tabId, absoluteUrl);
        } else {
          tab.appName = tabAppName;
        }
        that.workspace.tabs[tabId] = tab;
        that.bindWorkspaceTabShellEvents(tabId, targetEle, navLink, closeBtn);
        that.applyNavTabPinnedDom(tabId);
        if (om === NAV_OPENMODE_IFRAME || om === NAV_OPENMODE_AJAX) {
          return Promise.resolve();
        }
        return that.startWujieAppForTab(that, tabId, seq, tabAppName, absoluteUrl);
      }
      var expectTabCount = data.tabs.length;
      var chain = Promise.resolve();
      for (var ri = 0; ri < data.tabs.length; ri++) {
        (function(snap) {
          chain = chain.then(function() {
            return Promise.resolve(appendRestoredTab(snap)).catch(function(eTab) {
              if (window.console && console.warn) console.warn("[ems] restoreNavTabsFromSession tab failed", snap && snap.id, eTab);
            });
          });
        })(data.tabs[ri]);
      }
      function finalizeNavTabsRestore() {
        that.syncNavTabsDomOrder();
        var activeId = data.activeTabId;
        var actTab = null;
        var pickId = null;
        if (opts.preferredTabId && that.workspace.tabs[opts.preferredTabId]) {
          pickId = opts.preferredTabId;
        } else if (activeId && that.workspace.tabs[activeId]) {
          pickId = activeId;
        } else if (homeOnlyRestore && that.workspace.tabs[that.workspace.homeTabId]) {
          pickId = that.workspace.homeTabId;
        } else {
          var firstId = data.tabs[0] && data.tabs[0].id;
          if (firstId && that.workspace.tabs[firstId]) pickId = firstId;
        }
        if (pickId) {
          actTab = that.workspace.tabs[pickId];
          that.activateNavTab(pickId, { skipHistory: true });
          var finalizeHostUrl = that.menuEntryToHostUrl(actTab.url);
          if (!opts.skipSidebarInFinalize && finalizeHostUrl) {
            try {
              window.history.replaceState({ emsNavTab: pickId }, "", finalizeHostUrl);
            } catch (eRs) {
            }
          }
        }
        if (actTab && !opts.skipSidebarInFinalize) {
          that.syncSidebarForWorkspaceTab(actTab);
        }
        var got = Object.keys(that.workspace.tabs).length;
        if (got > 0) {
          that.trimNavTabsDownToMaxCount(targetEle);
          that.persistNavTabsSession();
        } else if (expectTabCount > 0 && window.console && console.warn) {
          console.warn("[ems] restoreNavTabsFromSession: \u9884\u671F\u6062\u590D " + expectTabCount + " \u4E2A\u6807\u7B7E\u4F46\u5B9E\u9645\u4E3A 0\uFF0C\u672A\u5199\u5165\u7A7A\u5FEB\u7167\u4EE5\u514D\u4E22\u5931 session");
        }
      }
      return chain.then(finalizeNavTabsRestore).catch(function(err) {
        if (window.console && console.warn) console.warn("[ems] restoreNavTabsFromSession chain", err);
        finalizeNavTabsRestore();
      });
    },

    // --- 菜单 openMenu 入口 ---

    /**
     * 左侧/顶部菜单：<a data-open-mode="iframe|wujie">，缺省或非合法值按 iframe。
     * - multiTab=true（默认）：iframe / wujie 在工作台开标签；ajax 仅用于内部首页 welcome。
     * - multiTab=false：iframe/wujie 进工作台单槽；#main 供内部 ajax 片段。
     * openMode 由 collectApps 推导：navStyle=wujie 否则 iframe。
     */
    openMenu: function(obj) {
      try {
        var target = "";
        if (typeof obj == "object" && obj.tagName && obj.tagName.toLowerCase() == "a") {
          target = (obj.getAttribute("target") || "").trim();
        }
        if (!target) target = "main";
        if (target == "_blank") return true;
        var navGroupIdFromLink = "";
        var navAppIdFromLink = "";
        var openMode = "";
        if (typeof obj == "object" && obj.tagName && obj.tagName.toLowerCase() == "a") {
          openMode = (obj.getAttribute("data-open-mode") || "").trim().toLowerCase();
          var ng = this.parseNavGroupAttr(obj.getAttribute("data-nav-group"));
          navAppIdFromLink = ng.appId;
          navGroupIdFromLink = ng.groupId;
        }
        var targetEle = document.getElementById(target);
        if (!targetEle || !targetEle.parentNode) {
          console.warn("[ems] openMenu: missing container #" + target);
          return false;
        }
        if (openMode !== NAV_OPENMODE_WUJIE && openMode !== NAV_OPENMODE_IFRAME) {
          openMode = NAV_OPENMODE_IFRAME;
        }
        var mainWrapper = targetEle.parentNode;
        if (openMode === NAV_OPENMODE_WUJIE) {
          var that = this;
          var rawHref = typeof obj == "object" && obj != null && obj.href ? String(obj.href) : "";
          if (!rawHref) {
            console.warn("[ems] openMenu: wujie \u9700\u8981\u5E26 href \u7684 <a> \u83DC\u5355\u94FE\u63A5");
            return false;
          }
          var url = this.navUrlToPath(rawHref);
          this.ensureNavWorkspace(mainWrapper, targetEle);
          this.showNavWorkspace(targetEle);
          this.syncNavWorkspaceChrome();
          if (this.tryActivateExistingWorkspaceTab(url, NAV_OPENMODE_WUJIE)) return false;
          var slotW = this.prepareNewWorkspaceTabSlot(targetEle, url, NAV_OPENMODE_WUJIE);
          if (!slotW) {
            console.error("[ems] openMenu: \u5D4C\u5165\u5DE5\u4F5C\u53F0 DOM \u672A\u5C31\u7EEA\uFF08\u7F3A\u5C11\u6807\u7B7E\u680F\u6216\u5185\u5BB9\u533A\uFF09");
            return false;
          }
          var title = this.getNavTabTitle(obj, rawHref || url);
          var shellW = this.appendWorkspaceTabShell(slotW.navBar, slotW.tabBody, slotW.tabId, title, slotW.absoluteUrl);
          var seq = ++this.workspace.openSeq;
          var tabAppName = this.buildMicroAppName(navAppIdFromLink, slotW.tabId);
          var tab = {
            id: slotW.tabId,
            url,
            title,
            openMode: NAV_OPENMODE_WUJIE,
            appName: tabAppName,
            navGroup: this.formatNavGroupAttr(navAppIdFromLink, navGroupIdFromLink),
            navLink: shellW.navLink,
            panel: shellW.panel,
            seq
          };
          this.workspace.tabs[slotW.tabId] = tab;
          this.bindWorkspaceTabShellEvents(slotW.tabId, targetEle, shellW.navLink, shellW.closeBtn);
          this.activateNavTab(slotW.tabId);
          this.startWujieAppForTab(that, slotW.tabId, seq, tabAppName, slotW.absoluteUrl);
          return false;
        }
        if (openMode === NAV_OPENMODE_IFRAME) {
          var rawHrefIf = typeof obj == "object" && obj != null && obj.href ? String(obj.href) : "";
          if (!rawHrefIf) {
            console.warn("[ems] openMenu: iframe \u9700\u8981\u5E26 href \u7684 <a> \u83DC\u5355\u94FE\u63A5");
            return false;
          }
          var urlIf = this.navUrlToPath(rawHrefIf);
          this.ensureNavWorkspace(mainWrapper, targetEle);
          this.showNavWorkspace(targetEle);
          this.syncNavWorkspaceChrome();
          if (this.tryActivateExistingWorkspaceTab(urlIf, NAV_OPENMODE_IFRAME)) return false;
          var slotIf = this.prepareNewWorkspaceTabSlot(targetEle, urlIf, NAV_OPENMODE_IFRAME);
          if (!slotIf) {
            console.error("[ems] openMenu: iframe \u591A\u6807\u7B7E\u5DE5\u4F5C\u53F0 DOM \u672A\u5C31\u7EEA");
            return false;
          }
          var titleIf = this.getNavTabTitle(obj, rawHrefIf || urlIf);
          var shellIf = this.appendWorkspaceTabShell(slotIf.navBar, slotIf.tabBody, slotIf.tabId, titleIf, slotIf.absoluteUrl);
          var iframeMain = this.mountIframeInWorkspacePanel(shellIf.panel, slotIf.tabId, slotIf.absoluteUrl);
          var seqIf = ++this.workspace.openSeq;
          var tabIf = {
            id: slotIf.tabId,
            url: urlIf,
            title: titleIf,
            openMode: NAV_OPENMODE_IFRAME,
            navGroup: this.formatNavGroupAttr(navAppIdFromLink, navGroupIdFromLink),
            navLink: shellIf.navLink,
            panel: shellIf.panel,
            iframeEl: iframeMain,
            seq: seqIf
          };
          this.workspace.tabs[slotIf.tabId] = tabIf;
          this.bindWorkspaceTabShellEvents(slotIf.tabId, targetEle, shellIf.navLink, shellIf.closeBtn);
          this.activateNavTab(slotIf.tabId);
          this.persistNavTabsSession();
          return false;
        }
        if (openMode === NAV_OPENMODE_AJAX) {
          if (!this.multiTab) {
            this.hideNavWorkspace(targetEle);
            if (targetEle.tagName == "DIV") {
              beangle.Go(obj, target);
            } else if (targetEle.tagName == "IFRAME") {
              mainWrapper.removeChild(targetEle);
              var f = document.createElement("div");
              f.setAttribute("width", "100%");
              f.setAttribute("height", "100%");
              f.setAttribute("class", "ajax_container");
              f.id = target;
              mainWrapper.appendChild(f);
              beangle.Go(obj, target);
            }
            return false;
          }
          var rawHrefAjax = typeof obj == "object" && obj != null && obj.href ? String(obj.href) : "";
          if (!rawHrefAjax) {
            console.warn("[ems] openMenu: ajax \u9700\u8981\u5E26 href \u7684 <a> \u83DC\u5355\u94FE\u63A5");
            return false;
          }
          var urlAjax = this.navUrlToPath(rawHrefAjax);
          this.ensureNavWorkspace(mainWrapper, targetEle);
          this.showNavWorkspace(targetEle);
          this.syncNavWorkspaceChrome();
          if (this.tryActivateExistingWorkspaceTab(urlAjax, NAV_OPENMODE_AJAX)) return false;
          var slotA = this.prepareNewWorkspaceTabSlot(targetEle, urlAjax, NAV_OPENMODE_AJAX);
          if (!slotA) {
            console.error("[ems] openMenu: ajax \u591A\u6807\u7B7E\u5DE5\u4F5C\u53F0 DOM \u672A\u5C31\u7EEA");
            return false;
          }
          var titleA = this.getNavTabTitle(obj, rawHrefAjax || urlAjax);
          var shellA = this.appendWorkspaceTabShell(slotA.navBar, slotA.tabBody, slotA.tabId, titleA, slotA.absoluteUrl);
          var ajaxInner = this.mountAjaxInWorkspacePanel(shellA.panel, slotA.tabId, obj);
          var seqA = ++this.workspace.openSeq;
          var tabA = {
            id: slotA.tabId,
            url: urlAjax,
            title: titleA,
            openMode: NAV_OPENMODE_AJAX,
            navGroup: this.formatNavGroupAttr(navAppIdFromLink, navGroupIdFromLink),
            navLink: shellA.navLink,
            panel: shellA.panel,
            ajaxMainId: ajaxInner.id,
            seq: seqA
          };
          this.workspace.tabs[slotA.tabId] = tabA;
          this.bindWorkspaceTabShellEvents(slotA.tabId, targetEle, shellA.navLink, shellA.closeBtn);
          this.activateNavTab(slotA.tabId);
          this.persistNavTabsSession();
          return false;
        }
      } catch (eOm) {
        console.error("[ems] openMenu", eOm);
        return false;
      }
    }
};
