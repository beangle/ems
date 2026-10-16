# Host (ems-shell) ↔ guest app contract

`@beangle/ems-shell` and the guest app (this package) share no code, only string conventions.
This page is the only description of them. Implementations:

| Side | Location |
|---|---|
| Guest app | `src/contract.ts` (single source of truth) plus `src/api/auth.ts`, `src/utils/portalUi.ts` |
| Host | `beangle/ems/packages/ems-shell/src/js/{storage,theme,tab-keys,wujie,config}.ts` |
| Components | `beangle/bui-vue/src/palette.ts` (theme palette) |
| Session / ticket | `beangle/ems/docs/ems-cas.md`, `ems-cas-impl.md` |

## localStorage keys

| Key | Writer | Reader | Meaning |
|---|---|---|---|
| `beangle.ems.auth_token` | guest | guest | session token of the guest app |
| `beangle.ems.user_info` | guest | guest | current user (JSON) |
| `beangle.ems.nav_tabs` | host | host | workspace tab state |
| `beangle.ems.multi_tab` | host | host | multi-tab preference |
| `beangle.ems.nav_sticky_header` | host | host | top bar sticky state |
| `beangle.ui.locale` | host / app | read by both | locale (`zh-CN` / `en-US`) |
| `beangle.ui.theme` | host | read by both | theme palette JSON |
| `beangle.ui.theme-mode` | host / app | read by both | `light` / `dark` |
| `beangle.ui.font-size` | host / app | read by both | `small` / `medium` / `large` |

## Same-page custom events

Emitted by the host and listened to by the guest app through `window.addEventListener`, for the
same-page case (no iframe sandbox):

| Event | detail |
|---|---|
| `beangle.ui.localechange` | locale tag |
| `beangle.ui.themechange` | theme mode |
| `beangle.ui.fontsizechange` | font size step |

Cross-page cases (multiple Wujie instances) fall back to the `storage` event or the bus below.

## Wujie bus events

Emitted by the host with `emitWujieBus(event, value)`, subscribed by the guest app through
`window.$wujie.bus.$on`:

| Event | Payload |
|---|---|
| `locale-change` | `zh-CN` / `en-US` |
| `theme-mode-change` | `light` / `dark` |
| `font-size-change` | `small` / `medium` / `large` |

Wujie `props` also carry initial values (`locale`/`lang`, `themeMode`, `fontSize`); the precedence
of this package's `detectPortal*` helpers is props → URL → localStorage → default.

## URL parameters

| Parameter | Direction | Meaning |
|---|---|---|
| `URP_SID` | CAS → guest | one-time session id for cross-origin renewal; the guest turns it into `Authorization: Bearer` or keeps it in the URL |
| `sid_name` | guest → CAS | session parameter name agreed with the login endpoint |
| `token` / `name` / `code` | CAS → guest | login result passed back directly (local or trusted redirect) |
| `micro=1` | debugging | makes a standalone deployment take the micro-frontend shell (snapshotted at module load) |
| `contextProfileId` | portal → app | current business context (profile / Env) |

## Sandbox semantics

A Wujie guest app runs in an iframe sandbox: `localStorage` and `window` are **not** shared with
the host. Therefore:

- the host reads and writes `beangle.ui.*` and `beangle.ems.nav_*`; the guest app only reads them
  and applies them to its own UI when an event arrives;
- the guest app's session storage (`beangle.ems.auth_token` and friends) is visible to the guest
  only; the host does not read it;
- detect the micro-frontend with `isWujieMicroApp()` instead of sniffing host DOM.

## Changing the contract

1. Update `src/contract.ts` and `tests/contract.spec.ts` (frozen literals);
2. Update the tables on this page;
3. Check the host implementation (`ems-shell/src/js/*`) and `bui-vue/src/palette.ts`;
4. Release both sides (the host also needs `<bundle name="ems-shell" version>` in the portal's
   `beangle.xml` updated).

Both sides now live in `beangle/ems` (one pnpm workspace), so a contract change and the host
implementation can land in one commit — but they are still two packages with zero code sharing.
Should "change once, both sides follow" ever become necessary, extract a zero-dependency
constants package for both to depend on; for now the convention plus the frozen test is enough,
and it saves one more release pipeline.
