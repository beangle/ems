/**
 * Extract Nav.prototype from static bundle → flat nav/menu.ts, workspace.ts, tabs.ts
 * Run from repo root: node packages/ems-shell/scripts/extract-nav-from-static.mjs
 */
import fs from 'node:fs';
import path from 'node:path';

const repo = path.resolve(import.meta.dirname, '../../..');
const pkg = path.join(repo, 'packages/ems-shell');
const staticJs = path.join(pkg, 'dist/js/ems-shell.js');
const src = fs.readFileSync(staticJs, 'utf8');

const start = src.indexOf('Nav.prototype = {');
if (start < 0) throw new Error('Nav.prototype not found');
const bodyStart = start + 'Nav.prototype = {'.length;
const bodyEnd = src.indexOf('\n    };', bodyStart);
const body = src.slice(bodyStart, bodyEnd);

const methodRe = /\n      ([a-zA-Z_][a-zA-Z0-9_]*): function/g;
const hits = [];
let m;
while ((m = methodRe.exec(body)) !== null) {
  hits.push({ name: m[1], fnStart: m.index });
}

function sliceStart(fnStart) {
  let start = fnStart;
  let pos = fnStart;
  while (pos > 0) {
    const prevNl = body.lastIndexOf('\n', pos - 1);
    if (prevNl < 0) break;
    const line = body.slice(prevNl + 1, pos).trim();
    if (line === '' || line.startsWith('*') || line.startsWith('/**') || line.startsWith('//')) {
      start = prevNl + 1;
      pos = prevNl;
      continue;
    }
    break;
  }
  return start;
}

function sliceEnd(nextFnStart) {
  let end = nextFnStart;
  let pos = nextFnStart;
  while (pos > 0) {
    const prevNl = body.lastIndexOf('\n', pos - 1);
    if (prevNl < 0) break;
    const line = body.slice(prevNl + 1, pos).trim();
    if (line === '' || line.startsWith('*') || line.startsWith('/**') || line.startsWith('//')) {
      end = prevNl + 1;
      pos = prevNl;
      continue;
    }
    break;
  }
  return end;
}

for (let i = 0; i < hits.length; i++) {
  const start = sliceStart(hits[i].fnStart);
  const end = i + 1 < hits.length ? sliceEnd(hits[i + 1].fnStart) : body.length;
  let chunk = body.slice(start, end).trimEnd();
  if (chunk.endsWith(',')) chunk = chunk.slice(0, -1);
  hits[i].text = chunk.replace(/^      /gm, '    ');
}

const byName = Object.fromEntries(hits.map((h) => [h.name, h.text]));

const menuNames = [
  'setWelcomeUrl',
  'processUrl',
  'collectApps',
  'searchMenu',
  'searchMenuByName',
  'locateMenu',
  'locateMenuByHref',
  'processMenus',
  'normalizeMenuOpenMode',
  'processMenuEntry',
  'createMenus',
  'activate',
  'bindTopGroupNav',
  'bindSearchMenuOpen',
  'fillAppName',
  'addTopGroups',
  'displayAppMenus',
  'displayGroupMenus',
  'search',
  'openSearchResults',
  'closeSearchResults',
  'toggleSearchResults',
  'renderSearchItem',
];

const workspaceNames = [
  'ensureNavWorkspace',
  'upgradeNavWorkspaceToolbar',
  'getNavWorkspaceTargetEl',
  'appendWorkspaceTabShell',
  'mountIframeInWorkspacePanel',
  'buildWorkspaceAjaxMainId',
  'mountAjaxInWorkspacePanel',
  'ensureWorkspaceHomeTab',
  'ensureWorkspaceHomeTabIcon',
  'showNavWorkspace',
  'hideNavWorkspace',
  'tabEntryMatchesContentUrl',
  'findSnapshotTabIdForUrl',
];

const urlKeyDelegateNames = new Set([
  'normalizeNavOpenMode',
  'navTabDedupeKey',
  'formatNavGroupAttr',
  'parseNavGroupAttr',
  'buildMicroAppName',
  'menuEntryToHostUrl',
  'navUrlToPath',
  'resolveNavAbsoluteUrl',
  'normalizeContentUrlKey',
  'urlsMatchForRouting',
]);

const tabsNames = hits.map((h) => h.name).filter(
  (n) => !menuNames.includes(n) && !workspaceNames.includes(n) && !urlKeyDelegateNames.has(n)
);

function pick(names) {
  return names.map((n) => {
    if (!byName[n]) throw new Error('Missing method: ' + n);
    return byName[n];
  });
}

const urlKeysMethods = `    normalizeNavOpenMode: function (k) {
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
    }`;

const menuTs = `// @ts-nocheck
import {
  NAV_OPENMODE_AJAX,
  NAV_OPENMODE_IFRAME,
  NAV_OPENMODE_WUJIE,
} from '../constants.js';
import { sameDomain } from '../url.js';
import { prependApps, switchNavActive } from './factory.js';

export const menuProto = {
${pick(menuNames).join(',\n')}
};
`;

const workspaceTs = `// @ts-nocheck
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
${pick(workspaceNames).join(',\n')},
${urlKeysMethods}
};
`;

const tabsTs = `// @ts-nocheck
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
${pick(tabsNames).join(',\n')}
};
`;

const navDir = path.join(pkg, 'src/js/nav');
fs.writeFileSync(path.join(navDir, 'menu.ts'), menuTs);
fs.writeFileSync(path.join(navDir, 'workspace.ts'), workspaceTs);
fs.writeFileSync(path.join(navDir, 'tabs.ts'), tabsTs);

console.log('menu:', menuNames.length, 'workspace:', workspaceNames.length + 9, 'tabs:', tabsNames.length);
console.log('lines', menuTs.split('\n').length, workspaceTs.split('\n').length, tabsTs.split('\n').length);
