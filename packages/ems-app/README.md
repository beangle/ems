# @beangle/ems-app

EMS 应用侧（guest）接入层：登录会话与 token、权限对接、运行时配置、微前端（Wujie）集成、
门户 UI（语言 / 主题 / 字号）同步、CAS 扫码登录，以及宿主与子应用之间的契约常量。

面向**多端**：主入口只依赖 `vue` + `vue-router`，PC 与移动端都能引用；
需要 tdesign 的 PC 独立壳走子入口 `@beangle/ems-app/pc`。

## 与 `@beangle/ems-shell` 的关系

| | `@beangle/ems-shell` | `@beangle/ems-app`（本包） |
|---|---|---|
| 运行位置 | 门户宿主窗口（portal 页面） | 子应用内（Wujie 沙箱或独立部署） |
| 职责 | 菜单树渲染、工作台多 Tab、wujie `startApp`/`destroyApp`、主题下发 | 登录态与 token、权限拉取、运行时配置、微前端生命周期、UI 偏好同步 |
| 产物 | esbuild IIFE 静态资源（门户 `<bundle>` 引用） | ESM npm 包（`dist` + `.d.ts`） |
| 许可 | GPL-3.0-or-later | GPL-3.0-or-later |

两者**零代码依赖**，只通过一套字符串契约协作（见 `docs/ems-shell-contract.md`）：
localStorage key、自定义事件名、Wujie 总线事件名、URL 参数名。契约常量集中在本包
`src/contract.ts`，并用 `tests/contract.spec.ts` 冻结字面量。两者同属 `beangle/ems`
仓库（`packages/ems-shell`、`packages/ems-app`），由一个 pnpm workspace 统一管理依赖。

## 安装

```bash
pnpm add @beangle/ems-app
```

消费端只写**版本号**，不要写 `link:` / `file:`（发布脚本会直接拦截）：

```json
"@beangle/ems-app": "^0.0.2"        // 正式发布版
"@beangle/ems-app": "^0.0.2-dev.0"  // 联调期跟随 dev 预览（同时接受正式版 0.0.2）
"@beangle/ems-app": "dev"           // 永远跟随最新预览（dist-tag）
```

联调上游改动时**不要在消费端链接本地目录**：先在本仓库发布开发预览版
（`pnpm release:dev`，版本形如 `0.0.2-dev.<UTC 时间戳>`、dist-tag `dev`），
消费端 `pnpm install` 即可取到；内网环境可把 `npm pack` 的 tgz 交给 micdn
（`micdn install <pkg>.tgz --tag dev`）由本地 registry 提供。

## 消费端注意：产物是 bundler 目标

`dist/*.js` 按 bundler 解析发布，**不是** Node 原生 ESM 可直接 import 的产物：
`dist/pc.js` 里有 `tdesign-vue-next/es/dropdown` 这类目录导入，而 tdesign 的 `es/*.mjs`
自身还 `import './style/css.mjs'`（CSS）。Node 原生 ESM 会分别报
`Directory import ... is not supported` 与 `Unknown file extension ".css"`。

- 浏览器 `vite build` / dev server：正常，无需额外配置；
- vitest / SSR / node 脚本等 Node 环境：把本包（以及再导出它的 `@openurp/sdk-vue`）
  放进测试运行器的 inline 名单，交给 vite 处理，例如 vitest：

```ts
// vitest.config.ts
test: {
  server: {
    deps: {
      inline: ['@beangle/ems-app', '@openurp/sdk-vue', 'tdesign-vue-next'],
    },
  },
}
```

## 入口

| 入口 | 内容 | 依赖 |
|---|---|---|
| `@beangle/ems-app` | 登录会话、权限、运行时配置、请求客户端、微前端、门户 UI 同步、扫码登录、契约常量、`MicroShell` | `vue`、`vue-router` |
| `@beangle/ems-app/pc` | `AppShell` 及语言/主题/全屏开关（独立部署时的应用壳） | 追加 `tdesign-vue-next`、`tdesign-icons-vue-next`（optional peer） |
| `@beangle/ems-app/styles/app-shell.css` | `AppShell` 样式源码入口 | — |

`dist/index.css` 已包含 `AppShell` 样式，按需二选一引入。

侧栏菜单的渲染与样式（规格取自 EMS 门户侧栏）在 `@beangle/bui-vue` 的 `SideNav`
组件里，`AppShell` 只负责传入菜单树与当前路径。该组件的样式按 bui-vue 的约定单独引出，
**独立部署用 `AppShell` 时必须额外引入一次**（`dist/index.css` 里没有）：

```ts
import '@beangle/ems-app/dist/index.css'
import '@beangle/bui-vue/styles/side-nav.css'
```

## 快速接入

子应用入口（Wujie 与独立部署都适用）：

```ts
import { createApp } from 'vue'
import {
  bindPortalUiSync,
  createEmsAuth,
  createEmsPermissions,
  createEmsRuntimeConfig,
  isWujieMicroApp,
  loadEmsRuntimeConfigs,
  registerWujieApp,
} from '@beangle/ems-app'
import '@beangle/ems-app/dist/index.css'
```

根容器：微前端下用 `MicroShell`，独立部署下可用 `pc` 入口的 `AppShell`（见 `src/App.vue`
的两种形态）。本地调试直接访问带 `?micro=1` 的地址即可走微前端壳层。

## 模块

| 模块 | 说明 |
|---|---|
| `api/auth` | CAS 登录 / 登出、`URP_SID`、token 与用户信息存取、登录态初始化 |
| `api/permissions` | 消费平台下发的用户功能权限（管理员在门户维护，应用不自建权限表）：扁平资源数组或路由结构 → `canRoute` / `can`，缺省 fail-open |
| `api/runtimeConfig` | `configs.json` / `themes.json` → `ems_base`、`cas_*`、主题、logo |
| `api/request` | 统一请求客户端：base URL、Bearer / `URP_SID`、跨域判断 |
| `api/jsonapi` | JSON:API 客户端 `EmsJsonApiClient`：文档 / 错误类型、`page[...]` / `filter[...]` 查询、401/302 跳登录、blob 下载；并把资源文档摊平成业务行（`unwrapResource` / `unwrapOne` / `unwrapMany`） |
| `api/qrLogin` + `useQrLogin` | CAS 扫码登录（kiosk）：二维码、SSE 订阅、轮询降级 |
| `utils/microApp` | `isWujieMicroApp` / `registerWujieApp`（挂载时机） |
| `utils/portalUi` | 门户语言 / 主题 / 字号同步（Wujie 总线 + storage + 自定义事件） |
| `contract` | 宿主↔子应用契约常量 |

## 边界（什么不放这里）

- 门户宿主逻辑（菜单、多 Tab、`startApp`）→ `@beangle/ems-shell`
- 业务 SDK（选人 / 选学期 / 码表 / 课表等）→ `@openurp/sdk-vue`
- 通用 Vue 组件与表格样式 → `@beangle/bui-vue`（传输层 / JSON:API 解析不放组件库）

## 开发

```bash
# 仓库根目录装依赖（workspace 内所有包一次装好）
pnpm install

pnpm --filter @beangle/ems-app typecheck
pnpm --filter @beangle/ems-app test
pnpm --filter @beangle/ems-app build
```

在本包目录内直接 `pnpm test` / `pnpm build` 等价（依赖来自根 workspace）。

## 发布

`ems-app` 位于 `beangle/ems` 的 `packages/` 下——该仓库同时承载 Scala 侧代码（tag 序列 `v4.x`）
与其他前端包，仓库级分支与标签由仓库流程负责，因此**包级发布脚本不执行任何 git 操作**
（不 checkout、不 merge develop、不打 tag、不 push）。版本以 `package.json` 与 registry 为准。

版本号遵循 npm/semver 约定：

| 用途 | 版本号 | dist-tag |
|---|---|---|
| 正式版 | `X.Y.Z` | `latest` |
| 开发预览 | `X.Y.Z-dev.<UTC yyyyMMddHHmmss>` | `dev` |

- `package.json` 里始终写「下一个正式版号」的纯 `X.Y.Z`：不要写 `-SNAPSHOT`（Maven/Java 概念），
  也**不重发同一个版本号**（npm 的 lockfile/store 按 integrity 寻址，重发只会让消费方卡在旧字节）；
- 正式发布：`pnpm release:prepare` 通过后执行 `pnpm publish --tag latest --no-git-checks`，
  再用 `pnpm release:finish` 算出下一个版本号，手工改 `package.json` 并提交；
- 开发预览：`pnpm release:dev`（自动加 UTC 时间戳并以 `--tag dev` 发布），
  消费方用 `@beangle/ems-app@dev` 跟随最新预览，或写 `^X.Y.Z-dev.0`。

许可：GPL-3.0-or-later。详见 `docs/architecture.md`、`docs/ems-shell-contract.md`。
