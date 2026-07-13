/**
 * 壳层布局断点与侧栏开合（<992px 滑出式侧栏）。
 */
import { EMS_LAYOUT_MOBILE_MAX_WIDTH } from './constants.js';

let overlayClickBound = false;

export function shellWrapper(): JQuery {
  return jQuery('.wrapper').first();
}

export function isMobileSidebarLayout(): boolean {
  return window.innerWidth <= EMS_LAYOUT_MOBILE_MAX_WIDTH;
}

/** <992px 时收起滑出侧栏 */
export function closeMobileSidebar(): void {
  if (isMobileSidebarLayout()) {
    shellWrapper().removeClass('sidebar-open');
  }
}

/** <992px 时展开滑出侧栏（选分组后展示左侧菜单） */
export function openMobileSidebar(): void {
  if (isMobileSidebarLayout()) {
    ensureMobileSidebarOverlay();
    shellWrapper().addClass('sidebar-open');
  }
}

/**
 * 内容区 iframe 内点击无法冒泡到 document，侧栏打开时用遮罩拦截内容区点击以收起侧栏。
 */
export function ensureMobileSidebarOverlay(): JQuery {
  let $overlay = jQuery('#ems_sidebar_overlay');
  if ($overlay.length === 0) {
    $overlay = jQuery(
      '<div id="ems_sidebar_overlay" class="ems-sidebar-overlay" aria-hidden="true"></div>'
    );
    shellWrapper().append($overlay);
  }
  if (!overlayClickBound) {
    overlayClickBound = true;
    $overlay.on('click.emsShell', function (e) {
      e.preventDefault();
      closeMobileSidebar();
    });
  }
  return $overlay;
}

/**
 * 窄屏侧栏滑出屏外时，在汉堡菜单按钮左侧显示侧栏 logo 副本（仅展示，不触发 clearNavState/跳转）。
 */
export function ensureMobileHeaderBrand(): void {
  if (jQuery('.ems-header-brand-item').length > 0) return;
  const $sidebarBrand = jQuery('.main-sidebar .brand-link').first();
  if ($sidebarBrand.length === 0) return;
  const $srcImg = $sidebarBrand.find('.brand-image').first();
  const src = $srcImg.attr('src');
  if (!src) return;

  const title = $sidebarBrand.attr('title') || '';
  const $link = jQuery(
    '<span class="nav-link ems-header-brand-link" role="img" aria-hidden="false"></span>'
  );
  if (title) $link.attr('title', title);
  $link.append(jQuery('<img class="ems-header-brand-image" alt="" />').attr('src', src));
  const $li = jQuery('<li class="nav-item ems-header-brand-item"></li>').append($link);
  jQuery('#main_header .navbar-nav').first().prepend($li);
}
