/**
 * 导航工厂与顶栏 DOM 辅助函数。
 * createNav 创建全局 nav 实例并完成首屏绑定；prependGroupToggle / switchNavActive 供 menu 使用。
 */
import { Nav } from './Nav.js';
import { isMobileSidebarLayout, openMobileSidebar, ensureMobileHeaderBrand } from '../layout.js';
import type { DomainMenus, GroupMenus, NavApp, NavInstance, NavParams } from '../types.js';

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

/** 同步窄屏分组下拉的 active 高亮 */
export function syncGroupToggleActive(groupId: string | number): void {
  const gid = String(groupId);
  jQuery('#group_drop_bar .dropdown-item').removeClass('active');
  jQuery('#group_drop_bar a[data-group-id="' + gid + '"]').addClass('active');
}

function closeGroupToggleDropdown(): void {
  const $drop = jQuery('.ems-group-toggle-nav .nav-item.dropdown');
  const $toggle = $drop.find('.dropdown-toggle');
  const $menu = $drop.find('.dropdown-menu');
  if ($toggle.length && typeof ($toggle as JQuery & { dropdown?: (action: string) => void }).dropdown === 'function') {
    ($toggle as JQuery & { dropdown: (action: string) => void }).dropdown('hide');
  }
  $drop.removeClass('show');
  $menu.removeClass('show');
  $toggle.attr('aria-expanded', 'false');
}

/** 窄屏顶栏：分组下拉（与宽屏 #top_nav_bar 等效，选中后切换侧栏菜单并滑出侧栏） */
export function prependGroupToggle(jqueryElem: JQuery, navRef: NavInstance): void {
  const groupDropNav =
    '<ul class="nav navbar-nav ems-group-toggle-nav"><li class="nav-item dropdown">' +
    '<a href="#" data-toggle="dropdown" data-display="static" class="nav-link dropdown-toggle group-toggle" role="button" title="分组" aria-haspopup="true" aria-expanded="false"><i class="fas fa-layer-group"></i></a>' +
    '<div id="group_drop_bar" class="dropdown-menu ems-group-toggle-menu"></div>' +
    '</li></ul>';
  jqueryElem.before(groupDropNav);
  const bar = jQuery('#group_drop_bar');
  const groups = navRef.groups ?? [];
  let html = '';
  for (let i = 0; i < groups.length; i++) {
    const group = groups[i];
    html +=
      '<a href="javascript:void(0)" class="dropdown-item' +
      (i === 0 ? ' active' : '') +
      '" data-group-id="' +
      group.id +
      '">' +
      (group.title ?? '') +
      '</a>';
  }
  bar.append(html);
}

/** 窄屏分组下拉点击：displayGroupMenus + 滑出侧栏 */
export function bindGroupToggleNav(navRef: NavInstance): void {
  jQuery(document)
    .off('click.emsGroupToggle')
    .on('click.emsGroupToggle', '#group_drop_bar a[data-group-id]', function (e) {
      e.preventDefault();
      e.stopImmediatePropagation();
      const groupId = this.getAttribute('data-group-id');
      if (!groupId) return;
      navRef.displayGroupMenus(groupId);
      syncGroupToggleActive(groupId);
      if (isMobileSidebarLayout()) {
        openMobileSidebar();
      }
      closeGroupToggleDropdown();
    });
}

/** 当前门户 Nav 单例，由 createNav 赋值 */
export let nav = null! as NavInstance;

/** 构造 Nav、注册历史监听并渲染首屏分组/侧栏 */
export function createNav(
  app: NavApp,
  portal: NavApp,
  domainMenus: DomainMenus | GroupMenus[] | null | undefined,
  params: NavParams | undefined,
  displayFirstGroup: boolean
): NavInstance {
  nav = new (Nav as unknown as new (...args: unknown[]) => NavInstance)(app, portal, domainMenus, params);
  nav.ensureNavTabHistoryListener();
  nav.addTopGroups(jQuery('#' + nav.navDomId));
  ensureMobileHeaderBrand();
  nav.bindTopGroupNav();
  bindGroupToggleNav(nav);
  nav.activate();
  nav.bindSearchMenuOpen();
  let resolvedInitialGroup = false;
  const wantGid = nav.initialGroupId;
  const groupMenus = nav.groupMenus ?? [];
  if (wantGid != null && wantGid !== '') {
    for (let gi = 0; gi < groupMenus.length; gi++) {
      if (String(groupMenus[gi].group.id) === String(wantGid)) {
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
