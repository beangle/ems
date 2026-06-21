// ems.js — EMS 工具与门户导航
(function (global) {
  'use strict';

  if (typeof String.prototype.endsWith != 'function') {
    String.prototype.endsWith = function (suffix) {
      return this.indexOf(suffix, this.length - suffix.length) !== -1;
    };
  }

  /** sessionStorage 键：嵌入工作台 iframe/wujie 多标签快照 */
  var NAV_TABS_SESSION_KEY = "beangle.ems.nav_tabs";

  /** localStorage 键：导航栏与外观偏好 */
  var NAV_STICKY_HEADER_STORAGE_KEY = "beangle.ems.nav_sticky_header";
  var NAV_SIDEBAR_THEME_STORAGE_KEY = "beangle.ems.nav_sidebar_theme";
  /** localStorage：工作台多标签（true）/ 单槽（false）；缺省为多标签 */
  var NAV_MULTI_TAB_STORAGE_KEY = "beangle.ems.multi_tab";
  var ROOT_FONT_SIZE_STORAGE_KEY = "beangle.ems.root_font_size";
  var THEME_STORAGE_KEY = "beangle.ems.theme";

  /** 菜单 data-open-mode / 工作台快照 tab.openMode（与后端、HTML 约定一致） */
  var NAV_OPENMODE_AJAX = "ajax";
  var NAV_OPENMODE_IFRAME = "iframe";
  var NAV_OPENMODE_WUJIE = "wujie";

  /** 多标签工作台：常驻首页标签（welcomeUrl ajax），不可关闭、不参与 URL 去重、不入快照 tabs 数组 */
  var NAV_WORKSPACE_HOME_TAB_ID = "ems_tab_home";

  /**
   * 规范化无界运行时（wujie 2.x 经 ESM import 时未必挂 window.wujie）。
   * 解析成功后写回 window.wujie，供 startApp / destroyApp 与 beangle.Go 子应用 jump 共用。
   */
  function resolveWujieRuntime() {
    if (typeof window !== "undefined" && window.wujie && typeof window.wujie.startApp === "function") {
      return window.wujie;
    }
    try {
      if (typeof beangle !== "undefined" && beangle.amd && typeof beangle.amd.pickModuleExport === "function") {
        var picked = beangle.amd.pickModuleExport("wujie");
        if (picked && typeof picked.startApp === "function") {
          window.wujie = picked;
          return picked;
        }
      }
    } catch (ePick) { /* ignore */ }
    return null;
  }

  var config = { profiles: [], profile: {}, api: "" };

  function processProfileUrl() {
    for (var i = 0; i < config.profiles.length; i++) {
      var profile = config.profiles[i];
      if (!profile.url) {
        profile.url = (location.origin + location.pathname + "?contextProfileId=" + profile.id)
      }
    }
  }

  function init(profiles, cookie) {
    var profile = null;
    if (cookie && cookie.profile) {
      for (var i = 0; i < profiles.length; i++) {
        var p = profiles[i];
        if (p.id == cookie.profile) {
          profile = p;
          break;
        }
      }
    }
    if (!profile) {
      profile = profiles[0];
    }
    config.profiles = profiles;
    config.profile = profile;
    processProfileUrl();
  }

  function hostName(u1) {
    if (u1 == null || u1 === "") return "";
    u1 = String(u1);
    var slashIdx = u1.indexOf('//');
    if (-1 == slashIdx) {
      slashIdx = 0;
    } else {
      slashIdx += 2;
    }
    var endIdx = u1.indexOf(':', slashIdx)
    if (-1 == endIdx) {
      endIdx = u1.indexOf('/', slashIdx)
    }
    if (-1 == endIdx) {
      endIdx = u1.length
    }
    return u1.substring(slashIdx, endIdx);
  }

  /** 相对路径 base（如 /portal）视为当前页同源，便于与 shell 完整 URL 比较 */
  function sameDomain(u1, u2) {
    var h1 = hostName(u1);
    var h2 = hostName(u2);
    if (h1 === "" && u1 && String(u1).charAt(0) === "/") {
      h1 = hostName(window.location.href);
    }
    if (h2 === "" && u2 && String(u2).charAt(0) === "/") {
      h2 = hostName(window.location.href);
    }
    return h1 === h2;
  }

  /**
   * @domainMenus
   *    {domain:{},groups:[{group:{},appMenus:[{app:{},menus:[]},{app:{},menus:[]}] }] }
   */
  function Nav(app, portal, domainMenus, params) {
    this.portal = portal;
    this.app = app;
    this.apps = [];
    this.groups = [];
    this.menuDomId = "menu_ul";
    this.navDomId = "top_nav_bar";
    this.sysName = null;
    this.params = {};
    this.maxTopItem = 9;
    this.welcomeUrl = null;
    /** iframe / wujie 多标签工作台状态（DOM id、打开序号、去重表等） */
    this.workspace = {
      rootId: "ems_nav_workspace",
      listId: "ems_nav_tab_nav",
      bodyId: "ems_nav_tab_body",
      homeTabId: NAV_WORKSPACE_HOME_TAB_ID,
      homeMainId: "ems_nav_home_main",
      tabSeed: 0,
      tabs: {},
      tabByUrl: {},
      activeTabId: null,
      openSeq: 0,
      maxTabCount: 30
    };
    /**
     * multiTab=true（默认）：ajax / iframe / wujie 均在工作台多标签；#main 作占位隐藏。
     * multiTab=false：单嵌入槽（隐藏标签条）；ajax 走 #main，iframe/wujie 每次先清空再开。
     */
    this.multiTab = true;
    this.initialGroupId = null;
    if (params) {
      for (var name in params) {
        var pv = params[name];
        if ("menuDomId" == name) {
          this.menuDomId = pv;
        } else if ("navDomId" == name) {
          this.navDomId = pv;
        } else if ("sysName" == name) {
          this.sysName = pv;
        } else if ("maxTopItem" == name) {
          this.maxTopItem = Number.parseInt(pv);
        } else if ("initialGroupId" == name) {
          this.initialGroupId = pv;
        } else if ("multiTab" == name) {
          if (pv === false || pv === "false" || pv === 0 || pv === "0") {
            this.multiTab = false;
          } else {
            this.multiTab = true;
          }
        } else {
          this.params[name] = pv;
        }
      }
    }
    this.groupMenus = domainMenus.groups;
    for (var i = 0; i < this.groupMenus.length; i++) {
      this.groups.push(this.groupMenus[i].group);
    }

    this.getIconClass = function (name) {
      if (name.indexOf("设置") > -1) {
        return "fas fa-cog";
      } else if (name.endsWith("开关")) {
        return "fa fa-toggle-on";
      } else if (name.endsWith("信息")) {
        return "fa fa-info-circle";
      } else if (name.indexOf("查询") > -1) {
        return "fa fa-search";
      } else if (name.indexOf("打印") > -1) {
        return "fa fa-print";
      } else if (name.indexOf("统计") > -1) {
        return "fa fa-chart-bar";
      } else if (name.indexOf("安排") > -1) {
        return "fa fa-calendar";
      } else if (name.indexOf("排名") > -1) {
        return "fa fa-sort-amount-down";
      } else if (name.endsWith("表")) {
        return "fa fa-table";
      } else {
        return "far fa-circle";
      }
    }
    this.menuTempalte = '<li class="nav-item"><a class="nav-link" href="{menu.entry}" target="{menu.target}" data-open-mode="{menu.openMode}" data-nav-group="{menu.navGroupAttr}"><i class="nav-icon {icon_class}"></i><p>{menu.title}</p></a></li>';
    this.foldTemplate = '<li class="nav-item has-treeview {open_class}"><a class="nav-link {active_class}" href="javascript:void(0)"><i class="nav-icon {icon_class}"></i><p>{menu.title}<i class="nav-icon fa fa-angle-left right"></i></p></a><ul class="nav nav-treeview" id="menu{menu.id}"></ul></li>'
    this.appFoldTemplate = '<li class="nav-item has-treeview {open_class}"><a class="nav-link {active_class}" href="javascript:void(0)"><i class="nav-icon {icon_class}"></i><p>{app.title}<i class="nav-icon fa fa-angle-left right"></i></p></a><ul class="nav nav-treeview" id="menu_app{app.id}"></ul></li>'

    this.groupTemplate = '<li class="nav-item"><a class="nav-link {active_class}" href="javascript:void(0)" id="group_{group.id}">{group.title}</a></li>';
    this.dropdownGroupNavTemplate = '<a href="javascript:void(0)"  class="dropdown-item {active_class}" id="group_{group.id}">{group.title}</a>'
    this.collectApps();
    this.processMenus();
    if (!this.sysName) {
      this.sysName = this.portal.title;
    }
    jQuery("#" + this.menuDomId).addClass("sidebar-menu");
  }

  Nav.prototype = {
    setWelcomeUrl: function (url) {
      this.welcomeUrl = url;
    },
    processUrl: function (url) {
      if (url.indexOf('{') == -1) return url;
      for (var name in this.params) {
        url = url.replace('{' + name + '}', this.params[name]);
      }
      return url;
    },
    /**
     * 收集domain中的apps，以及每个app对应的菜单
     */
    collectApps: function () {
      var shellAppBase = this.app.base;
      if (!this.portal.base && this.portal.url) {
        this.portal.base = this.portal.url;
      }
      for (var p = 0; p < this.groupMenus.length; p++) {
        var childrenApps = this.groupMenus[p].appMenus; // a group contain many app
        var group = this.groupMenus[p].group;
        for (var i = 0; i < childrenApps.length; i++) {
          var app = childrenApps[i].app;
          if (!app.group) {
            app.group = group;
          }
          if (app.name == this.portal.name) {
            if (!this.portal.title) {
              this.portal.title = app.title;
            }
            this.portal.group = group;
            if (this.portal !== this.app) {
              this.portal.base = app.base;
            }
          }else{
            this.apps.push(app);
          }
          /* 当前壳应用始终用页面传入的 base（完整 URL），不被 JSON 中相对路径覆盖 */
          if (app.name == this.app.name) {
            app.base = shellAppBase;
            this.app.group = group;
            if (!this.app.title) {
              this.app.title = app.title;
            }
            this.app.id = app.id;
            if (this.apps.indexOf(app) < 0) {
              this.apps.push(app);
            }
          }
          //去除appbase的结尾斜线，因为资源的形式为/path/to/uri
          if (app.base && app.base.endsWith("/")) {
            app.base = app.base.substring(0, app.base.length - 1);
          }
          app.base = this.processUrl(app.base || shellAppBase);
          /* openMode：仅 wujie 走微前端，其余（含空、iframe、历史 ajax/adminlte 等）均 iframe */
          if (app.navStyle == NAV_OPENMODE_WUJIE) {
            app.openMode = NAV_OPENMODE_WUJIE;
          } else {
            app.openMode = NAV_OPENMODE_IFRAME;
          }
          app.embeddable = sameDomain(shellAppBase, app.base);
        }
      }
      if (shellAppBase) {
        var trimmed = shellAppBase;
        if (trimmed.endsWith("/")) {
          trimmed = trimmed.substring(0, trimmed.length - 1);
        }
        this.app.base = this.processUrl(trimmed);
      }
    },

    searchMenu: function (name, limit) {
      if (!limit) limit = 10;
      var results = [];
      for (var p = 0; p < this.groupMenus.length; p++) {
        var childrenAppMenus = this.groupMenus[p].appMenus;
        for (var i = 0; i < childrenAppMenus.length; i++) {
          this.searchMenuByName(childrenAppMenus[i].menus, name, limit, results, []);
          if (results.length >= limit) return results;
        }
      }
      return results;
    },

    searchMenuByName: function (menus, name, limit, results, path) {
      for (var j = 0; j < menus.length; j++) {
        if (results.length >= limit) return;
        var menu = menus[j];
        if (menu.entry && (menu.title.includes(name) || menu.entry.toLowerCase().includes(name))) {
          var resultPath = path.slice();
          resultPath.push(menu.title);
          var result = { 'name': menu.title, 'link': menu.entry, 'path': resultPath, 'target': menu.target, 'openMode': menu.openMode || NAV_OPENMODE_IFRAME, 'navGroupAttr': this.formatNavGroupAttr(menu.navAppId, menu.navGroupId) }
          results.push(result);
          if (results.length >= limit) break;
        } else {
          if (menu.children && menu.children.length > 0) {
            path.push(menu.title);
            this.searchMenuByName(menu.children, name, limit, results, path);
          }
        }
      }
      if (path.length > 0) path.pop();
    },
    locateMenu: function (url) {
      for (var p = 0; p < this.groupMenus.length; p++) {
        var appMenus = this.groupMenus[p].appMenus;
        for (var i = 0; i < appMenus.length; i++) {
          var m = this.locateMenuByHref(appMenus[i].menus, url);
          if (m) return { "group": this.groupMenus[p].group, "app": appMenus[i], "menu": m };
        }
      }
      return null;
    },
    locateMenuByHref: function (menus, url) {
      var menu = null;
      var urlPath = this.navUrlToPath(url);
      for (var j = 0; j < menus.length; j++) {
        menu = menus[j];
        if (menu.entry) {
          if (url.startsWith(menu.entry)) {
            return menu;
          }
          var entryPath = this.navUrlToPath(menu.entry);
          if (entryPath !== "" && urlPath.startsWith(entryPath)) {
            return menu;
          }
        }
        if (menu.children && menu.children.length > 0) {
          var childHit = this.locateMenuByHref(menu.children, url);
          if (childHit) return childHit;
        }
      }
      return null;
    },
    processMenus: function () { // 修改和构架menu的入口和target
      for (var p = 0; p < this.groupMenus.length; p++) {
        var childrenAppMenus = this.groupMenus[p].appMenus;
        var navGroupId = this.groupMenus[p].group.id;
        for (var i = 0; i < childrenAppMenus.length; i++) {
          var app = childrenAppMenus[i].app;
          this.processMenuEntry(app, childrenAppMenus[i].menus, navGroupId);
        }
      }
    },

    /** 将 menu.openMode 规范为 iframe | wujie；ajax 等历史值归并为 iframe */
    normalizeMenuOpenMode: function (menu, fallbackOpenMode) {
      var raw = menu.openMode != null && String(menu.openMode) !== "" ? String(menu.openMode).toLowerCase() : "";
      if (raw === NAV_OPENMODE_WUJIE) {
        menu.openMode = NAV_OPENMODE_WUJIE;
      } else if (raw === NAV_OPENMODE_IFRAME || raw === NAV_OPENMODE_AJAX) {
        menu.openMode = NAV_OPENMODE_IFRAME;
      } else {
        menu.openMode = (fallbackOpenMode === NAV_OPENMODE_WUJIE) ? NAV_OPENMODE_WUJIE : NAV_OPENMODE_IFRAME;
      }
    },

    processMenuEntry: function (app, menus, navGroupId) {
      if (navGroupId === undefined || navGroupId === null) {
        navGroupId = "";
      }
      for (var i = 0; i < menus.length; i++) {
        var menu = menus[i];
        if (menu.entry) {
          menu.navGroupId = navGroupId;
          menu.navAppId = app.id;
        }
        if (menu.entry && !menu.entry.startsWith("http")) {
          if (app.embeddable) menu.target = "main"; else menu.target = "_blank";
          menu.entry = this.processUrl(app.base + menu.entry);
          this.normalizeMenuOpenMode(menu, app.openMode);
        } else if (menu.entry) {
          this.normalizeMenuOpenMode(menu, NAV_OPENMODE_IFRAME);
        }
        if (menu.children) this.processMenuEntry(app, menu.children, navGroupId);
      }
    },
    /**
     * 在左侧菜单栏创建菜单
     * @param openMenuId may be appId,menuId
     */
    createMenus: function (jqueryElem, menus, openMenuId) {
      jqueryElem.empty();
      if (!openMenuId) openMenuId = -1;
      var menuItem = '';
      for (var i = 0; i < menus.length; i++) {
        var menu = menus[i];
        var fonticon = "fa fa-list"
        if (menu.menus) {//menu is an app
          var appItem = this.appFoldTemplate.replace('{app.id}', menu.app.id);
          appItem = appItem.replace('{icon_class}', fonticon);
          appItem = appItem.replace('{app.title}', menu.app.title);
          appItem = appItem.replace('{open_class}', (openMenuId == menu.app.id) ? "menu-open" : "");
          appItem = appItem.replace('{active_class}', (openMenuId == menu.app.id) ? "active" : "");
          jqueryElem.append(appItem);
          this.createMenus(jQuery('#menu_app' + menu.app.id), menu.menus);
        } else if (menu.children) {//fold
          menuItem = this.foldTemplate.replace('{menu.id}', menu.id);
          if (menu.fonticon) fonticon = menu.fonticon;
          menuItem = menuItem.replace('{icon_class}', fonticon);
          menuItem = menuItem.replace('{menu.title}', menu.title);
          menuItem = menuItem.replace('{open_class}', (openMenuId == menu.id) ? "menu-open" : "");
          menuItem = menuItem.replace('{active_class}', (openMenuId == menu.id) ? "active" : "");
          jqueryElem.append(menuItem);
          this.createMenus(jQuery('#menu' + menu.id), menu.children);
        } else {//menu
          menuItem = this.menuTempalte.replace('{menu.title}', menu.title);
          if (menu.fonticon) { fonticon = menu.fonticon; }
          else { fonticon = this.getIconClass(menu.title); }
          menuItem = menuItem.replace('{icon_class}', fonticon);
          menuItem = menuItem.replace('{menu.entry}', menu.entry);
          menuItem = menuItem.replace(/\{menu\.target\}/g, menu.target);
          menuItem = menuItem.replace('{menu.openMode}', menu.openMode ? String(menu.openMode) : NAV_OPENMODE_IFRAME);
          menuItem = menuItem.replace('{menu.navGroupAttr}', this.formatNavGroupAttr(menu.navAppId, menu.navGroupId));
          jqueryElem.append(menuItem);
        }
      }
    },
    activate: function () {
      var menuDomId = this.menuDomId;
      var navSelf = this;
      jQuery("#" + menuDomId).off("click.emsTree").on("click.emsTree", "li a", function (e) {
        var target = (this.getAttribute("target") || "").trim();
        if (target === "_blank") {
          return;
        }
        e.preventDefault();
        e.stopImmediatePropagation();
        var href = this.getAttribute("href") || "";
        var jThis = jQuery(this);
        var li = jThis.parent("li");
        if (href === "javascript:void(0)" || href.indexOf("javascript:void") === 0) {
          if (li.hasClass("has-treeview")) {
            var opening = !li.hasClass("menu-open");
            li.siblings(".has-treeview").removeClass("menu-open menu-is-opening");
            li.siblings(".has-treeview").children("a").removeClass("active");
            if (opening) {
              li.addClass("menu-open");
              jThis.addClass("active");
            } else {
              li.removeClass("menu-open menu-is-opening");
              jThis.removeClass("active");
            }
            return;
          }
          li.siblings().each(function (i, sli) {
            jQuery(sli).removeClass("menu-open menu-is-opening");
            jQuery(sli).children("ul").removeAttr("style");
            jQuery(sli).children("a").removeClass("active");
          });
          jThis.toggleClass("active");
          return;
        }
        li.siblings().each(function (i, sli) {
          jQuery(sli).children("a").removeClass("active");
        });
        jThis.addClass("active");
        navSelf.openMenu(this);
      });
    },
    /** 顶栏分组：事件委托，只绑定一次 */
    bindTopGroupNav: function () {
      jQuery("#" + this.navDomId).off("click.emsGroup").on("click.emsGroup", "a[id^='group_']", function (e) {
        e.preventDefault();
        e.stopImmediatePropagation();
        changeGroup(this);
      });
    },
    /** 侧栏搜索结果菜单打开 */
    bindSearchMenuOpen: function () {
      var navSelf = this;
      jQuery("#main_siderbar").off("click.emsSearchOpen").on("click.emsSearchOpen", ".sidebar-search-results a.list-group-item[data-open-mode]", function (e) {
        e.preventDefault();
        e.stopImmediatePropagation();
        navSelf.openMenu(this);
      });
    },
    /**
     * 保证嵌入导航工作台 DOM（多标签 + 工具栏 + 内容区）已挂载。
     * 若工作台根节点（this.workspace.rootId，如 #ems_nav_workspace）已存在：按需写入 session 快照、同步壳层可见性后返回该节点。
     * 否则：在 mainWrapper 下创建整块结构、可选恢复 targetEle 显示、绑定标签右键菜单一次，再持久化与同步。
     * @param {HTMLElement} mainWrapper 工作台根节点挂载的父元素
     * @param {HTMLElement|null} targetEle 与 iframe 并列的宿主区域（创建时会 display=""，一般为 #main）
     * @param {{skipPersist?:boolean}} [opts] skipPersist 为 true 时不调用 persistNavTabsSession
     */
    ensureNavWorkspace: function (mainWrapper, targetEle, opts) {
      opts = opts || {};
      var skipPersist = !!opts.skipPersist;
      var rootEl = document.getElementById(this.workspace.rootId);
      if (rootEl) {
        if (!skipPersist) this.persistNavTabsSession();
        this.syncNavWorkspaceChrome();
        this.ensureWorkspaceHomeTab(targetEle);
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
      rootEl.innerHTML =
        '<div class="' + cardCls + '">' +
        '<div class="card-header ems-nav-toolbar p-0 d-flex align-items-stretch ems-nav-toolbar-strip">' +
        '<div class="ems-nav-tabs-scroll flex-grow-1 overflow-auto">' +
        '<ul class="nav ems-nav-tabs ems-nav-tabs--workspace flex-nowrap align-items-center" id="' + this.workspace.listId + '" role="tablist"></ul>' +
        '</div></div>' +
        '<div class="' + bodyCls + '" id="' + this.workspace.bodyId + '"' + bodyMinAttr + '></div>' +
        '</div>';
      mainWrapper.appendChild(rootEl);
      if (targetEle) targetEle.style.display = "";

      var that = this;
      var tgtId = targetEle && targetEle.id ? targetEle.id : "main";
      rootEl.setAttribute("data-ems-nav-target", tgtId);
      if (!rootEl.getAttribute("data-ems-toolbar-bound")) {
        rootEl.setAttribute("data-ems-toolbar-bound", "1");
        that.bindWorkspaceTabContextMenu(rootEl);
      }
      if (!skipPersist) this.persistNavTabsSession();
      this.syncNavWorkspaceChrome();
      this.ensureWorkspaceHomeTab(targetEle);
      return rootEl;
    },
    getNavWorkspaceTargetEl: function () {
      var w = document.getElementById(this.workspace.rootId);
      if (!w) return null;
      var tid = w.getAttribute("data-ems-nav-target");
      return tid ? document.getElementById(tid) : null;
    },
    workspaceTabCount: function () {
      return Object.keys(this.workspace.tabs).length;
    },
    /** multiTab=false 时 session 快照只保留一条（优先 activeTabId，否则首条） */
    trimNavTabsSnapshotIfSingleMode: function (data) {
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
    syncNavWorkspaceChrome: function () {
      var w = document.getElementById(this.workspace.rootId);
      if (!w) return;
      if (this.multiTab) {
        w.classList.remove("ems-nav--single-tab-mode");
      } else {
        w.classList.add("ems-nav--single-tab-mode");
      }
    },
    /** 标签栏最右一个 tab 的 id（溢出腾位）；DOM 对不上时用 seq 最大兜底。
     * @param {Array=} tabIdsPre 调用方已得到的 Object.keys(this.workspace.tabs)，可与外层共用避免重复遍历。 */
    pickLastNavTabIdForOverflowReplace: function (tabIdsPre) {
      var tabIds = (tabIdsPre && tabIdsPre.length !== undefined) ? tabIdsPre : Object.keys(this.workspace.tabs);
      tabIds = tabIds.filter(function (id) { return id !== NAV_WORKSPACE_HOME_TAB_ID; });
      var navBar = document.getElementById(this.workspace.listId);
      if (navBar) {
        var lis = navBar.querySelectorAll("li.ems-nav-tab");
        if (lis.length > 0) {
          var lastLi = lis[lis.length - 1];
          for (var di = 0; di < tabIds.length; di++) {
            var id = tabIds[di];
            var t = this.workspace.tabs[id];
            var liPick = t.navLink && t.navLink.parentElement;
            if (liPick === lastLi) return id;
          }
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
    ensureRoomForNewNavTab: function (targetEle) {
      if (!this.multiTab) return;
      var cap = this.workspace.maxTabCount;
      if (!(typeof cap === "number") || isNaN(cap) || cap < 1) return;
      var tabIdsEr = Object.keys(this.workspace.tabs).filter(function (id) { return id !== NAV_WORKSPACE_HOME_TAB_ID; });
      if (tabIdsEr.length < cap) return;
      var victim = this.pickLastNavTabIdForOverflowReplace(tabIdsEr);
      if (victim) this.closeNavTab(victim, targetEle || this.getNavWorkspaceTargetEl());
    },
    /** 恢复后若仍超过上限则反复去掉最右标签 */
    trimNavTabsDownToMaxCount: function (targetEle) {
      if (!this.multiTab) return;
      var cap = this.workspace.maxTabCount;
      if (!(typeof cap === "number") || isNaN(cap) || cap < 1) return;
      var te = targetEle || this.getNavWorkspaceTargetEl();
      var homeId = this.workspace.homeTabId;
      var guard = 0;
      while (guard < 128) {
        var tabIdsTrim = Object.keys(this.workspace.tabs);
        var embedOnly = tabIdsTrim.filter(function (id) { return id !== homeId; }).length;
        if (embedOnly <= cap) break;
        guard += 1;
        var victim = this.pickLastNavTabIdForOverflowReplace(tabIdsTrim);
        if (!victim) break;
        this.closeNavTab(victim, te);
      }
    },
    closeOtherNavTabs: function (keepTabId, targetEle) {
      var te = targetEle || this.getNavWorkspaceTargetEl();
      var ids = Object.keys(this.workspace.tabs).filter(function (id) {
        return id !== keepTabId && id !== NAV_WORKSPACE_HOME_TAB_ID;
      });
      for (var i = 0; i < ids.length; i++) this.closeNavTab(ids[i], te);
    },
    /** multiTab=false：下次打开 iframe/wujie 前清空嵌入实例与去重表，便于三种 openMode 切换 */
    clearNavTabsForSingleMenuSwitch: function (targetEle) {
      var ids = Object.keys(this.workspace.tabs);
      for (var i = 0; i < ids.length; i++) {
        this.closeNavTab(ids[i], targetEle);
      }
      this.workspace.tabByUrl = {};
      this.workspace.tabSeed = 0;
      if (targetEle) this.showNavWorkspace(targetEle);
    },
    /**
     * 工作台标签 openMode 规范为 ajax | iframe | wujie；按「路径 + openMode」去重。
     * multiTab=true 时三者均在工作台开标签；multiTab=false 时 ajax 仍走 #main，iframe/wujie 单槽。
     */
    normalizeNavOpenMode: function (k) {
      if (k === NAV_OPENMODE_IFRAME) return NAV_OPENMODE_IFRAME;
      if (k === NAV_OPENMODE_AJAX) return NAV_OPENMODE_AJAX;
      return NAV_OPENMODE_WUJIE;
    },
    navTabDedupeKey: function (urlPath, openMode) {
      return this.navUrlToPath(urlPath) + "\n" + this.normalizeNavOpenMode(openMode);
    },
    buildNavTabId: function (url, openMode) {
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
    /** data-nav-group 属性：appId@groupId */
    formatNavGroupAttr: function (appId, groupId) {
      var a = appId != null && appId !== "" ? String(appId) : "";
      var g = groupId != null && groupId !== "" ? String(groupId) : "";
      if (!a && !g) return "";
      return a + "@" + g;
    },
    parseNavGroupAttr: function (raw) {
      var out = { appId: "", groupId: "" };
      if (raw == null || String(raw).trim() === "") return out;
      var s = String(raw).trim();
      var at = s.indexOf("@");
      if (at < 0) {
        out.appId = s;
        return out;
      }
      out.appId = s.slice(0, at).trim();
      out.groupId = s.slice(at + 1).trim();
      return out;
    },
    /** 无界子应用实例名：app_${appId}_tab_${序号} */
    buildMicroAppName: function (appId, tabId) {
      var aid = String(appId != null && appId !== "" ? appId : "0").replace(/[^a-zA-Z0-9_-]/g, "_");
      var mx = /^ems_tab_(\d+)$/.exec(String(tabId || ""));
      var idx = mx ? mx[1] : String(tabId || "0").replace(/[^a-zA-Z0-9_-]/g, "_");
      return "app_" + aid + "_tab_" + idx;
    },
    getNavTabTitle: function (obj, fallback) {
      if (typeof obj == "object" && obj.tagName && obj.tagName.toLowerCase() == "a") {
        var titleEle = obj.querySelector("p");
        if (titleEle && titleEle.textContent) return titleEle.textContent.trim();
        var searchTitle = obj.querySelector(".search-title");
        if (searchTitle && searchTitle.textContent) return searchTitle.textContent.trim();
      }
      return fallback || "微前端";
    },
    /**
     * 工作台标签去重命中时已存在的 tab：激活并返回 true。
     */
    tryActivateExistingWorkspaceTab: function (urlPath, embedOpenMode) {
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
    prepareNewWorkspaceTabSlot: function (targetEle, urlPath, embedOpenMode) {
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
        navBar: navBar,
        tabBody: tabBody,
        tabId: tabId,
        absoluteUrl: this.resolveNavAbsoluteUrl(urlPath)
      };
    },
    /**
     * 在工作台标签栏与内容区追加一条标签 Shell DOM（li + panel 容器，不含 iframe/wujie 子应用内容）。
     * @param {{ prepend?: boolean, omitClose?: boolean, homeTab?: boolean }} [shellOpts]
     * @returns {{ navItem: HTMLElement, navLink: HTMLElement, panel: HTMLElement, closeBtn: HTMLElement|null }}
     */
    appendWorkspaceTabShell: function (navBar, tabBody, title, absoluteUrl, shellOpts) {
      shellOpts = shellOpts || {};
      var navItem = document.createElement("li");
      navItem.className = "nav-item ems-nav-tab";
      if (shellOpts.homeTab) navItem.classList.add("ems-nav-tab--home");
      var navLink = document.createElement("a");
      navLink.className = "nav-link ems-nav-tab-link d-flex align-items-center";
      navLink.href = "javascript:void(0)";
      navLink.setAttribute("role", "tab");
      var titleSpan = document.createElement("span");
      titleSpan.className = "ems-nav-tab-title text-truncate";
      titleSpan.textContent = title;
      titleSpan.title = title + "\n" + absoluteUrl;
      navLink.appendChild(titleSpan);
      var closeBtn = null;
      if (!shellOpts.omitClose) {
        closeBtn = document.createElement("button");
        closeBtn.type = "button";
        closeBtn.className = "btn btn-tool btn-sm ems-nav-tab-close ml-1 flex-shrink-0";
        closeBtn.title = "关闭";
        closeBtn.innerHTML = "&times;";
        navLink.appendChild(closeBtn);
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

      return { navItem: navItem, navLink: navLink, panel: panel, closeBtn: closeBtn };
    },
    /** 在 panel 内挂载 iframe 子页面（与快照恢复、菜单打开共用）。 */
    mountIframeInWorkspacePanel: function (panel, tabId, absoluteUrl) {
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
    buildWorkspaceAjaxMainId: function (tabId) {
      return "ems_ajax_" + String(tabId).replace(/[^a-zA-Z0-9_-]/g, "_");
    },
    /** 在工作台 panel 内挂载 ajax 片段区（与首页 welcome、菜单 ajax 共用）。 */
    mountAjaxInWorkspacePanel: function (panel, tabId, linkOrUrl) {
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
    /** 刷新工作台标签内容：用打开标签时记录的 tab.url 重新加载 */
    refreshNavTab: function (tabId) {
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
        var seq = (++this.workspace.openSeq);
        tab.seq = seq;
        this.startWujieAppForTab(this, tabId, seq, tab.appName, url);
      }
      this.activateNavTab(tabId, { skipHistory: true });
    },
    /** 工作台标签条：右键菜单（刷新 / 在新标签打开 / 关闭 / 关闭其他） */
    bindWorkspaceTabContextMenu: function (rootEl) {
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
        menu.innerHTML =
          '<a href="#" class="dropdown-item ems-nav-ctx-refresh" role="menuitem" tabindex="-1">' +
            '<i class="fa fa-sync-alt fa-fw" aria-hidden="true"></i>刷新' +
          '</a>' +
          '<a href="#" class="dropdown-item ems-nav-ctx-open-new" role="menuitem" tabindex="-1">' +
            '<i class="fa fa-external-link-alt fa-fw" aria-hidden="true"></i>在新标签打开' +
          '</a>' +
          '<div class="dropdown-divider"></div>' +
          '<a href="#" class="dropdown-item ems-nav-ctx-close" role="menuitem" tabindex="-1">' +
            '<i class="fa fa-times fa-fw" aria-hidden="true"></i>关闭' +
          '</a>' +
          '<a href="#" class="dropdown-item ems-nav-ctx-close-others" role="menuitem" tabindex="-1">' +
            '<i class="fa fa-minus-circle fa-fw" aria-hidden="true"></i>关闭其他' +
          '</a>';
        document.body.appendChild(menu);

        menu.addEventListener("click", function (e) {
          var item = e.target.closest && e.target.closest(".dropdown-item");
          if (!item) return;
          e.preventDefault();
          var tabId = menu.getAttribute("data-ems-tab-id");
          var te = navSelf.getNavWorkspaceTargetEl();
          if (item.classList.contains("ems-nav-ctx-refresh") && tabId) {
            navSelf.refreshNavTab(tabId);
          } else if (item.classList.contains("ems-nav-ctx-open-new") && tabId) {
            var tab = navSelf.workspace.tabs[tabId];
            if (tab && tab.url) {
              var openUrl = navSelf.resolveNavAbsoluteUrl(tab.url);
              if (openUrl) window.open(openUrl, "_blank", "noopener,noreferrer");
            }
          } else if (item.classList.contains("ems-nav-ctx-close-others") && tabId) {
            navSelf.closeOtherNavTabs(tabId, te);
          } else if (item.classList.contains("ems-nav-ctx-close") && tabId && tabId !== NAV_WORKSPACE_HOME_TAB_ID) {
            navSelf.closeNavTab(tabId, te);
          }
          menu.style.display = "none";
          menu.removeAttribute("data-ems-tab-id");
        });

        document.addEventListener("click", function () {
          menu.style.display = "none";
          menu.removeAttribute("data-ems-tab-id");
        });
        document.addEventListener("scroll", function () {
          menu.style.display = "none";
          menu.removeAttribute("data-ems-tab-id");
        }, true);
        document.addEventListener("keydown", function (e) {
          if (e.key === "Escape") {
            menu.style.display = "none";
            menu.removeAttribute("data-ems-tab-id");
          }
        });
      }

      var hideMenu = function () {
        menu.style.display = "none";
        menu.removeAttribute("data-ems-tab-id");
      };

      var navBar = document.getElementById(this.workspace.listId);
      if (!navBar || navBar.getAttribute("data-ems-tab-ctx-bound")) return;
      navBar.setAttribute("data-ems-tab-ctx-bound", "1");

      navBar.addEventListener("contextmenu", function (e) {
        if (!navSelf.multiTab) return;
        var tabItem = e.target.closest && e.target.closest(".ems-nav-tab");
        if (!tabItem) return;
        var tabId = tabItem.getAttribute("data-ems-tab-id");
        if (!tabId) return;
        e.preventDefault();
        menu.setAttribute("data-ems-tab-id", tabId);
        var isHome = tabId === NAV_WORKSPACE_HOME_TAB_ID;
        var closeItem = menu.querySelector(".ems-nav-ctx-close");
        var closeOthersItem = menu.querySelector(".ems-nav-ctx-close-others");
        var otherCount = Object.keys(navSelf.workspace.tabs).filter(function (id) {
          return id !== tabId && id !== NAV_WORKSPACE_HOME_TAB_ID;
        }).length;
        if (closeItem) closeItem.style.display = isHome ? "none" : "";
        if (closeOthersItem) closeOthersItem.style.display = otherCount > 0 ? "" : "none";
        navSelf.activateNavTab(tabId, { skipHistory: true });
        menu.style.display = "block";
        menu.style.left = e.clientX + "px";
        menu.style.top = e.clientY + "px";
      });
    },
    /** 工作台标签条 Shell（标题行 + panel 容器）上的切换 / 关闭交互。 */
    bindWorkspaceTabShellEvents: function (tabId, targetEle, navLink, closeBtn) {
      var navSelf = this;
      if (navLink.parentElement) {
        navLink.parentElement.setAttribute("data-ems-tab-id", tabId);
      }
      navLink.addEventListener("click", function (e) {
        if (e.target.closest && e.target.closest(".ems-nav-tab-close")) return;
        navSelf.activateNavTab(tabId);
      });
      if (closeBtn) {
        closeBtn.addEventListener("click", function (e) {
          e.preventDefault();
          e.stopPropagation();
          navSelf.closeNavTab(tabId, targetEle);
        });
      }
    },
    /**
     * 多标签 + 已配置 welcomeUrl：保证首枚「首页」标签存在（无关闭钮、ajax 加载 welcome），并插在列表最前。
     */
    ensureWorkspaceHomeTab: function (targetEle) {
      if (!this.multiTab || !this.welcomeUrl) return;
      if (this.workspace.tabs[this.workspace.homeTabId]) return;
      var navBar = document.getElementById(this.workspace.listId);
      var tabBody = document.getElementById(this.workspace.bodyId);
      if (!navBar || !tabBody) return;
      var te = targetEle || this.getNavWorkspaceTargetEl();
      var urlPath = this.navUrlToPath(this.welcomeUrl);
      var absoluteUrl = this.resolveNavAbsoluteUrl(urlPath);
      var shell = this.appendWorkspaceTabShell(navBar, tabBody, "首页", absoluteUrl, {
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
      shell.panel.appendChild(inner);
      var seq = (++this.workspace.openSeq);
      var tab = {
        id: this.workspace.homeTabId,
        url: urlPath,
        title: "首页",
        openMode: NAV_OPENMODE_AJAX,
        navLink: shell.navLink,
        panel: shell.panel,
        seq: seq,
        homeTab: true
      };
      this.workspace.tabs[this.workspace.homeTabId] = tab;
      this.bindWorkspaceTabShellEvents(this.workspace.homeTabId, te, shell.navLink, shell.closeBtn);
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
    showNavWorkspace: function (targetEle) {
      var rootEl = document.getElementById(this.workspace.rootId);
      if (rootEl) rootEl.style.display = "";
      if (targetEle) targetEle.style.display = "none";
    },
    hideNavWorkspace: function (targetEle) {
      var rootEl = document.getElementById(this.workspace.rootId);
      if (rootEl) rootEl.style.display = "none";
      if (targetEle) targetEle.style.display = "";
    },
    /**
     * 同源菜单入口 URL → 门户地址栏展示的 hash URL（与 beangle.history.convertUrl 一致，通常为 ...#/path...）。
     */
    menuEntryToHostUrl: function (entryHref) {
      if (!entryHref) return null;
      try {
        if (typeof beangle !== "undefined" && beangle.history && typeof beangle.history.convertUrl === "function") {
          return beangle.history.convertUrl(entryHref);
        }
        var u = new URL(entryHref, window.location.href);
        var path = u.pathname + u.search;
        return window.location.origin + window.location.pathname.replace(/[^/]+$/, "") + "#" + path;
      } catch (e) {
        return null;
      }
    },
    /**
     * 同站微应用 tab 记录的 url：pathname + search（与地址栏 # 后路径一致），不含 origin。
     */
    navUrlToPath: function (href) {
      if (href == null || href === "") return "";
      var s = String(href).trim();
      if (!s) return "";
      var hashIdx = s.indexOf("#");
      if (hashIdx >= 0) {
        var frag = s.substring(hashIdx + 1);
        if (frag.charAt(0) === "/") {
          try {
            var uFrag = new URL(frag, window.location.origin);
            return (uFrag.pathname || "/") + uFrag.search;
          } catch (eFrag) {
            return frag;
          }
        }
      }
      try {
        var u = new URL(s, window.location.href);
        return (u.pathname || "/") + u.search;
      } catch (e) {
        var noHash = s.split("#")[0];
        return noHash.charAt(0) === "/" ? noHash : s;
      }
    },
    /** 由 tab 记录的路径得到加载子应用用的绝对 URL */
    resolveNavAbsoluteUrl: function (pathOrUrl) {
      if (!pathOrUrl) return "";
      try {
        return new URL(pathOrUrl, window.location.href).href;
      } catch (e) {
        return pathOrUrl;
      }
    },
    /** 用于 hash / 子应用 URL 比对（忽略末尾多余 /） */
    normalizeContentUrlKey: function (href) {
      if (!href) return "";
      try {
        var u = new URL(href, window.location.href);
        var path = u.pathname.replace(/\/+$/, "") || "/";
        return (u.origin + path + u.search).toLowerCase();
      } catch (e) {
        return String(href).toLowerCase();
      }
    },
    urlsMatchForRouting: function (a, b) {
      if (!a || !b) return false;
      var ka = this.normalizeContentUrlKey(a);
      var kb = this.normalizeContentUrlKey(b);
      if (ka === kb) return true;
      if (ka.startsWith(kb + "/") || kb.startsWith(ka + "/")) return true;
      return false;
    },
    /** 内存 tab 是否对应当前目标地址（同源：比对 tabUrl 与 hash 形式的 menuEntryToHostUrl(tabUrl)） */
    tabEntryMatchesContentUrl: function (targetHref, tabUrl) {
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
        } catch (eH) { /* ignore */ }
      }
      return false;
    },
    /** preferredOpenMode：与当前菜单 openMode 一致（wujie|iframe），混合挂载时避免同 URL 串到另一类标签 */
    findSnapshotTabIdForUrl: function (snapshot, targetHref, preferredOpenMode) {
      if (!snapshot || !snapshot.tabs || !targetHref) return null;
      var tabs = snapshot.tabs;
      var want = preferredOpenMode != null && preferredOpenMode !== ""
        ? this.normalizeNavOpenMode(preferredOpenMode)
        : null;
      for (var i = 0; i < tabs.length; i++) {
        var s = tabs[i];
        if (!s || !s.id) continue;
        if (want != null && this.normalizeNavOpenMode(s.openMode) !== want) continue;
        if (this.tabEntryMatchesContentUrl(targetHref, s.url)) return s.id;
      }
      return null;
    },
    /**
     * 阶段一：解析 sessionStorage（NAV_TABS_SESSION_KEY）标签快照、URL 参数 group.id、hash 菜单；合并激活标签并写回 storage（如有变更）。
     * @returns {{ navTabsSnapshot: object, snapshotMutated: boolean, initialGroupId: *, initMenuLoc: object|null, menuHighlightOnly: boolean, menuHref: string|null }}
     */
    phase1ResolvePortalBootstrap: function () {
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
      } catch (e0) { /* keep defaults */ }
      var validTabs = [];
      for (var vi = 0; vi < data.tabs.length; vi++) {
        var vt = data.tabs[vi];
        if (vt && vt.id && vt.url) {
          vt.url = this.navUrlToPath(vt.url);
          validTabs.push(vt);
        }
      }
      data.tabs = validTabs;

      var initialGroupId = (this.initialGroupId != null && this.initialGroupId !== "") ? this.initialGroupId : null;
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
          if (data.tabs[ai].id === data.activeTabId) { activeSnap = data.tabs[ai]; break; }
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
        } catch (eP) { /* ignore */ }
      }

      return {
        navTabsSnapshot: data,
        snapshotMutated: snapshotMutated,
        initialGroupId: initialGroupId,
        initMenuLoc: initMenuLoc,
        menuHighlightOnly: menuHighlightOnly,
        menuHref: menuHref
      };
    },
    /** 写入 sessionStorage（模块内 NAV_TABS_SESSION_KEY；多标签工作台快照） */
    persistNavTabsSession: function () {
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
          ids = Object.keys(this.workspace.tabs).filter(function (id) {
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
          tabs.push(row);
        }
        var activeTabId = !this.multiTab ? (ids.length ? ids[0] : null) : this.workspace.activeTabId;
        sessionStorage.setItem(NAV_TABS_SESSION_KEY, JSON.stringify({
          activeTabId: activeTabId,
          tabs: tabs
        }));
      } catch (e) {
        if (window.console && console.warn) console.warn("[ems] persistNavTabsSession failed", e);
      }
    },
    /** 浏览器前进后退：根据 history.state 切换工作台标签（需先 ensureNavTabHistoryListener） */
    activateNavTab: function (tabId, opts) {
      opts = opts || {};
      var tab = this.workspace.tabs[tabId];
      if (!tab) return;
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
          var on = (id === tabId);
          t.navLink.classList.toggle("active", on);
          var liAct = t.navLink && t.navLink.parentElement;
          if (liAct) liAct.classList.toggle("ems-nav-tab--active", on);
          t.panel.style.display = (on ? "" : "none");
        }
      }
      this.workspace.activeTabId = tabId;
      var liScroll = tab.navLink && tab.navLink.parentElement;
      if (liScroll && liScroll.scrollIntoView) {
        try { liScroll.scrollIntoView({ block: "nearest", inline: "nearest", behavior: "smooth" }); } catch (err) { liScroll.scrollIntoView(false); }
      }
      var hostUrl = this.menuEntryToHostUrl(tab.url);
      if (!opts.skipHistory && hostUrl) {
        try {
          var useReplace = !!(opts.replaceState || this.workspaceTabCount() <= 1);
          var method = useReplace ? "replaceState" : "pushState";
          window.history[method]({ emsNavTab: tabId }, "", hostUrl);
        } catch (eH) { /* ignore */ }
      }
      this.persistNavTabsSession();
    },
    closeNavTab: function (tabId, targetEle) {
      if (tabId === this.workspace.homeTabId) return;
      var tab = this.workspace.tabs[tabId];
      if (!tab) return;
      var te = targetEle || this.getNavWorkspaceTargetEl();
      var wujieRtClose = resolveWujieRuntime();
      if (tab.openMode !== NAV_OPENMODE_IFRAME && tab.appName && wujieRtClose && wujieRtClose.destroyApp) {
        try { wujieRtClose.destroyApp(tab.appName); } catch (e) { console.warn('[ems] wujie destroyApp', e); }
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
    teardownNavWorkspace: function (targetEle) {
      var te = targetEle || this.getNavWorkspaceTargetEl();
      var homeId = this.workspace.homeTabId;
      var ids = Object.keys(this.workspace.tabs).filter(function (id) { return id !== homeId; });
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
      } catch (eR) { /* ignore */ }
      this.persistNavTabsSession();
    },
    /** 注册 popstate：后退/前进时在工作台标签间切换（依赖 history.state.emsNavTab） */
    ensureNavTabHistoryListener: function () {
      if (window.__emsNavTabHistoryBound) return;
      window.__emsNavTabHistoryBound = true;
      window.addEventListener("popstate", function (ev) {
        var n = (typeof window.ems !== "undefined" && window.ems.getNav) ? window.ems.getNav() : null;
        if (!n || !n.workspace) return;
        var st = ev.state;
        if (st && st.emsNavTab && n.workspace.tabs[st.emsNavTab]) {
          n.activateNavTab(st.emsNavTab, { skipHistory: true });
        }
      });
    },
    startWujieAppForTab: function (that, tabId, seq, tabAppName, absoluteUrl) {
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
        return !wujieAlive || wujieFetchNoStore ||
          !!(that.params && (that.params["wujieEntryReload"] === true || that.params["wujieEntryReload"] === "true"));
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
        var errMissing = new Error('无界运行时未就绪：请确保页面已加载无界并在 window.wujie 上提供 startApp。');
        console.error("[ems] wujie startApp", errMissing);
        if (current && current.panel) {
          current.panel.innerHTML = '<div class="p-3 text-danger">子应用加载失败：' + errMissing.message + '</div>';
        }
        return Promise.reject(errMissing);
      }
      if (!current || current.seq !== seq) return Promise.resolve();
      if (!document.body.contains(current.panel)) return Promise.resolve();
      try {
        if (W.destroyApp) W.destroyApp(tabAppName);
      } catch (eDes) { /* ignore */ }
      try {
        current.panel.innerHTML = "";
      } catch (eCl) { /* ignore */ }
      var entryFetchUrl = wujieBustEntryUrl(absoluteUrl);
      var startOpts = {
        name: tabAppName,
        url: entryFetchUrl,
        el: current.panel,
        sync: wujieSync,
        alive: wujieAlive,
        fiber: wujieFiber,
        loadError: function (url, err) {
          console.error("[ems] wujie loadError", url, err);
        },
        fetch: function (input, init) {
          init = Object.assign({ mode: "cors" }, init || {}, { credentials: "include" });
          if (wujieFetchNoStore || !wujieAlive) {
            init.cache = "no-store";
          }
          return fetch(input, init).catch(function () {
            return fetch(input, Object.assign({}, init, { credentials: "omit" }));
          });
        }
      };
      var iframeSrcRaw = (that.params && that.params["wujieIframeSrc"]) || "";
      if (iframeSrcRaw) {
        try {
          startOpts.attrs = {
            src: new URL(iframeSrcRaw, window.location.href).href
          };
        } catch (eIs) {
          console.warn("[ems] wujie iframe 初始地址无效:", iframeSrcRaw, eIs);
        }
      }
      startOpts.props = {
        jump: function (location, query) {
          var urlRaw = "";
          if (typeof location === "string") {
            urlRaw = location;
          } else if (location && typeof location === "object") {
            urlRaw = location.href || location.url || location.path || "";
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
          Promise.resolve()
            .then(function () {
              return W.destroyApp(tabAppName);
            })
            .then(function () {
              var jumpFetchUrl = wujieBustEntryUrl(abs);
              var next = Object.assign({}, opts, { url: jumpFetchUrl });
              return W.startApp(next);
            })
            .then(function () {
              var hostUrl = that.menuEntryToHostUrl(t.url);
              if (that.workspace.activeTabId === tabId && hostUrl) {
                try {
                  window.history.pushState({ emsNavTab: tabId }, "", hostUrl);
                } catch (ePu) { /* ignore */ }
              }
              that.persistNavTabsSession();
              try {
                window.dispatchEvent(new Event("resize"));
              } catch (eR) {
                /* ignore */
              }
            })
            .catch(function (err) {
              console.error("[ems] wujie props.jump", err);
              window.location.assign(abs);
            });
        }
      };
      current.wujieStartOpts = startOpts;
      return W.startApp(startOpts).then(function () {
        try {
          if (current.panel && current.panel.getBoundingClientRect().height < 8) {
            var sbMin = document.getElementById("main_siderbar");
            current.panel.style.minHeight = sbMin ? sbMin.offsetHeight + "px" : "480px";
          }
          window.dispatchEvent(new Event("resize"));
        } catch (eR) { /* ignore */ }
      }).catch(function (e) {
        console.error("[ems] wujie startApp", e);
        var curErr = that.workspace.tabs[tabId];
        if (curErr && curErr.panel) {
          var msg = (e && e.message) ? e.message : String(e);
          var hint = "";
          if (/NetworkError|Failed to fetch|fetch/i.test(msg)) {
            hint = '<p class="small text-muted mt-2 mb-0">跨域时无界会用 fetch 拉取子应用 HTML/资源，子应用需对门户 Origin 返回 CORS；使用 Cookie 时须 Access-Control-Allow-Credentials: true 且 Allow-Origin 不能为 *。门户固定先按 credentials=include 请求，失败后会自动用 omit 重试一次。</p>';
          }
          curErr.panel.innerHTML = '<div class="p-3 text-danger">子应用加载失败：' + msg + hint + '</div>';
        }
      });
    },
    /**
     * 刷新后根据 sessionStorage（NAV_TABS_SESSION_KEY）重建多标签工作台；最后激活上次选中标签。
     * @param prefetched 若已由 restoreNav 解析好则可传入，避免重复读取；省略或传 null 时再读 sessionStorage。
     * @param opts.preferredTabId 若地址栏 hash 已指向某标签内容，可指定优先激活的标签 id（须在快照 tabs 内）。
     * @param opts.skipSidebarInFinalize 为 true 时不根据激活标签改左侧分组（由 restoreNav 阶段二统一 displayGroupMenus）。
     * @returns {Promise<void>} 无快照或无需异步链时返回 resolved Promise。
     */
    restoreNavTabsFromSession: function (prefetched, opts) {
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

      /**
       * 根据 session 快照中的一条记录，在工作台里恢复「一条」标签（DOM + workspace.tabs 条目）：
       * - 校验 id/url，已存在同 id 则跳过（幂等）；
       * - 解析 snap.navGroup、归一化 openMode，写入 tabByUrl 去重键；
       * - 创建顶部标签 li、内容区 panel；iframe 直接设 src；wujie 则登记 appName 后走 startWujieAppForTab（返回 Promise）；
       * - 绑定切换 / 关闭事件。
       * 不负责选中标签、改地址栏或左侧菜单，这些事在链结束的 finalizeNavTabsRestore 里做。
       */
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
        var title = snap.title || absoluteUrl.replace(/^https?:\/\/[^/]+/, "") || "微前端";
        var shell = that.appendWorkspaceTabShell(navBar, tabBody, title, absoluteUrl);
        var navLink = shell.navLink;
        var panel = shell.panel;
        var closeBtn = shell.closeBtn;
        var navGroupStr = (snap.navGroup != null && String(snap.navGroup).trim() !== "")
          ? String(snap.navGroup).trim()
          : that.formatNavGroupAttr(snapNg.appId, snapNg.groupId);

        var seq = (++that.workspace.openSeq);
        var tab = {
          id: tabId,
          url: url,
          title: title,
          openMode: om,
          navGroup: navGroupStr,
          navLink: navLink,
          panel: panel,
          seq: seq
        };
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
        if (om === NAV_OPENMODE_IFRAME || om === NAV_OPENMODE_AJAX) {
          return Promise.resolve();
        }
        return that.startWujieAppForTab(that, tabId, seq, tabAppName, absoluteUrl);
      }

      var expectTabCount = data.tabs.length;
      var chain = Promise.resolve();
      for (var ri = 0; ri < data.tabs.length; ri++) {
        (function (snap) {
          chain = chain.then(function () {
            return Promise.resolve(appendRestoredTab(snap)).catch(function (eTab) {
              if (window.console && console.warn) console.warn("[ems] restoreNavTabsFromSession tab failed", snap && snap.id, eTab);
            });
          });
        })(data.tabs[ri]);
      }
      /**
       * 异步恢复链（逐个 appendRestoredTab，含 wujie startApp）全部结束后执行：
       * 1）按优先级选中一条工作台标签：opts.preferredTabId → 快照 activeTabId → 快照首条；
       * 2）activateNavTab（skipHistory，避免与恢复过程重复写 history）；
       * 3）若未 skipSidebarInFinalize：replaceState 对齐地址栏，并按该标签的分组/应用刷新左侧菜单（displayGroupMenus / locateMenu 兜底）；
       * 4）若确有标签则 trim 超限并 persistNavTabsSession；若期望有条但一条未成则打告警。
       * Promise 链出错时也会在 catch 里调用本函数，尽量保证侧栏与快照一致。
       */
      function finalizeNavTabsRestore() {
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
            } catch (eRs) { /* ignore */ }
          }
        }
        if (actTab && !opts.skipSidebarInFinalize) {
          var ngFin = that.parseNavGroupAttr(actTab.navGroup);
          var gid = ngFin.groupId;
          var aid = ngFin.appId;
          if (gid !== undefined && gid !== null && String(gid) !== "") {
            var openAppId = (aid !== undefined && aid !== null && String(aid) !== "") ? aid : undefined;
            if (openAppId !== undefined) {
              that.displayGroupMenus(gid, openAppId);
            } else {
              that.displayGroupMenus(gid);
            }
          } else if (actTab.url) {
            var ml = that.locateMenu(actTab.url);
            if (ml) {
              var entry = ml.app && ml.app.app ? ml.app.app : ml.app;
              var appOpenId = entry && entry.id != null ? entry.id : undefined;
              that.displayGroupMenus(ml.group.id, appOpenId);
            }
          }
        }
        var got = Object.keys(that.workspace.tabs).length;
        if (got > 0) {
          that.trimNavTabsDownToMaxCount(targetEle);
          that.persistNavTabsSession();
        } else if (expectTabCount > 0 && window.console && console.warn) {
          console.warn("[ems] restoreNavTabsFromSession: 预期恢复 " + expectTabCount + " 个标签但实际为 0，未写入空快照以免丢失 session");
        }
      }
      return chain.then(finalizeNavTabsRestore).catch(function (err) {
        if (window.console && console.warn) console.warn("[ems] restoreNavTabsFromSession chain", err);
        finalizeNavTabsRestore();
      });
    },
    /**
     * 左侧/顶部菜单：<a data-open-mode="iframe|wujie">，缺省或非合法值按 iframe。
     * - multiTab=true（默认）：iframe / wujie 在工作台开标签；ajax 仅用于内部首页 welcome。
     * - multiTab=false：iframe/wujie 进工作台单槽；#main 供内部 ajax 片段。
     * openMode 由 collectApps 推导：navStyle=wujie 否则 iframe。
     */
    openMenu: function (obj) {
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
          console.warn('[ems] openMenu: missing container #' + target);
          return false;
        }
        if (openMode !== NAV_OPENMODE_WUJIE && openMode !== NAV_OPENMODE_IFRAME) {
          openMode = NAV_OPENMODE_IFRAME;
        }

        var mainWrapper = targetEle.parentNode;
        if (openMode === NAV_OPENMODE_WUJIE) {
          var that = this;
          var rawHref = (typeof obj == "object" && obj != null && obj.href) ? String(obj.href) : "";
          if (!rawHref) {
            console.warn('[ems] openMenu: wujie 需要带 href 的 <a> 菜单链接');
            return false;
          }
          var url = this.navUrlToPath(rawHref);
          this.ensureNavWorkspace(mainWrapper, targetEle);
          this.showNavWorkspace(targetEle);
          this.syncNavWorkspaceChrome();
          if (this.tryActivateExistingWorkspaceTab(url, NAV_OPENMODE_WUJIE)) return false;
          var slotW = this.prepareNewWorkspaceTabSlot(targetEle, url, NAV_OPENMODE_WUJIE);
          if (!slotW) {
            console.error('[ems] openMenu: 嵌入工作台 DOM 未就绪（缺少标签栏或内容区）');
            return false;
          }
          var title = this.getNavTabTitle(obj, rawHref || url);
          var shellW = this.appendWorkspaceTabShell(slotW.navBar, slotW.tabBody, title, slotW.absoluteUrl);
          var seq = (++this.workspace.openSeq);
          var tabAppName = this.buildMicroAppName(navAppIdFromLink, slotW.tabId);
          var tab = {
            id: slotW.tabId,
            url: url,
            title: title,
            openMode: NAV_OPENMODE_WUJIE,
            appName: tabAppName,
            navGroup: this.formatNavGroupAttr(navAppIdFromLink, navGroupIdFromLink),
            navLink: shellW.navLink,
            panel: shellW.panel,
            seq: seq
          };
          this.workspace.tabs[slotW.tabId] = tab;
          this.bindWorkspaceTabShellEvents(slotW.tabId, targetEle, shellW.navLink, shellW.closeBtn);
          this.activateNavTab(slotW.tabId);
          this.startWujieAppForTab(that, slotW.tabId, seq, tabAppName, slotW.absoluteUrl);
          return false;
        }
        if (openMode === NAV_OPENMODE_IFRAME) {
          var rawHrefIf = (typeof obj == "object" && obj != null && obj.href) ? String(obj.href) : "";
          if (!rawHrefIf) {
            console.warn('[ems] openMenu: iframe 需要带 href 的 <a> 菜单链接');
            return false;
          }
          var urlIf = this.navUrlToPath(rawHrefIf);
          this.ensureNavWorkspace(mainWrapper, targetEle);
          this.showNavWorkspace(targetEle);
          this.syncNavWorkspaceChrome();
          if (this.tryActivateExistingWorkspaceTab(urlIf, NAV_OPENMODE_IFRAME)) return false;
          var slotIf = this.prepareNewWorkspaceTabSlot(targetEle, urlIf, NAV_OPENMODE_IFRAME);
          if (!slotIf) {
            console.error('[ems] openMenu: iframe 多标签工作台 DOM 未就绪');
            return false;
          }
          var titleIf = this.getNavTabTitle(obj, rawHrefIf || urlIf);
          var shellIf = this.appendWorkspaceTabShell(slotIf.navBar, slotIf.tabBody, titleIf, slotIf.absoluteUrl);
          var iframeMain = this.mountIframeInWorkspacePanel(shellIf.panel, slotIf.tabId, slotIf.absoluteUrl);
          var seqIf = (++this.workspace.openSeq);
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
            if (targetEle.tagName == 'DIV') {
              beangle.Go(obj, target);
            } else if (targetEle.tagName == 'IFRAME') {
              mainWrapper.removeChild(targetEle);
              var f = document.createElement('div')
              f.setAttribute("width", "100%");
              f.setAttribute("height", "100%");
              f.setAttribute("class", "ajax_container");
              f.id = target;
              mainWrapper.appendChild(f);
              beangle.Go(obj, target);
            }
            return false;
          }
          var rawHrefAjax = (typeof obj == "object" && obj != null && obj.href) ? String(obj.href) : "";
          if (!rawHrefAjax) {
            console.warn('[ems] openMenu: ajax 需要带 href 的 <a> 菜单链接');
            return false;
          }
          var urlAjax = this.navUrlToPath(rawHrefAjax);
          this.ensureNavWorkspace(mainWrapper, targetEle);
          this.showNavWorkspace(targetEle);
          this.syncNavWorkspaceChrome();
          if (this.tryActivateExistingWorkspaceTab(urlAjax, NAV_OPENMODE_AJAX)) return false;
          var slotA = this.prepareNewWorkspaceTabSlot(targetEle, urlAjax, NAV_OPENMODE_AJAX);
          if (!slotA) {
            console.error('[ems] openMenu: ajax 多标签工作台 DOM 未就绪');
            return false;
          }
          var titleA = this.getNavTabTitle(obj, rawHrefAjax || urlAjax);
          var shellA = this.appendWorkspaceTabShell(slotA.navBar, slotA.tabBody, titleA, slotA.absoluteUrl);
          var ajaxInner = this.mountAjaxInWorkspacePanel(shellA.panel, slotA.tabId, obj);
          var seqA = (++this.workspace.openSeq);
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
        console.error('[ems] openMenu', eOm);
        return false;
      }
    },
    fillAppName: function () {
      if (this.sysName.length > 6) {
        jQuery('#appName').css("font-size", "0.875rem")
      }
      jQuery('#appName').html(this.sysName);
    },
    /**
     * 添加顶层groups
     */
    addTopGroups: function (jqueryElem) {
      var appItem = '';
      var topItemCount = 0;
      var topMoreHappened = false;
      this.fillAppName();
      prependApps(jqueryElem, this, this.apps, true)

      for (var i = 0; i < this.groups.length; i++) {
        var group = this.groups[i];
        topItemCount += 1;
        if (topItemCount == this.maxTopItem && this.groups.length > this.maxTopItem) {
          jqueryElem.append('<li class="nav-item dropdown"><a href="#" data-toggle="dropdown" class="dropdown-toggle nav-link">更多...</a><div id="topMore" aria-labelledby="navbarDropdown" class="dropdown-menu"></div><li>');
          topMoreHappened = true;
        }
        if (topMoreHappened) {
          jqueryElem = jQuery('#topMore');
        }
        if (topMoreHappened) {
          appItem = this.dropdownGroupNavTemplate.replace('{group.id}', group.id);
        } else {
          appItem = this.groupTemplate.replace('{group.id}', group.id);
        }
        appItem = appItem.replace('{group.title}', group.title);
        appItem = appItem.replace('{group.name}', group.name);
        appItem = appItem.replace('{active_class}', (i == 0) ? "active" : "");
        jqueryElem.append(appItem);
      }
    },

    displayAppMenus: function (appName) {
      var groupId = 0, appId = 0;
      if (appName == this.portal.name) {
        groupId = this.portal.group.id;
      } else {
        var apps = this.apps;
        for (var i = 0; i < apps.length; i++) {
          if (apps[i].name == appName) {
            appId = apps[i].id;
            groupId = apps[i].group.id;
            break;
          }
        }
      }
      if (!groupId) {
        console.log("error app-name " + appName);
        groupId = this.groupMenus[0].group.id
      }
      this.displayGroupMenus(groupId, appId)
    },
    /**显示指定group的menu；navOpts.highlightOnly 为 true 时对 menuObj 仅展开父级并高亮，不 trigger 打开（用于已从快照恢复的无界标签）。*/
    displayGroupMenus: function (groupId, appId, menuObj, navOpts) {
      navOpts = navOpts || {};
      var gid = String(groupId);
      var aid = appId != null && appId !== "" ? String(appId) : "";
      var sameGroup = this._displayedGroupId === gid && this._displayedAppId === aid && !menuObj;
      switchNavActive("#group_" + groupId);
      for (var i = 0; i < this.groupMenus.length; i++) {
        var groupMenu = this.groupMenus[i];
        if (groupMenu.group.id == groupId) {
          document.title = groupMenu.group.title;
          if (!sameGroup) {
            var openMenuId = appId;
            if (groupMenu.appMenus.length > 0 && !openMenuId) {
              openMenuId = groupMenu.appMenus[0].app.id;
            }
            if (groupMenu.appMenus.length == 1) {
              var onlyOneAppMenu = groupMenu.appMenus[0]
              if (onlyOneAppMenu.menus.length > 0) openMenuId = onlyOneAppMenu.menus[0].id;
              this.createMenus(jQuery('#' + this.menuDomId), onlyOneAppMenu.menus, openMenuId);
            } else {
              this.createMenus(jQuery('#' + this.menuDomId), groupMenu.appMenus, openMenuId);
            }
            this._displayedGroupId = gid;
            this._displayedAppId = aid;
          }
          break;
        }
      }
      if (menuObj && menuObj.entry) {
        var menu = jQuery('#main_siderbar a[href="' + menuObj.entry + '"]')
        menu.parents('li').each(function (i, li) {
          jQuery(li).addClass('menu-open');
          jQuery(li).siblings().each(function (i, sli) {
            jQuery(sli).removeClass('menu-open menu-is-opening');
            jQuery(sli).children('ul').removeAttr('style');
          }
          );
        });
        if (navOpts.highlightOnly) {
          jQuery("#" + this.menuDomId + " a.nav-link.active").removeClass("active");
          menu.parent('li').siblings().each(function (i, li) { jQuery(li).children('a').removeClass('active'); });
          menu.addClass('active');
        } else {
          this.openMenu(menu[0]);
        }
      }
    },
    /** 菜单查找*/
    search: function (name, limit) {
      var results = this.searchMenu(name, limit);
      var searchRegExp = new RegExp(name, 'gi');
      var resultDom = jQuery(".sidebar-search-results .list-group");
      resultDom.empty();
      if (results.length == 0) {
        var notfound = { 'name': "找不到结果", 'link': '#', 'path': [], 'target': "main", 'openMode': NAV_OPENMODE_AJAX, 'navGroupAttr': '' }
        resultDom.append(this.renderSearchItem(searchRegExp, notfound))
      } else {
        for (var i = 0; i < results.length; i++) {
          var result = results[i];
          resultDom.append(this.renderSearchItem(searchRegExp, result));
        }
      }
      this.openSearchResults();
    },

    openSearchResults: function () {
      var searchDom = jQuery('#' + this.searchInputId).parent().parent();
      searchDom.addClass("sidebar-search-open");
      searchDom.find(".input-group-append .btn i").removeClass("fa-search").addClass("fa-times");
    },

    closeSearchResults: function () {
      var searchDom = jQuery('#' + this.searchInputId).parent().parent();
      searchDom.removeClass("sidebar-search-open");
      searchDom.find(".input-group-append .btn i").removeClass("fa-times").addClass("fa-search");
    },

    toggleSearchResults: function () {
      var searchDom = jQuery('#' + this.searchInputId).parent().parent();
      if (searchDom.hasClass("sidebar-search-open")) {
        this.closeSearchResults();
      } else {
        this.openSearchResults();
      }
    },

    renderSearchItem: function (searchRegExp, result) {
      var pathStr = result.path.join(" -> ");
      var name = result.name.replace(searchRegExp, function (str) {
        return "<strong>" + str + "</strong>";
      });
      var item = null;
      if (result.link != '#') {
        item = jQuery('<a/>', { href: decodeURIComponent(result.link), class: 'list-group-item', target: result.target });
        var som = result.openMode || NAV_OPENMODE_AJAX;
        item.attr('data-open-mode', som);
        item.attr('data-nav-group', result.navGroupAttr || '');
      } else {
        item = jQuery('<a/>', { href: decodeURIComponent(result.link), class: 'list-group-item', target: result.target });
      }
      var searchTitleElement = jQuery('<div/>', { class: 'search-title' }).html(name);
      var searchPathElement = jQuery('<div/>', { class: 'search-path' }).html(pathStr);
      item.append(searchTitleElement).append(searchPathElement);
      return item;
    }
  }

  /**
   * 切换顶部导航栏上的按钮
   */
  function switchNavActive(anchorId) {
    if (jQuery(anchorId).parent()[0].tagName == "LI") {
      jQuery(anchorId).parent().siblings().each(function (i, li) { jQuery(li).children("a").removeClass("active") });
      jQuery(anchorId).addClass("active");
    } else {
      jQuery(anchorId).siblings().each(function (i, a) { jQuery(a).removeClass("active") });
      jQuery(anchorId).addClass("active");
    }
  }

  /**
   * 添加app导航,是app内部菜单之外的所有app的展示panel
   */
  function prependApps(jqueryElem, nav, apps, autohide) {
    var appDropNav = '<ul class="nav navbar-nav"><li class="nav-item dropdown">' +
      '<a href="#" data-toggle="dropdown" class="nav-link dropdown-toggle {autohide}" role="button" title="应用" aria-haspopup="true" aria-expanded="true"><i class="fas fa-th"></i></a>' +
      '<div id="app_drop_bar" class="dropdown-menu columns-3"></div>' +
      '</li></ul>';
    var appTemplate = '<a href="{app.base}" class="dropdown-item {active_class}" target="_top">{app.title}</a>';
    jqueryElem.before(appDropNav.replace("{autohide}", autohide ? "app-toggle" : ""));
    var appDropBarID = "#app_drop_bar";
    jqueryElem = jQuery(appDropBarID);
    var curGroupId = 0;
    if (!apps) {
      apps = nav.apps;
    }
    var columRows = Math.ceil(apps.length / 3);
    var content = '<div class="row">';
    var columnApps = [[], [], []]
    for (var i = 0; i < apps.length; i++) {
      columnApps[Math.floor(i / columRows)].push(apps[i]);
    }
    for (var column = 0; column < columnApps.length; column++) {
      var columnApp = columnApps[column];
      var columnDiv = '<div class="col-sm-4">'
      for (var i = 0; i < columnApp.length; i++) {
        var app = columnApp[i];
        if (app.group) {
          if (curGroupId == 0) {
            curGroupId = app.group.id;
          } else {
            if (app.group.id != curGroupId) {
              if (i > 0) {
                columnDiv += '<div class="dropdown-divider"></div>';
              }
              curGroupId = app.group.id;
            }
          }
        }
        if (app.name == nav.app.name) {//添加左侧的标题
          columnDiv += '<a  class="dropdown-item active" href="#">' + app.title + '</a>';
        } else {
          var appendHtml = appTemplate.replace('{app.base}', nav.processUrl(app.base));
          appendHtml = appendHtml.replace('{app.title}', app.title);
          appendHtml = appendHtml.replace('{active_class}', "");
          columnDiv += appendHtml;
        }
      }
      columnDiv += "</div>";
      content += columnDiv;
    }
    content += "</div>";
    jqueryElem.append(content);
  }

  var nav = {};

  function createNav(app, portal, domainMenus, params, displayFirstGroup) {
    nav = new Nav(app, portal, domainMenus, params);
    nav.ensureNavTabHistoryListener();
    nav.addTopGroups(jQuery('#' + nav.navDomId));
    nav.bindTopGroupNav();
    nav.activate();
    nav.bindSearchMenuOpen();
    var resolvedInitialGroup = false;
    var wantGid = nav.initialGroupId;
    if (wantGid != null && wantGid !== "") {
      for (var gi = 0; gi < nav.groupMenus.length; gi++) {
        if (String(nav.groupMenus[gi].group.id) === String(wantGid)) {
          nav.displayGroupMenus(nav.groupMenus[gi].group.id);
          resolvedInitialGroup = true;
          break;
        }
      }
    }
    if (!resolvedInitialGroup && displayFirstGroup && nav.groups.length > 0) {
      nav.displayGroupMenus(nav.groups[0].id);
    }
    return nav;
  }

  /**
   * 切换group的全局函数
   * @param id
   * @param name
   * @returns
   */
  function changeGroup(ele) {
    var id = ele.id
    nav.displayGroupMenus(id.substring("group_".length));
  }

  function createProfileNav() {
    var profileSelectTemplate =
      '<li class="nav-item dropdown">' +
      '<a class="dropdown-toggle nav-link" data-toggle="dropdown" href="#" id="profile_switcher" aria-expanded="false">{first}</a> ' +
      '<div class="dropdown-menu">{list}</div>' +
      '</li>';
    var profileTemplate = '<a href="{profile.url}" class="dropdown-item">{profile.name}</a>'
    var profiles = config.profiles;
    if (profiles.length > 1) { //display profile when multi profile occur
      var profile = config.profile;
      var profilehtml = profileSelectTemplate.replace('{first}', profile.name);
      var list = "";
      for (var i = 0; i < profiles.length; i++) {
        if (profiles[i].id != profile.id) {
          var profileItem = profileTemplate.replace("{profile.url}", profiles[i].url);
          profileItem = profileItem.replace("{profile.name}", profiles[i].name);
          list += profileItem
        }
      }
      profilehtml = profilehtml.replace('{list}', list);
      jQuery('.main-header > .ml-auto').prepend(profilehtml)
    }
  }

  function fetchMessages(params) {
    if (!sameDomain(window.location.href, params['webapp'])) {
      return;
    }
    jQuery.ajax({
      url: params['webapp'] + '/portal/user/message/newly?callback=ems.messageCallBack', cache: false,
      type: "GET", dataType: "html",
      complete: function (jqXHR) {
        try {
          jQuery("#newly-message").html(jqXHR.responseText);
        } catch (e) { alert(e) }
      }
    });
    jQuery.ajax({
      url: params['webapp'] + '/portal/user/todo/newly?callback=ems.taskCallBack', cache: false,
      type: "GET", dataType: "html",
      complete: function (jqXHR) {
        try {
          jQuery("#newly-task").html(jqXHR.responseText);
        } catch (e) { alert(e) }
      }
    });
  }

  function setLocal(name, value) {
    if (localStorage) {
      if (value) {
        localStorage.setItem(name, value);
      } else {
        localStorage.removeItem(name);
      }
    }
  }
  function getLocal(name, defaultValue) {
    if (localStorage) {
      return (localStorage.getItem(name) || defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** 读本地 multiTab 偏好；无记录时默认 true（多标签）。 */
  function getMultiTabPreference() {
    var v = getLocal(NAV_MULTI_TAB_STORAGE_KEY, "1");
    return v !== "0" && v !== "false";
  }

  function setMultiTabPreference(enabled) {
    setLocal(NAV_MULTI_TAB_STORAGE_KEY, enabled ? "1" : "0");
  }

  /**
   * 合并服务端/门户 params.multiTab 与 localStorage；params 已有合法值时优先 params。
   * @returns {"true"|"false"} 供 createNav params 使用
   */
  function resolveMultiTabParam(explicit) {
    if (explicit !== undefined && explicit !== null && String(explicit).trim() !== "") {
      var s = String(explicit).trim().toLowerCase();
      if (s === "false" || s === "0") return "false";
      if (s === "true" || s === "1") return "true";
      return explicit;
    }
    return getMultiTabPreference() ? "true" : "false";
  }

  function shell() {
    return jQuery(".wrapper").first();
  }

  function initShellLayout() {
    jQuery(document).off("click.emsShell");
    jQuery(document).on("click.emsShell", "[data-ems-pushmenu]", function (e) {
      e.preventDefault();
      if (window.innerWidth <= 991.98) {
        shell().toggleClass("sidebar-open");
      } else {
        shell().toggleClass("sidebar-collapse");
      }
    });
    jQuery(document).on("click.emsShell", "[data-ems-control-sidebar]", function (e) {
      e.preventDefault();
      shell().toggleClass("control-sidebar-slide-open");
    });
    jQuery(document).on("click.emsShell", "[data-ems-fullscreen]", function (e) {
      e.preventDefault();
      var docEl = document.documentElement;
      if (!document.fullscreenElement && docEl.requestFullscreen) {
        docEl.requestFullscreen();
      } else if (document.exitFullscreen) {
        document.exitFullscreen();
      }
    });
    jQuery(document).on("click.emsShell", ".control-sidebar-bg", function (e) {
      e.preventDefault();
      shell().removeClass("control-sidebar-slide-open");
    });
  }

  function applyStickyHeader(enabled) {
    if (enabled) {
      shell().addClass("layout-navbar-fixed");
    } else {
      shell().removeClass("layout-navbar-fixed");
    }
  }

  function setup(theme, params) {
    nav.theme = theme;
    initShellLayout();
    shell().addClass("sidebar-mini layout-fixed");
    jQuery("body").addClass("text-sm");
    document.documentElement.style.setProperty("scrollbar-width", "thin");
    fetchMessages(params);
    var stickyHeader = getLocal(NAV_STICKY_HEADER_STORAGE_KEY, "1")
    if (stickyHeader == "1") {
      applyStickyHeader(true);
      jQuery("#sticky_header").prop("checked", true);
    }

    jQuery("#control_sidebar input[name=root_font_size]").on("click", function (e) { changeFontSize(jQuery(e.target).val()) });
    jQuery("#sticky_header").on("click", function (event) {
      applyStickyHeader(!!this.checked);
      if (localStorage) localStorage.setItem(NAV_STICKY_HEADER_STORAGE_KEY, this.checked ? "1" : "0");
    });

    var multiTabPref = getMultiTabPreference();
    jQuery("#nav_multi_tab").prop("checked", multiTabPref);
    jQuery("#nav_multi_tab").on("click", function () {
      setMultiTabPreference(!!this.checked);
    });

    changeNavSidebarTheme(getLocal(NAV_SIDEBAR_THEME_STORAGE_KEY, "--"));
    changeFontSize(getLocal(ROOT_FONT_SIZE_STORAGE_KEY, "--"));
    applyTheme(getLocal(THEME_STORAGE_KEY, theme))

    var pageSize = beangle.cookie.get("pageSize");
    if (pageSize) jQuery("#page_size_selector").val(pageSize);
    jQuery("#page_size_selector").on("change", function (event) {
      beangle.cookie.set("pageSize", this.value, "/", 10 * 365);
    });
    jQuery("#main_siderbar .brand-link").css("height", jQuery("#main_header").outerHeight() + "px");//对齐brand
    jQuery(document).ready(restoreNav);
  }

  function enableSearch(searchInputId) {
    nav.searchInputId = searchInputId;
    var searchDom = jQuery('#' + searchInputId).parent().parent();
    searchDom.show();
    searchDom.removeClass("sidebar-search-open");
    searchDom.find(".input-group-append .btn").click(function (event) {
      event.preventDefault();
      nav.toggleSearchResults();
    });
    jQuery(document).on('keyup', '#' + searchInputId, function (event) {
      setTimeout(function () {
        var searchValue = jQuery('#' + nav.searchInputId).val().toLowerCase();
        if (!searchValue || searchValue.length < 2) {
          nav.closeSearchResults();
        } else {
          nav.search(searchValue, 7);
        }
      }, 100);
    });
  }

  function openMenu(obj) {
    return nav.openMenu(obj);
  }

  function changeNavSidebarTheme(theme) {
    if (theme == '--') return;
    if (localStorage) localStorage.setItem(NAV_SIDEBAR_THEME_STORAGE_KEY, theme);
    jQuery("#nav_siderbar_theme_" + theme).prop("checked", true);
    if (theme == "dark") {
      jQuery('#main_siderbar').removeClass("sidebar-light-lightblue").addClass("sidebar-dark-primary");
      jQuery('#control_sidebar').removeClass("control-sidebar-light").addClass("control-sidebar-dark");
    } else {
      jQuery('#main_siderbar').removeClass("sidebar-dark-primary").addClass("sidebar-light-lightblue");
      jQuery('#control_sidebar').removeClass("control-sidebar-dark").addClass("control-sidebar-light");
    }
  }

  function changeFontSize(font_size) {
    if (font_size == "--") return;
    jQuery("#control_sidebar input[name=root_font_size]").each(function (i, a) { if (jQuery(a).val() == font_size) jQuery(a).prop("checked", true) })
    if (localStorage) localStorage.setItem(ROOT_FONT_SIZE_STORAGE_KEY, font_size);
    document.documentElement.style.setProperty("font-size", font_size);
    jQuery("#main_siderbar .brand-link").css("height", jQuery("#main_header").outerHeight() + "px");//对齐brand
  }

  function changeTheme(theme) {
    if (theme) {
      if (typeof theme == "string") {
        theme = JSON.parse(theme)
      }
      applyTheme(theme)
      setLocal(THEME_STORAGE_KEY, JSON.stringify(theme))
    } else {
      applyTheme(nav.theme)
      setLocal(THEME_STORAGE_KEY, null)
    }
  }

  function applyTheme(theme) {
    if (typeof theme == "string") {
      theme = JSON.parse(theme)
    }
    var r = document.querySelector(':root');
    r.style.setProperty("--primary-color", theme.primaryColor)
    r.style.setProperty("--navbar-bg-color", theme.navbarBgColor)
    r.style.setProperty("--search-bg-color", theme.searchBgColor)
    r.style.setProperty("--gridbar-bg-color", theme.gridbarBgColor)
    r.style.setProperty("--grid-border-color", theme.gridBorderColor)
    jQuery("#theme_primaryColor").val(theme.primaryColor)
    jQuery("#theme_navbarBgColor").val(theme.navbarBgColor)
    jQuery("#theme_searchBgColor").val(theme.searchBgColor)
    jQuery("#theme_gridbarBgColor").val(theme.gridbarBgColor)
    jQuery("#theme_gridBorderColor").val(theme.gridBorderColor)
  }
  /**
   * 导航恢复：阶段一 phase1ResolvePortalBootstrap（快照、参数、hash）；阶段二渲染侧边菜单与工作台多标签。
   * 不限定 URL 路径（门户 /portal、终端开发壳等均可）。
   */
  function restoreNav() {
    if (!nav || typeof nav.phase1ResolvePortalBootstrap !== "function") {
      return;
    }
    var boot = nav.phase1ResolvePortalBootstrap();

    function phase2Render() {
      var openAppId = undefined;
      if (boot.initMenuLoc) {
        var ae = boot.initMenuLoc.app && boot.initMenuLoc.app.app ? boot.initMenuLoc.app.app : boot.initMenuLoc.app;
        openAppId = ae && ae.id != null ? ae.id : undefined;
      }
      var menuObj = boot.initMenuLoc ? boot.initMenuLoc.menu : null;
      if (boot.navTabsSnapshot.tabs.length > 0 && menuObj && menuObj.openMode === NAV_OPENMODE_AJAX && !boot.menuHighlightOnly) {
        menuObj = null;
      }
      var hlOnly = !!(boot.menuHighlightOnly && menuObj && (menuObj.openMode === NAV_OPENMODE_WUJIE || menuObj.openMode === NAV_OPENMODE_IFRAME || menuObj.openMode === NAV_OPENMODE_AJAX));

      function syncHashHistory() {
        if (!boot.menuHref || !boot.navTabsSnapshot.activeTabId) return;
        var tid = boot.navTabsSnapshot.activeTabId;
        if (!nav.workspace.tabs[tid]) return;
        var hostU = nav.menuEntryToHostUrl(boot.menuHref);
        if (hostU) {
          try {
            window.history.replaceState({ emsNavTab: tid }, "", hostU);
          } catch (eH) { /* ignore */ }
        }
      }

      function finishSidebar() {
        nav.displayGroupMenus(boot.initialGroupId, openAppId, menuObj, { highlightOnly: hlOnly });
        syncHashHistory();
      }

      var snap0 = boot.navTabsSnapshot;
      if (snap0.tabs.length > 0 || (nav.multiTab && snap0.activeTabId === nav.workspace.homeTabId)) {
        var rp = nav.restoreNavTabsFromSession(snap0, {
          preferredTabId: snap0.activeTabId,
          skipSidebarInFinalize: true
        });
        Promise.resolve(rp).then(finishSidebar).catch(function (eRn) {
          if (window.console && console.warn) console.warn("[ems] restoreNav phase2", eRn);
          finishSidebar();
        });
      } else {
        if (nav.groups && nav.groups.length > 0) {
          nav.displayGroupMenus(boot.initialGroupId, openAppId, menuObj, { highlightOnly: false });
        }
        if (nav.welcomeUrl && !(boot.menuHref && boot.initMenuLoc)) {
          if (nav.multiTab) {
            var mainEl0 = document.getElementById("main");
            if (mainEl0 && mainEl0.parentNode) {
              nav.ensureNavWorkspace(mainEl0.parentNode, mainEl0, { skipPersist: true });
              nav.showNavWorkspace(mainEl0);
              if (nav.workspace.tabs[nav.workspace.homeTabId]) {
                nav.activateNavTab(nav.workspace.homeTabId, { replaceState: true });
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

  /** 退出或回门户首页时清理导航侧状态：工作台 teardown + 清空本标签页 sessionStorage。 */
  function clearNavState() {
    nav.teardownNavWorkspace(document.getElementById("main"));
    sessionStorage.clear();
  }

  function publish(exports) {
    exports.config = config;
    exports.hostName = hostName;
    exports.sameDomain = sameDomain;
    exports.init = init;
    exports.createNav = createNav;
    exports.changeGroup = changeGroup;
    exports.createProfileNav = createProfileNav;
    exports.fetchMessages = fetchMessages;
    exports.setup = setup;
    exports.enableSearch = enableSearch;
    exports.openMenu = openMenu;
    exports.changeNavSidebarTheme = changeNavSidebarTheme;
    exports.changeFontSize = changeFontSize;
    exports.clearNavState = clearNavState;
    exports.setWelcomeUrl = function (url) { nav.setWelcomeUrl(url); };
    exports.ensureWujieRuntime = resolveWujieRuntime;
    exports.getNav = function () { return nav; };
    exports.changeTheme = changeTheme;
    exports.getMultiTabPreference = getMultiTabPreference;
    exports.setMultiTabPreference = setMultiTabPreference;
    exports.resolveMultiTabParam = resolveMultiTabParam;
    exports.messageCallBack = function (c) {
      jQuery('#newly-message-count').text(c);
    };
    exports.taskCallBack = function (c) {
      jQuery('#newly-task-count').text(c);
    };
  }

  var api = {};
  publish(api);

  if (typeof define === 'function' && define.amd) {
    define('ems', function () {
      return api;
    });
  }

  if (typeof exports === 'object' && typeof module !== 'undefined' && module.exports) {
    module.exports = api;
  }

  if (typeof window !== 'undefined') {
    window.ems = api;
  }
}(typeof globalThis !== 'undefined' ? globalThis : typeof window !== 'undefined' ? window : this));
