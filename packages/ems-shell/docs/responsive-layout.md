# 自适应布局 — 设计说明与用法

本文记录 `@beangle/ems-shell` 门户壳层在**窄屏 / 宽屏**下的布局约定、DOM 结构要求与运行时行为，便于维护 `nav.ftl`、`ems-shell.css` 与 `layout.ts` 时保持一致。

门户页面通过 `app/nav.ftl`（或 portal `index.ftl` 内嵌的 `displayFrame` 宏）加载壳层；**无需在业务 FTL 中重复实现断点逻辑**，由 `emsShell.setup()` → `initShellLayout()` 与 `createNav()` 自动完成。

---

## 核心断点：992px

与 Bootstrap `lg` 对齐，全项目统一使用 **`991.98px`** 作为分界（常量 `EMS_LAYOUT_MOBILE_MAX_WIDTH`，见 `src/js/constants.ts`）。

| 视口宽度 | 模式 | 侧栏 | 顶栏分组 | 汉堡按钮 |
|----------|------|------|----------|----------|
| **≥ 992px** | 桌面 | 固定占位（`margin-left` 偏移顶栏与内容区） | `#top_nav_bar` 横向分组链接 | 切换 `sidebar-collapse`（折叠侧栏） |
| **< 992px** | 窄屏 | 默认滑出屏外；`sidebar-open` 时滑入覆盖 | `#group_drop_bar` 分组下拉（group toggle） | 切换 `sidebar-open`（滑出/收回侧栏） |

**为何统一为 992px：** 此前顶栏/内容区在 `≤991px` 已取消左偏移，而侧栏要到 `≤767px` 才滑出，768–991px 会出现侧栏占位与内容重叠。现侧栏、顶栏偏移、汉堡逻辑、group toggle 显隐均与 992px 对齐。

---

## 页面 DOM 约定

`nav.ftl` / `index.ftl` 需提供以下结构（类名与 id 由壳层依赖）：

```text
.wrapper                          ← shell() 根；窄屏侧栏类 sidebar-open 加在此处
  #main_header.main-header         ← 顶栏
    ul.navbar-nav                  ← 第一项：汉堡 [data-ems-pushmenu]（窄屏 logo 会 prepend 到本 ul 最前）
    #top_nav_bar                   ← 宽屏横向分组（JS 填充）
    #navbar-setting                ← 右侧消息/用户/设置
  #main_siderbar.main-sidebar
    a.brand-link                   ← 含 .brand-image、#appName（窄屏顶栏 logo 从此复制）
    #menu_ul                       ← 侧栏菜单
  #main_wrapper.content-wrapper
    #main                          ← 主内容 / 工作台
```

壳层在运行时**自动注入**（无需手写 FTL）：

| 元素 | 说明 |
|------|------|
| `#ems_sidebar_overlay` | 窄屏侧栏打开时的半透明遮罩 |
| `.ems-group-toggle-nav` / `#group_drop_bar` | 窄屏分组下拉 |
| `.ems-header-brand-item` | 窄屏顶栏 logo（汉堡左侧） |

初始化入口：

```javascript
beangle.require(['ems-shell'], function (emsShell) {
  jQuery(document).ready(function () {
    emsShell.createNav(app, portal, menusJson, params, false);
    emsShell.setup(theme, params);  // → initShellLayout()、restoreNav()
  });
});
```

---

## 窄屏顶栏布局

从左到右典型顺序：

```text
[logo] [汉堡] [分组▼] … [消息] [用户] [设置]
```

### 顶栏 Logo（`.ems-header-brand-item`）

- **何时显示：** 仅 `< 992px`（`≥992px` 由侧栏 `brand-link` 展示）。
- **来源：** `ensureMobileHeaderBrand()` 复制侧栏 `.brand-image` 的 `src`。
- **位置：** 插入第一个 `ul.navbar-nav` 的**最前**（汉堡按钮左侧）。
- **交互：** 纯展示（`pointer-events: none`），**不**复制侧栏 `clearNavState` / 跳转，避免误点导致工作台状态清空。
- **稳定性：** 窄屏下**常驻显示**，不随 `sidebar-open` 隐藏，避免开关侧栏时顶栏闪动。

侧栏内 `brand-link` 仍可点击回首页并执行 `emsShell.clearNavState()`。

### 汉堡菜单（`[data-ems-pushmenu]`）

- `< 992px`：在 `.wrapper` 上切换 `sidebar-open`。
- `≥ 992px`：切换 `sidebar-collapse`（AdminLTE 折叠侧栏宽度）。

### 分组切换（Group Toggle）

原 **App Toggle**（九宫格选应用）已改为 **Group Toggle**（只选分组）：

- 图标：`fa-layer-group`，类名 `group-toggle`。
- 下拉：`#group_drop_bar`，左缘与按钮对齐（`data-display="static"` + `left: 0`）。
- 选中分组：调用 `displayGroupMenus(groupId)`，与宽屏 `#top_nav_bar` 行为一致；**并**在窄屏调用 `openMobileSidebar()` 滑出左侧菜单。
- 选中后下拉自动关闭（Bootstrap `dropdown('hide')` + 移除 `show` 类）。
- 不再提供单独的应用跳转列表。

宽屏仍使用 `#top_nav_bar` 内 `group_{id}` 锚点；`bindTopGroupNav()` 与 `bindGroupToggleNav()` 共用 `displayGroupMenus`。

---

## 窄屏侧栏与遮罩

### 侧栏滑入/滑出

```css
/* < 992px */
.main-sidebar { margin-left: calc(-1 * var(--ems-sidebar-width)); }
.sidebar-open .main-sidebar { margin-left: 0; }
```

### 遮罩（`#ems_sidebar_overlay`）

侧栏打开时盖住**顶栏下方内容区**（`top: var(--ems-header-height)`），用于：

1. 视觉上区分侧栏与内容。
2. **拦截点击**：iframe / 无界子应用内点击无法冒泡到 `document`，必须靠遮罩收起侧栏。

遮罩背景：`rgba(0, 0, 0, 0.25)`。

### 侧栏自动收起

在 `< 992px` 且 `sidebar-open` 时，以下操作会 `closeMobileSidebar()`：

| 触发 | 说明 |
|------|------|
| 点击遮罩 | 主要内容区 |
| 点击顶栏（汉堡除外） | 顶栏非侧栏区域 |
| 打开侧栏菜单项 | `openMenu()` 内调用 |
| 窄屏分组下拉选分组 | 选后先开侧栏；再点遮罩可关 |
| 窗口拉宽到 ≥ 992px | `resize` 时清除 `sidebar-open` |

---

## 宽屏布局要点

- `.layout-fixed` + `.layout-navbar-fixed`：顶栏固定，侧栏 `brand-link` 在 `≥992px` 使用 `position: fixed` 与顶栏对齐。
- 顶栏、内容区 `margin-left: var(--ems-sidebar-width)`（折叠时为 `--ems-sidebar-collapsed`）。
- `sidebar-mini.sidebar-collapse`：鼠标悬停侧栏可临时展开（仅宽屏）。

`< 992px` 下**不**对 `brand-link` 使用 fixed，避免 logo 区域盖住顶栏控件。

---

## CSS 变量

| 变量 | 默认 | 用途 |
|------|------|------|
| `--ems-sidebar-width` | `156px` | 侧栏宽度 |
| `--ems-sidebar-collapsed` | `3rem` | 折叠侧栏宽度 |
| `--ems-header-height` | 由顶栏实测写入 | 固定顶栏、遮罩 top、brand 高度对齐 |
| `--ems-header-nav-font-size` | `0.875rem` | 顶栏链接与分组下拉（配合 `body.text-sm`） |

`syncBrandHeaderHeight()` 在主题/字号变更后更新 `--ems-header-height`。

---

## JS API（布局相关）

由 `emsShell` / `layout.ts` / `shell.ts` 导出：

| 函数 | 说明 |
|------|------|
| `isMobileSidebarLayout()` | `innerWidth <= 991.98` |
| `openMobileSidebar()` | 窄屏展开侧栏（含遮罩） |
| `closeMobileSidebar()` | 窄屏收起侧栏 |
| `ensureMobileSidebarOverlay()` | 确保遮罩 DOM 存在 |
| `ensureMobileHeaderBrand()` | 确保顶栏 logo 存在（`createNav` / `initShellLayout` 内调用） |

业务代码**一般无需直接调用**；扩展窄屏行为时可复用上述 API。

---

## 门户 / 下游应用集成注意

### `app/nav.ftl`

```javascript
var navMenu = emsShell.createNav(app, portal, menusJson, params, false);
navMenu.displayAppMenus('${nav.app.name}');  // 下游应用：首屏显示当前应用所属分组菜单
emsShell.setup(theme, params);
```

- Portal 首页（`app.name === portal.name`）：`restoreNav()` 默认**第一个分组**，除非 URL 带 `group.id` 或 session 快照。
- 下游 Webapp：`displayAppMenus` 定位到当前应用分组。

### 菜单 JSON

`createNav` 第三个参数须为含 `groups` 数组的 `DomainMenus`，或 groups 数组本身；缺失时按空数组处理，避免 `groupMenus` 未定义。

### 静态资源

修改布局后需重新构建并部署 `ems-shell`：

```bash
cd packages/ems-shell && npm run build && npm run package
```

---

## 相关源码

| 路径 | 职责 |
|------|------|
| `src/css/ems-shell.css` | 断点、`sidebar-open`、遮罩、group toggle、顶栏 logo |
| `src/js/layout.ts` | 窄屏侧栏开/关、遮罩、顶栏 logo |
| `src/js/shell.ts` | `initShellLayout()`、汉堡与顶栏点击 |
| `src/js/nav/factory.ts` | `prependGroupToggle`、`bindGroupToggleNav` |
| `src/js/nav/menu.ts` | `addTopGroups`、`displayGroupMenus`、`bindTopGroupNav` |
| `src/js/constants.ts` | `EMS_LAYOUT_MOBILE_MAX_WIDTH` |
| `app/src/main/resources/.../nav.ftl` | 壳层 HTML 骨架 |

---

## 维护清单

修改自适应行为时，请同步检查：

1. **断点一致**：`EMS_LAYOUT_MOBILE_MAX_WIDTH`、`ems-shell.css` 中 `@media`、`shell.ts` 中 `isMobileSidebarLayout()`、`initShellLayout` 汉堡分支。
2. **z-index**：侧栏 `1038` > 遮罩 `1037` > 固定顶栏 `1033`；宽屏 `brand-link` `1035`。
3. **窄屏勿对 brand-link 使用 fixed**（除非同时改顶栏占位与 group toggle 位置）。
4. **顶栏 logo 勿绑定 `clearNavState`**，侧栏 brand 保留即可。
5. **iframe 场景**：收起侧栏必须依赖遮罩，不能仅依赖 `document` 点击委托。
