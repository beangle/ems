# 多标签工作台 — 设计说明

本文记录 `@beangle/ems-shell` 工作台标签的**有意为之**的行为与**刻意不做**的增强，便于后续维护时避免重复讨论或误实现。

**门户用户使用说明**见 [tabs-user-guide.md](./tabs-user-guide.md)。

## 标签元数据：打开时确定，不随页内跳转变化（iframe）

### 决策

**iframe 模式下的标签，`url` 与 `title` 保持打开菜单时的原值，不随 iframe 内页内导航更新。**

### 背景

曾评估过「iframe 标签的 URL / 标题跟随页内跳转」：在 iframe 内点击链接后，用 `onload` 读取 `contentWindow.location`、`document.title`，同步到标签标题、 `tab.url`、去重键 `tabByUrl`、地址栏 history 等。

### 不实现的原因

1. **跨域**：非同源 iframe 无法读取子页 location / title，行为不一致。
2. **与门户 history 模型冲突**：标签切换使用 `pushState` / `replaceState` 写门户 hash；iframe 内跳转若再写回 `tab.url`，需额外规则区分「用户点标签」与「子页导航」，易与 `popstate`、session 恢复打架。
3. **去重与快照**：`tabByUrl`、session 快照、侧栏 `locateMenu(tab.url)` 均假设 `tab.url` 是**入口 URL**；随子页漂移会导致重复标签、恢复错页、菜单高亮错位。
4. **标题语义**：用户从菜单打开时，标签名表示「从哪进的」；若随子页 title 变化，多标签对比与「关闭其他」的语义会变模糊。

### 当前行为

| 项目 | 行为 |
|------|------|
| 标签标题 | 菜单名（或打开时的 fallback），不变 |
| `tab.url` | 打开时的路径，不变 |
| iframe `src` | 初始为 `tab.url`；之后由 iframe 内导航自行变化，**不回写** shell |
| 刷新 / session 恢复 | 仍按打开时的 `url` + `title` 重建标签并加载入口地址 |
| 地址栏（门户） | 随**标签切换**更新，不随 iframe 内跳转更新 |

### 相关代码

- 挂载：`src/js/nav/workspace.ts` → `mountIframeInWorkspacePanel`
- 标签状态：`src/js/nav/tabs.ts`（`openMenu`、`persistNavTabsSession`、`restoreNavTabsFromSession`）

### 备注

无界（wujie）子应用若通过 `props.jump` 等机制更新路由，属于微前端沙箱内的约定，**不等同于**上述 iframe 跟随方案；是否扩展 wujie 的 tab 元数据同步应单独评估，不在本条决策范围内。

## 双击刷新

标签标题区域 **双击** 调用 `refreshNavTab`：按打开时记录的 `tab.url` 重新加载内容（iframe / ajax / wujie 各自走已有刷新逻辑），**不更新** `tab.url` 与标题。

- 单击仍切换标签（250ms 延迟，避免与双击冲突）
- 右侧关闭 / 固定按钮不参与双击刷新
- 右键菜单「刷新」与双击效果相同

## 标签过多时的导航：不扩展滚动/列表（刻意不做）

### 决策

**不实现**标签栏在标签过多时的额外导航能力（如左右 `‹ ›` 滚动按钮、「全部标签」下拉列表、搜索/filter 等）。标签条仅保留现有 **横向滚动**（`overflow-auto` + 激活项 `scrollIntoView`）。

### 背景

标签数量较多（例如超过 10 个）时，仅靠横向滚动不易定位。曾评估参考浏览器或 IDE 标签栏的增强：chevron 逐页滚动、可搜索的标签总览下拉等。

### 不实现的原因

**不鼓励用户同时打开大量标签。** 工作台设计假设用户以少量标签并行工作；标签过多时应通过 **关闭不需要的标签**、**固定常用入口**、或依赖 **侧栏菜单 / 搜索** 重新打开，而不是在标签条上再叠一层「管理很多标签」的 UI。

额外导航控件会：

1. 暗示「开很多标签是正常用法」，与产品取向相反；
2. 增加工具栏复杂度，而多数场景标签数应在可控范围内（另有 `maxTabCount` 上限与溢出腾位）；
3. 与已有能力重复：键盘 ←/→ 切换、右键关闭其他、固定首页/常用页。

### 当前行为

| 项目 | 行为 |
|------|------|
| 标签条溢出 | 横向滚动；切换激活标签时滚入可视区 |
| 数量上限 | `maxTabCount`（默认 30，可配置）；满额时关闭最右侧未固定标签 |
| 推荐用法 | 少开标签；固定首页/常用页；及时关闭；用菜单或搜索重新进入 |

### 相关代码

- 标签条容器：`src/js/nav/workspace.ts` → `ensureNavWorkspace`（`ems-nav-tabs-scroll`）
- 激活滚入视口：`src/js/nav/tabs.ts` → `activateNavTab`

## 无界子应用缓存策略：只强刷入口，不干预子应用请求

### 决策

**壳层传给无界的 `fetch` 只负责两件事：统一补 `credentials` / `mode`；需要强刷时，只对「入口文档」这一个请求加 `cache: 'no-store'`。子应用自身发出的请求不再被改写缓存策略，遵循 HTTP 响应头（`Cache-Control`）与子应用传给 `fetch` 的 `init`。**

### 背景

无界会把 `startApp({ fetch })` 装到子应用沙箱的 `window.fetch` 上（wujie-core `sandbox.active`），所以这个 fetch 有两个调用方：无界加载子应用入口 HTML/JS/CSS，以及子应用内部的所有请求。

早期实现在此处无条件加 `cache: 'no-store'`，等于替所有子应用关掉了浏览器 HTTP 缓存：子应用即使声明 `Cache-Control: private, max-age=300`（甚至自己传 `cache` 选项）也会被覆盖，门户里每次重开标签都要整份重新下载（例如选课建议的 msgpack 响应 200KB+）。

### 当前行为

| 项目 | 行为 |
|------|------|
| `credentials` / `mode` | 所有请求统一 `credentials: 'include'`、`mode: 'cors'`；失败时降级为 `credentials: 'omit'` 重试一次 |
| 入口文档 | `wujieFetchNoStore=true`（门户默认）、`wujieAlive=false` 或 `wujieEntryReload=true` 时：入口 URL 追加 `_=时间戳`，且该请求带 `cache: 'no-store'` |
| 入口之外的请求（子应用接口、入口页引用的 JS/CSS） | **不改写**缓存策略，按子应用与服务端自己的 `Cache-Control` 走 |
| 参数 | `wujieFetchNoStore` 名称保留以兼容既有配置，语义收窄为「入口强刷」，与 `wujieEntryReload` 等价 |

### 相关代码

- 壳层 fetch 包装：`src/js/nav/tabs.ts` → `startWujieAppForTab`（`wujieShouldBustEntryUrl` / `wujieBustEntryUrl`）
- 入口文档判定：`src/js/wujie.ts` → `isWujieEntryRequest`

### 备注

入口强刷只保证拿到新的入口 HTML；入口引用的静态资源带内容 hash，可长期缓存。因此部署侧仍需为 `index.html` 配置 `no-cache`（或依赖 `wujieFetchNoStore` 的 `_=` 时间戳），`/assets/*` 建议 `public, max-age=31536000, immutable`。
