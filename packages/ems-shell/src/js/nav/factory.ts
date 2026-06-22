/**
 * 导航工厂与顶栏 DOM 辅助函数。
 * createNav 创建全局 nav 实例并完成首屏绑定；prependApps / switchNavActive 供 menu 使用。
 */
import { Nav } from './Nav.js';
import type { DomainMenus, NavApp, NavInstance, NavParams } from '../types.js';

/** 切换顶部导航栏上的按钮 */
export function switchNavActive(anchorId: string): void {
  const $el = jQuery(anchorId);
  const parent = $el.parent()[0];
  if (parent?.tagName === 'LI') {
    $el.parent().siblings().each(function (_i, li) {
      jQuery(li).children('a').removeClass('active');
    });
    $el.addClass('active');
  } else {
    $el.siblings().each(function (_i, a) {
      jQuery(a).removeClass('active');
    });
    $el.addClass('active');
  }
}

/** 在顶栏插入应用切换下拉（九宫格），按列分组展示各微应用链接 */
export function prependApps(
  jqueryElem: JQuery,
  navRef: NavInstance,
  apps: NavApp[] | undefined,
  autohide: boolean
): void {
  const appDropNav =
    '<ul class="nav navbar-nav"><li class="nav-item dropdown">' +
    '<a href="#" data-toggle="dropdown" class="nav-link dropdown-toggle {autohide}" role="button" title="应用" aria-haspopup="true" aria-expanded="true"><i class="fas fa-th"></i></a>' +
    '<div id="app_drop_bar" class="dropdown-menu columns-3"></div>' +
    '</li></ul>';
  const appTemplate =
    '<a href="{app.base}" class="dropdown-item {active_class}" target="_top">{app.title}</a>';
  jqueryElem.before(appDropNav.replace('{autohide}', autohide ? 'app-toggle' : ''));
  const bar = jQuery('#app_drop_bar');
  let curGroupId = 0;
  const appList = apps ?? navRef.apps;
  const columRows = Math.ceil(appList.length / 3);
  let content = '<div class="row">';
  const columnApps: NavApp[][] = [[], [], []];
  for (let i = 0; i < appList.length; i++) {
    columnApps[Math.floor(i / columRows)].push(appList[i]);
  }
  for (let column = 0; column < columnApps.length; column++) {
    const columnApp = columnApps[column];
    let columnDiv = '<div class="col-sm-4">';
    for (let i = 0; i < columnApp.length; i++) {
      const app = columnApp[i];
      if (app.group) {
        if (curGroupId === 0) {
          curGroupId = app.group.id as number;
        } else if (app.group.id != curGroupId) {
          if (i > 0) columnDiv += '<div class="dropdown-divider"></div>';
          curGroupId = app.group.id as number;
        }
      }
      if (app.name == navRef.app.name) {
        columnDiv += '<a  class="dropdown-item active" href="#">' + app.title + '</a>';
      } else {
        let appendHtml = appTemplate.replace('{app.base}', navRef.processUrl(app.base ?? ''));
        appendHtml = appendHtml.replace('{app.title}', app.title ?? '');
        appendHtml = appendHtml.replace('{active_class}', '');
        columnDiv += appendHtml;
      }
    }
    columnDiv += '</div>';
    content += columnDiv;
  }
  content += '</div>';
  bar.append(content);
}

/** 当前门户 Nav 单例，由 createNav 赋值 */
export let nav = null! as NavInstance;

/** 构造 Nav、注册历史监听并渲染首屏分组/侧栏 */
export function createNav(
  app: NavApp,
  portal: NavApp,
  domainMenus: DomainMenus,
  params: NavParams | undefined,
  displayFirstGroup: boolean
): NavInstance {
  nav = new (Nav as unknown as new (...args: unknown[]) => NavInstance)(app, portal, domainMenus, params);
  nav.ensureNavTabHistoryListener();
  nav.addTopGroups(jQuery('#' + nav.navDomId));
  nav.bindTopGroupNav();
  nav.activate();
  nav.bindSearchMenuOpen();
  let resolvedInitialGroup = false;
  const wantGid = nav.initialGroupId;
  if (wantGid != null && wantGid !== '') {
    for (let gi = 0; gi < nav.groupMenus.length; gi++) {
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

/** 顶栏分组点击：切换 displayGroupMenus */
export function changeGroup(ele: HTMLElement): void {
  nav.displayGroupMenus(ele.id.substring('group_'.length));
}
