# 分层与边界

## 四层结构

```
门户宿主壳            @beangle/ems-shell（GPL-3.0-or-later，IIFE 静态包，跑在 portal 页面）
   │  URL 打开子应用 + 总线事件 + localStorage
   ▼
应用接入层（本包）     @beangle/ems-app（GPL-3.0-or-later，ESM，跑在子应用内）
   │  base URL / token / 权限 / 运行时配置
   ▼
业务 SDK              @openurp/sdk-vue（业务模型、选择器、课表）
   ▼
组件与样式层          @beangle/bui-vue（GPL-3.0-or-later，DataGrid、主题变量、调色板）
```

依赖方向单向向下：宿主壳不依赖接入层，接入层不依赖业务 SDK，业务 SDK 可依赖接入层的
请求客户端（`api/request`、`api/jsonapi`）。

## 为什么接入层是独立包，而不是并进 `@beangle/ems-shell`

两者同属 `beangle/ems` 仓库（同一个 pnpm workspace，见根 `pnpm-workspace.yaml`），
许可也一致（GPL-3.0-or-later），但仍是**两个包**：

1. **产物不同**：宿主壳是 IIFE 静态资源（门户通过 `beangle.require` 加载，版本写死在
   `app/src/main/resources/beangle.xml` 的 `<bundle>`），接入层是 ESM npm 包（含 `.d.ts`）；
2. **运行位置不同**：宿主壳在门户窗口（jQuery / `window.wujie`），接入层在子应用沙箱
   （自己的 `window` / localStorage）；
3. **发布节奏不同**：宿主壳跟着门户静态资源走，接入层按 npm 语义化版本发布，
   消费端（xurp 各应用）只依赖后者。

同仓库的好处是契约与宿主实现能一次改完、一眼对照；两者仍然**零代码依赖**，
只共享契约常量（见 `ems-shell-contract.md`）。

## 多端策略

- 主入口（`.`）：端无关，只依赖 `vue` + `vue-router`；`tdesign-*` 声明为 optional peer，
  移动端（vant 等技术栈）安装时不会被迫引入 tdesign。
- `./pc`：PC 独立部署壳（`AppShell` + 三个开关 + `app-shell.css`），只有 tdesign 依赖。
- 规则：新增只适合某一端的组件时，放对应子入口，不要进主入口。

## 状态与存储

- **本应用会话**：`beangle.ems.auth_token`、`beangle.ems.user_info`（本包读写）。
- **门户偏好**：`beangle.ui.*`（门户写入，本包只读 + 监听，不回写）。
- **门户工作台**：`beangle.ems.nav_tabs`、`beangle.ems.multi_tab`、`beangle.ems.nav_sticky_header`
  （宿主维护，本包只声明常量，避免双方各写一份字面量）。
