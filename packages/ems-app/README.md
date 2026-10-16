# @beangle/ems-app

Guest-app integration layer for EMS: login session and tokens, permissions, runtime config,
micro-frontend (Wujie) integration, portal UI preference sync (locale / theme / font size),
CAS QR login, and the string contract shared with the portal host.

Multi-platform by design: the main entry only needs `vue` + `vue-router`, so PC and mobile apps
can both use it. The PC-only shell lives in a separate entry, `@beangle/ems-app/pc`.

## Relation to `@beangle/ems-shell`

| | `@beangle/ems-shell` | `@beangle/ems-app` (this package) |
|---|---|---|
| Runs in | portal host window | guest app (Wujie sandbox or standalone) |
| Does | menu tree, workspace tabs, wujie `startApp`/`destroyApp`, theme push | session and token, permissions, runtime config, micro-app lifecycle, UI preference sync |
| Ships as | IIFE static asset (portal `<bundle>`) | ESM npm package (`dist` + `.d.ts`) |
| License | GPL-3.0-or-later | GPL-3.0-or-later |

The two packages share no code, only string conventions (localStorage keys, custom events, Wujie
bus events, URL params), defined in `src/contract.ts` and frozen by `tests/contract.spec.ts`.
See `docs/ems-shell-contract.md`.

## Install

```bash
pnpm add @beangle/ems-app
```

Pin a version; never `link:` / `file:` (the release scripts reject them):

```json
"@beangle/ems-app": "^0.0.2"        // released version
"@beangle/ems-app": "^0.0.2-dev.0"  // follow dev previews during integration
"@beangle/ems-app": "dev"           // always the newest preview (dist-tag)
```

To pick up upstream changes, publish a preview here (`pnpm release:dev`) and `pnpm install` in the
consumer; on an isolated network, load the `npm pack` tarball into micdn with
`micdn install <pkg>.tgz --tag dev`.

## Bundler target

`dist/*.js` targets bundlers, not native Node ESM: `dist/pc.js` uses directory imports
(`tdesign-vue-next/es/dropdown`) and tdesign's `es/*.mjs` imports CSS, which Node rejects with
`Directory import ... is not supported` and `Unknown file extension ".css"`.

- Browser (`vite build`, dev server): works as is;
- Node (vitest, SSR, scripts): inline this package (and `@openurp/sdk-vue`, which re-exports it)
  so that vite processes it:

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

## Entries

| Entry | Contents | Requires |
|---|---|---|
| `@beangle/ems-app` | session, permissions, runtime config, request client, micro-app, portal UI sync, QR login, contract constants, `MicroShell` | `vue`, `vue-router` |
| `@beangle/ems-app/pc` | `AppShell` plus locale / theme / fullscreen switches | `tdesign-vue-next`, `tdesign-icons-vue-next` (optional peers) |
| `@beangle/ems-app/styles/app-shell.css` | `AppShell` style source | — |

`dist/index.css` already includes the `AppShell` styles — import one or the other.
The sidebar is `SideNav` from `@beangle/bui-vue`, whose styles are a separate entry by that
package's convention, so a standalone `AppShell` also needs:

```ts
import '@beangle/ems-app/dist/index.css'
import '@beangle/bui-vue/styles/side-nav.css'
```

## Quick start

Guest app entry (works both under Wujie and standalone):

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

Root component: `MicroShell` under a micro-frontend, `AppShell` (from the `pc` entry) when
standalone — see the two forms in `src/App.vue`. For local debugging, open the app with `?micro=1`
to take the micro-frontend shell.

## Modules

| Module | Purpose |
|---|---|
| `api/auth` | CAS login / logout, `URP_SID`, token and user storage, session init |
| `api/permissions` | Consumes platform-owned permissions (maintained in the portal, no app-side table): flat resource array or route tree → `canRoute` / `can`, fail-open by default |
| `api/runtimeConfig` | `configs.json` / `themes.json` → `ems_base`, `cas_*`, theme, logo |
| `api/request` | Shared request client: base URL, Bearer / `URP_SID`, cross-origin detection |
| `api/jsonapi` | JSON:API client `EmsJsonApiClient`: document / error types, `page[...]` / `filter[...]` queries, 401/302 to login, blob download, plus document flattening (`unwrapResource` / `unwrapOne` / `unwrapMany`) |
| `api/qrLogin` + `useQrLogin` | CAS QR login (kiosk): QR code, SSE subscription, polling fallback |
| `utils/microApp` | `isWujieMicroApp` / `registerWujieApp` (mount timing) |
| `utils/portalUi` | Portal locale / theme / font size sync (Wujie bus + storage + custom events) |
| `contract` | Host ↔ guest contract constants |

## Out of scope

- Portal host logic (menus, workspace tabs, `startApp`) → `@beangle/ems-shell`
- Business SDK (people / term / code table pickers, timetable) → `@openurp/sdk-vue`
- Generic Vue components and grid styles → `@beangle/bui-vue` (the transport and JSON:API layer
  does not belong in a component library)

## Development

```bash
pnpm install                                     # at the repo root, installs the whole workspace

pnpm --filter @beangle/ems-app typecheck
pnpm --filter @beangle/ems-app test
pnpm --filter @beangle/ems-app build
```

## Release

`ems-app` lives in `packages/` of `beangle/ems`, which also carries the Scala side (tag series
`v4.x`) and other frontend packages. Branch and tag handling belongs to the repo, so the package
release scripts do **no git work**; `package.json` and the registry are the source of truth.

Versions follow npm/semver:

| Purpose | Version | dist-tag |
|---|---|---|
| Release | `X.Y.Z` | `latest` |
| Dev preview | `X.Y.Z-dev.<UTC yyyyMMddHHmmss>` | `dev` |

- `package.json` carries the *next* release number suffixed with `-dev` (`0.0.3-dev` publishes
  `0.0.3`; never `-SNAPSHOT`, a Maven/Java concept), and a version number is never re-published:
  npm lockfiles and stores are keyed by integrity, so a re-publish only leaves consumers on the
  old bytes;
- Release: run `~/bin/npm-publish.sh` in this directory. It drops the `-dev` suffix, refuses a
  version that is already on the registry, runs build + test + `npm publish --dry-run`, publishes
  with `npm publish`, and finally rewrites `package.json` to the next `X.Y.(Z+1)-dev`, leaving
  the commit to you (it does no git work);
- Dev preview: `pnpm release:dev` (adds the UTC timestamp and publishes with `--tag dev`);
  consumers follow it with `@beangle/ems-app@dev` or `^X.Y.Z-dev.0`.

Publishing goes through `npm`, not `pnpm publish`: pnpm 11 gets a `404 Not Found` on `PUT` from
the npm registry for these scoped packages, while `npm publish` succeeds with the same token.
The scripts keep `pnpm` for build, test, and `publishLocal` (local registry).

License: GPL-3.0-or-later. See `docs/architecture.md` and `docs/ems-shell-contract.md`.
