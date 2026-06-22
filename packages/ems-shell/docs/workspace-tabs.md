# 多标签工作台 — 设计说明

本文记录 `@beangle/ems-shell` 工作台标签的**有意为之**的行为与**刻意不做**的增强，便于后续维护时避免重复讨论或误实现。

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
