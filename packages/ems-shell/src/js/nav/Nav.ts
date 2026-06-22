/**
 * Nav 构造函数：初始化运行时状态（含 nav.workspace），
 * 并将 menu / workspace / tabs 三组 proto 挂到 Nav.prototype。
 */
import { NAV_WORKSPACE_HOME_TAB_ID } from '../constants.js';
import type { DomainMenus, NavApp, NavInstance, NavParams } from '../types.js';
import { menuProto } from './menu.js';
import { workspaceProto } from './workspace.js';
import { tabsProto } from './tabs.js';

export function Nav(
  this: NavInstance,
  app: NavApp,
  portal: NavApp,
  domainMenus: DomainMenus,
  params?: NavParams
): void {
  this.portal = portal;
  this.app = app;
  this.apps = [];
  this.groups = [];
  this.menuDomId = 'menu_ul';
  this.navDomId = 'top_nav_bar';
  this.sysName = null;
  this.params = {};
  this.maxTopItem = 9;
  this.welcomeUrl = null;
  this.workspace = {
    rootId: 'ems_nav_workspace',
    scrollId: 'ems_nav_tab_scroll',
    listId: 'ems_nav_tab_nav',
    bodyId: 'ems_nav_tab_body',
    homeTabId: NAV_WORKSPACE_HOME_TAB_ID,
    homeMainId: 'ems_nav_home_main',
    tabSeed: 0,
    tabs: {},
    tabByUrl: {},
    activeTabId: null,
    openSeq: 0,
    maxTabCount: 30,
  };
  this.multiTab = true;
  this.initialGroupId = null;
  if (params) {
    for (const name in params) {
      const pv = params[name];
      if (name === 'menuDomId') {
        this.menuDomId = String(pv);
      } else if (name === 'navDomId') {
        this.navDomId = String(pv);
      } else if (name === 'sysName') {
        this.sysName = String(pv);
      } else if (name === 'maxTopItem') {
        this.maxTopItem = Number.parseInt(String(pv));
      } else if (name === 'initialGroupId') {
        this.initialGroupId = pv as string | number;
      } else if (name === 'multiTab') {
        this.multiTab = !(pv === false || pv === 'false' || pv === 0 || pv === '0');
      } else {
        this.params[name] = pv;
      }
    }
  }
  this.groupMenus = domainMenus.groups;
  for (let i = 0; i < this.groupMenus.length; i++) {
    this.groups.push(this.groupMenus[i].group);
  }

  this.getIconClass = function (name: string): string {
    if (name.indexOf('设置') > -1) return 'fas fa-cog';
    if (name.endsWith('开关')) return 'fa fa-toggle-on';
    if (name.endsWith('信息')) return 'fa fa-info-circle';
    if (name.indexOf('查询') > -1) return 'fa fa-search';
    if (name.indexOf('打印') > -1) return 'fa fa-print';
    if (name.indexOf('统计') > -1) return 'fa fa-chart-bar';
    if (name.indexOf('安排') > -1) return 'fa fa-calendar';
    if (name.indexOf('排名') > -1) return 'fa fa-sort-amount-down';
    if (name.endsWith('表')) return 'fa fa-table';
    return 'far fa-circle';
  };
  this.menuTempalte =
    '<li class="nav-item"><a class="nav-link" href="{menu.entry}" target="{menu.target}" data-open-mode="{menu.openMode}" data-nav-group="{menu.navGroupAttr}"><i class="nav-icon {icon_class}"></i><p>{menu.title}</p></a></li>';
  this.foldTemplate =
    '<li class="nav-item has-treeview {open_class}"><a class="nav-link {active_class}" href="javascript:void(0)"><i class="nav-icon {icon_class}"></i><p>{menu.title}<i class="nav-icon fa fa-angle-left right"></i></p></a><ul class="nav nav-treeview" id="menu{menu.id}"></ul></li>';
  this.appFoldTemplate =
    '<li class="nav-item has-treeview {open_class}"><a class="nav-link {active_class}" href="javascript:void(0)"><i class="nav-icon {icon_class}"></i><p>{app.title}<i class="nav-icon fa fa-angle-left right"></i></p></a><ul class="nav nav-treeview" id="menu_app{app.id}"></ul></li>';
  this.groupTemplate =
    '<li class="nav-item"><a class="nav-link {active_class}" href="javascript:void(0)" id="group_{group.id}">{group.title}</a></li>';
  this.dropdownGroupNavTemplate =
    '<a href="javascript:void(0)"  class="dropdown-item {active_class}" id="group_{group.id}">{group.title}</a>';
  (this as NavInstance & { collectApps(): void; processMenus(): void }).collectApps();
  (this as NavInstance & { collectApps(): void; processMenus(): void }).processMenus();
  if (!this.sysName) {
    this.sysName = this.portal.title ?? null;
  }
  jQuery('#' + this.menuDomId).addClass('sidebar-menu');
}

Object.assign(Nav.prototype, menuProto, workspaceProto, tabsProto);
