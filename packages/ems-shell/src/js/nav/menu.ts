// @ts-nocheck
/**
 * 侧栏与顶栏导航 proto（menuProto → Nav.prototype）。
 *
 * 职责：解析 domain 菜单树、渲染分组/应用侧栏、菜单搜索、
 * 侧栏树点击委托、顶部分组与应用切换。
 */
import {
  NAV_OPENMODE_AJAX,
  NAV_OPENMODE_IFRAME,
  NAV_OPENMODE_WUJIE,
} from '../constants.js';
import { sameDomain } from '../url.js';
import { prependApps, switchNavActive } from './factory.js';

export const menuProto = {

    // --- 基础 ---

    setWelcomeUrl: function(url) {
      this.welcomeUrl = url;
    },

    processUrl: function(url) {
      if (url.indexOf("{") == -1) return url;
      for (var name in this.params) {
        url = url.replace("{" + name + "}", this.params[name]);
      }
      return url;
    },
    /**
     * 收集 domain 中的 apps，以及每个 app 对应的菜单
     */
    collectApps: function() {
      var shellAppBase = this.app.base;
      if (!this.portal.base && this.portal.url) {
        this.portal.base = this.portal.url;
      }
      for (var p = 0; p < this.groupMenus.length; p++) {
        var childrenApps = this.groupMenus[p].appMenus;
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
          } else {
            this.apps.push(app);
          }
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
          if (app.base && app.base.endsWith("/")) {
            app.base = app.base.substring(0, app.base.length - 1);
          }
          app.base = this.processUrl(app.base || shellAppBase);
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

    // --- 搜索与菜单定位 ---

    searchMenu: function(name, limit) {
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

    searchMenuByName: function(menus, name, limit, results, path) {
      for (var j = 0; j < menus.length; j++) {
        if (results.length >= limit) return;
        var menu = menus[j];
        if (menu.entry && (menu.title.includes(name) || menu.entry.toLowerCase().includes(name))) {
          var resultPath = path.slice();
          resultPath.push(menu.title);
          var result = { "name": menu.title, "link": menu.entry, "path": resultPath, "target": menu.target, "openMode": menu.openMode || NAV_OPENMODE_IFRAME, "navGroupAttr": this.formatNavGroupAttr(menu.navAppId, menu.navGroupId) };
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

    locateMenu: function(url) {
      for (var p = 0; p < this.groupMenus.length; p++) {
        var appMenus = this.groupMenus[p].appMenus;
        for (var i = 0; i < appMenus.length; i++) {
          var m = this.locateMenuByHref(appMenus[i].menus, url);
          if (m) return { "group": this.groupMenus[p].group, "app": appMenus[i], "menu": m };
        }
      }
      return null;
    },

    locateMenuByHref: function(menus, url) {
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

    // --- 菜单树渲染 ---

    processMenus: function() {
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
    normalizeMenuOpenMode: function(menu, fallbackOpenMode) {
      var raw = menu.openMode != null && String(menu.openMode) !== "" ? String(menu.openMode).toLowerCase() : "";
      if (raw === NAV_OPENMODE_WUJIE) {
        menu.openMode = NAV_OPENMODE_WUJIE;
      } else if (raw === NAV_OPENMODE_IFRAME || raw === NAV_OPENMODE_AJAX) {
        menu.openMode = NAV_OPENMODE_IFRAME;
      } else {
        menu.openMode = fallbackOpenMode === NAV_OPENMODE_WUJIE ? NAV_OPENMODE_WUJIE : NAV_OPENMODE_IFRAME;
      }
    },

    processMenuEntry: function(app, menus, navGroupId) {
      if (navGroupId === void 0 || navGroupId === null) {
        navGroupId = "";
      }
      for (var i = 0; i < menus.length; i++) {
        var menu = menus[i];
        if (menu.entry) {
          menu.navGroupId = navGroupId;
          menu.navAppId = app.id;
        }
        if (menu.entry && !menu.entry.startsWith("http")) {
          if (app.embeddable) menu.target = "main";
          else menu.target = "_blank";
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
    createMenus: function(jqueryElem, menus, openMenuId) {
      jqueryElem.empty();
      if (!openMenuId) openMenuId = -1;
      var menuItem = "";
      for (var i = 0; i < menus.length; i++) {
        var menu = menus[i];
        var fonticon = "fa fa-list";
        if (menu.menus) {
          var appItem = this.appFoldTemplate.replace("{app.id}", menu.app.id);
          appItem = appItem.replace("{icon_class}", fonticon);
          appItem = appItem.replace("{app.title}", menu.app.title);
          appItem = appItem.replace("{open_class}", openMenuId == menu.app.id ? "menu-open" : "");
          appItem = appItem.replace("{active_class}", openMenuId == menu.app.id ? "active" : "");
          jqueryElem.append(appItem);
          this.createMenus(jQuery("#menu_app" + menu.app.id), menu.menus);
        } else if (menu.children) {
          menuItem = this.foldTemplate.replace("{menu.id}", menu.id);
          if (menu.fonticon) fonticon = menu.fonticon;
          menuItem = menuItem.replace("{icon_class}", fonticon);
          menuItem = menuItem.replace("{menu.title}", menu.title);
          menuItem = menuItem.replace("{open_class}", openMenuId == menu.id ? "menu-open" : "");
          menuItem = menuItem.replace("{active_class}", openMenuId == menu.id ? "active" : "");
          jqueryElem.append(menuItem);
          this.createMenus(jQuery("#menu" + menu.id), menu.children);
        } else {
          menuItem = this.menuTempalte.replace("{menu.title}", menu.title);
          if (menu.fonticon) {
            fonticon = menu.fonticon;
          } else {
            fonticon = this.getIconClass(menu.title);
          }
          menuItem = menuItem.replace("{icon_class}", fonticon);
          menuItem = menuItem.replace("{menu.entry}", menu.entry);
          menuItem = menuItem.replace(/\{menu\.target\}/g, menu.target);
          menuItem = menuItem.replace("{menu.openMode}", menu.openMode ? String(menu.openMode) : NAV_OPENMODE_IFRAME);
          menuItem = menuItem.replace("{menu.navGroupAttr}", this.formatNavGroupAttr(menu.navAppId, menu.navGroupId));
          jqueryElem.append(menuItem);
        }
      }
    },

    // --- 侧栏 / 顶栏事件 ---

    activate: function() {
      var menuDomId = this.menuDomId;
      var navSelf = this;
      jQuery("#" + menuDomId).off("click.emsTree").on("click.emsTree", "li a", function(e) {
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
          li.siblings().each(function(i, sli) {
            jQuery(sli).removeClass("menu-open menu-is-opening");
            jQuery(sli).children("ul").removeAttr("style");
            jQuery(sli).children("a").removeClass("active");
          });
          jThis.toggleClass("active");
          return;
        }
        li.siblings().each(function(i, sli) {
          jQuery(sli).children("a").removeClass("active");
        });
        jThis.addClass("active");
        navSelf.openMenu(this);
      });
    },
    /** 顶栏分组：事件委托，只绑定一次 */
    bindTopGroupNav: function() {
      var navSelf = this;
      jQuery("#" + this.navDomId).off("click.emsGroup").on("click.emsGroup", "a[id^='group_']", function(e) {
        e.preventDefault();
        e.stopImmediatePropagation();
        navSelf.displayGroupMenus(this.id.substring("group_".length));
      });
    },
    /** 侧栏搜索结果菜单打开 */
    bindSearchMenuOpen: function() {
      var navSelf = this;
      jQuery("#main_siderbar").off("click.emsSearchOpen").on("click.emsSearchOpen", ".sidebar-search-results a.list-group-item[data-open-mode]", function(e) {
        e.preventDefault();
        e.stopImmediatePropagation();
        navSelf.openMenu(this);
      });
    },

    // --- 顶栏分组与应用 ---

    fillAppName: function() {
      if (this.sysName.length > 6) {
        jQuery("#appName").css("font-size", "0.875rem");
      }
      jQuery("#appName").html(this.sysName);
    },
    /**
     * 添加顶层groups
     */
    addTopGroups: function(jqueryElem) {
      var appItem = "";
      var topItemCount = 0;
      var topMoreHappened = false;
      this.fillAppName();
      prependApps(jqueryElem, this, this.apps, true);
      for (var i = 0; i < this.groups.length; i++) {
        var group = this.groups[i];
        topItemCount += 1;
        if (topItemCount == this.maxTopItem && this.groups.length > this.maxTopItem) {
          jqueryElem.append('<li class="nav-item dropdown"><a href="#" data-toggle="dropdown" class="dropdown-toggle nav-link">\u66F4\u591A...</a><div id="topMore" aria-labelledby="navbarDropdown" class="dropdown-menu"></div><li>');
          topMoreHappened = true;
        }
        if (topMoreHappened) {
          jqueryElem = jQuery("#topMore");
        }
        if (topMoreHappened) {
          appItem = this.dropdownGroupNavTemplate.replace("{group.id}", group.id);
        } else {
          appItem = this.groupTemplate.replace("{group.id}", group.id);
        }
        appItem = appItem.replace("{group.title}", group.title);
        appItem = appItem.replace("{group.name}", group.name);
        appItem = appItem.replace("{active_class}", i == 0 ? "active" : "");
        jqueryElem.append(appItem);
      }
    },

    displayAppMenus: function(appName) {
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
        groupId = this.groupMenus[0].group.id;
      }
      this.displayGroupMenus(groupId, appId);
    },
    /**显示指定group的menu；navOpts.highlightOnly 为 true 时对 menuObj 仅展开父级并高亮，不 trigger 打开（用于已从快照恢复的无界标签）。*/
    displayGroupMenus: function(groupId, appId, menuObj, navOpts) {
      navOpts = navOpts || {};
      var gid = String(groupId);
      var aid = appId != null && appId !== "" ? String(appId) : "";
      var sameGroupApp = this._displayedGroupId === gid && this._displayedAppId === aid;
      /** highlightOnly（标签切换）：同 group/app 仅改高亮；否则保持原逻辑（有 menuObj 时仍重建侧栏） */
      var sameGroup = navOpts.highlightOnly ? sameGroupApp : sameGroupApp && !menuObj;
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
              var onlyOneAppMenu = groupMenu.appMenus[0];
              if (onlyOneAppMenu.menus.length > 0) openMenuId = onlyOneAppMenu.menus[0].id;
              this.createMenus(jQuery("#" + this.menuDomId), onlyOneAppMenu.menus, openMenuId);
            } else {
              this.createMenus(jQuery("#" + this.menuDomId), groupMenu.appMenus, openMenuId);
            }
            this._displayedGroupId = gid;
            this._displayedAppId = aid;
          }
          break;
        }
      }
      if (menuObj && menuObj.entry) {
        var menu = jQuery('#main_siderbar a[href="' + menuObj.entry + '"]');
        menu.parents("li").each(function(i2, li) {
          jQuery(li).addClass("menu-open");
          jQuery(li).siblings().each(
            function(i3, sli) {
              jQuery(sli).removeClass("menu-open menu-is-opening");
              jQuery(sli).children("ul").removeAttr("style");
            }
          );
        });
        if (navOpts.highlightOnly) {
          jQuery("#" + this.menuDomId + " a.nav-link.active").removeClass("active");
          menu.parent("li").siblings().each(function(i2, li) {
            jQuery(li).children("a").removeClass("active");
          });
          menu.addClass("active");
        } else {
          this.openMenu(menu[0]);
        }
      }
    },

    // --- 侧栏搜索 ---

    /** 侧栏搜索：展示结果并高亮关键字 */
    search: function(name, limit) {
      var results = this.searchMenu(name, limit);
      var searchRegExp = new RegExp(name, "gi");
      var resultDom = jQuery(".sidebar-search-results .list-group");
      resultDom.empty();
      if (results.length == 0) {
        var notfound = { "name": "\u627E\u4E0D\u5230\u7ED3\u679C", "link": "#", "path": [], "target": "main", "openMode": NAV_OPENMODE_AJAX, "navGroupAttr": "" };
        resultDom.append(this.renderSearchItem(searchRegExp, notfound));
      } else {
        for (var i = 0; i < results.length; i++) {
          var result = results[i];
          resultDom.append(this.renderSearchItem(searchRegExp, result));
        }
      }
      this.openSearchResults();
    },

    openSearchResults: function() {
      var searchDom = jQuery("#" + this.searchInputId).parent().parent();
      searchDom.addClass("sidebar-search-open");
      searchDom.find(".input-group-append .btn i").removeClass("fa-search").addClass("fa-times");
    },

    closeSearchResults: function() {
      var searchDom = jQuery("#" + this.searchInputId).parent().parent();
      searchDom.removeClass("sidebar-search-open");
      searchDom.find(".input-group-append .btn i").removeClass("fa-times").addClass("fa-search");
    },

    toggleSearchResults: function() {
      var searchDom = jQuery("#" + this.searchInputId).parent().parent();
      if (searchDom.hasClass("sidebar-search-open")) {
        this.closeSearchResults();
      } else {
        this.openSearchResults();
      }
    },

    renderSearchItem: function(searchRegExp, result) {
      var pathStr = result.path.join(" -> ");
      var name = result.name.replace(searchRegExp, function(str) {
        return "<strong>" + str + "</strong>";
      });
      var item = null;
      if (result.link != "#") {
        item = jQuery("<a/>", { href: decodeURIComponent(result.link), class: "list-group-item", target: result.target });
        var som = result.openMode || NAV_OPENMODE_AJAX;
        item.attr("data-open-mode", som);
        item.attr("data-nav-group", result.navGroupAttr || "");
      } else {
        item = jQuery("<a/>", { href: decodeURIComponent(result.link), class: "list-group-item", target: result.target });
      }
      var searchTitleElement = jQuery("<div/>", { class: "search-title" }).html(name);
      var searchPathElement = jQuery("<div/>", { class: "search-path" }).html(pathStr);
      item.append(searchTitleElement).append(searchPathElement);
      return item;
    }
};
