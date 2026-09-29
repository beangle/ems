# 宿主（ems-shell）↔ 子应用契约

宿主 `@beangle/ems-shell` 与子应用（本包）不共享代码，只共享字符串约定。本页是唯一说明，
对应实现：

| 侧 | 位置 |
|---|---|
| 子应用 | 本包 `src/contract.ts`（唯一出处）+ `src/api/auth.ts`、`src/utils/portalUi.ts` |
| 宿主 | `beangle/ems/packages/ems-shell/src/js/{storage,theme,tab-keys,wujie,config}.ts` |
| 组件层 | `beangle/bui-vue/src/palette.ts`（主题色板） |
| 会话/票据 | `beangle/ems/docs/ems-cas.md`、`ems-cas-impl.md` |

## localStorage key

| Key | 写入方 | 读取方 | 说明 |
|---|---|---|---|
| `beangle.ems.auth_token` | 子应用 | 子应用 | 本应用会话 token |
| `beangle.ems.user_info` | 子应用 | 子应用 | 当前用户（JSON） |
| `beangle.ems.nav_tabs` | 宿主 | 宿主 | 工作台多 Tab 状态 |
| `beangle.ems.multi_tab` | 宿主 | 宿主 | 多 Tab 开关偏好 |
| `beangle.ems.nav_sticky_header` | 宿主 | 宿主 | 顶栏吸顶状态 |
| `beangle.ui.locale` | 宿主 / 应用 | 双方只读 | 语言（`zh-CN` / `en-US`） |
| `beangle.ui.theme` | 宿主 | 双方只读 | 主题色板 JSON |
| `beangle.ui.theme-mode` | 宿主 / 应用 | 双方只读 | `light` / `dark` |
| `beangle.ui.font-size` | 宿主 / 应用 | 双方只读 | `small` / `medium` / `large` |

## 同页自定义事件

子应用通过 `window.addEventListener` 监听，宿主在**同页**（非 iframe 沙箱）时使用：

| 事件名 | detail |
|---|---|
| `beangle.ui.localechange` | 语言标签 |
| `beangle.ui.themechange` | 主题模式 |
| `beangle.ui.fontsizechange` | 字号档位 |

跨页场景（Wujie 多实例）用 `storage` 事件兜底，或下面的总线事件。

## Wujie 总线事件

宿主 `emitWujieBus(event, value)` 发出，子应用通过 `window.$wujie.bus.$on` 订阅：

| 事件名 | 载荷 |
|---|---|
| `locale-change` | `zh-CN` / `en-US` |
| `theme-mode-change` | `light` / `dark` |
| `font-size-change` | `small` / `medium` / `large` |

另外，Wujie `props` 上会带初始值（`locale`/`lang`、`themeMode`、`fontSize`），
本包 `detectPortal*` 的优先级为：props → URL → localStorage → 默认值。

## URL 参数

| 参数 | 方向 | 说明 |
|---|---|---|
| `URP_SID` | CAS → 子应用 | 跨域会话续期的一次性会话 id；子应用转成 `Authorization: Bearer` 或续挂在 URL 上 |
| `sid_name` | 子应用 → CAS | 登录接口约定的会话参数名 |
| `token` / `name` / `code` | CAS → 子应用 | 直接回传的登录结果（本地/受信跳转） |
| `micro=1` | 调试 | 让独立部署的应用走微前端壳层（模块加载时快照一次） |
| `contextProfileId` | 门户 → 应用 | 当前业务场景（profile / Env）切换 |

## 沙箱语义

Wujie 子应用运行在 iframe 沙箱内：`localStorage`、`window` 与宿主**不共享**。
因此：

- 宿主读写 `beangle.ui.*` 与 `beangle.ems.nav_*`；子应用只读并在收到事件后应用到自身 UI；
- 子应用的会话存储（`beangle.ems.auth_token` 等）只对子应用可见，宿主不读；
- 判断当前是否在微前端内用 `isWujieMicroApp()`，不要直接嗅探宿主 DOM。

## 变更流程

1. 改本包 `src/contract.ts` 与 `tests/contract.spec.ts`（字面量冻结）；
2. 同步本页表格；
3. 核对宿主实现（`ems-shell/src/js/*`）与 `bui-vue/src/palette.ts`；
4. 双方各自发布（宿主需同步门户 `beangle.xml` 的 `<bundle name="ems-shell" version>`）。

宿主与接入层现在同在 `beangle/ems` 仓库（一个 pnpm workspace），契约改动与宿主实现可以
一次提交改完；但两者仍是两个包、零代码依赖。将来若希望“改一处、双方生效”，可以再抽一个
零依赖的常量包（只含常量与类型）由双方依赖；当前阶段维持“约定 + 冻结测试”，
避免为一个常量包增加一条发布链路。
