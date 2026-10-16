# Layers and boundaries

## Four layers

```
portal host shell       @beangle/ems-shell (GPL-3.0-or-later, IIFE static asset, runs in the portal page)
   │  opens guest apps by URL + bus events + localStorage
   ▼
app integration layer   @beangle/ems-app (this package, GPL-3.0-or-later, ESM, runs inside the guest app)
   │  base URL / token / permissions / runtime config
   ▼
business SDK            @openurp/sdk-vue (models, pickers, timetable)
   ▼
components and styles   @beangle/bui-vue (GPL-3.0-or-later, DataGrid, theme vars, palette)
```

Dependencies point one way only: the host shell does not depend on the integration layer, the
integration layer does not depend on the business SDK, and the business SDK may depend on the
integration layer's request clients (`api/request`, `api/jsonapi`).

## Why the integration layer is its own package

It could have been folded into `@beangle/ems-shell`: both live in `beangle/ems` (one pnpm
workspace, see the root `pnpm-workspace.yaml`) and share the same license. They stay separate
because:

1. **Different artifacts** — the host shell is an IIFE static asset (loaded by the portal through
   `beangle.require`, version pinned in `app/src/main/resources/beangle.xml` `<bundle>`), while
   the integration layer is an ESM npm package with `.d.ts`;
2. **Different runtime** — the host shell runs in the portal window (jQuery, `window.wujie`), the
   integration layer runs in the guest sandbox with its own `window` / localStorage;
3. **Different release cadence** — the host shell ships with the portal static assets, the
   integration layer follows npm semver and is the only one consumer apps depend on.

Being in one repo keeps the contract and the host implementation one change apart. The two still
share no code, only the constants described in `ems-shell-contract.md`.

## Multi-platform

- Main entry (`.`): platform neutral, only `vue` + `vue-router`; `tdesign-*` are optional peers,
  so a mobile app (vant and friends) is never forced to install tdesign.
- `./pc`: PC standalone shell (`AppShell`, three switches, `app-shell.css`), tdesign only.
- Rule: a component that fits one platform only goes into its own entry, never the main one.

## State and storage

- **App session**: `beangle.ems.auth_token`, `beangle.ems.user_info` (read and written here).
- **Portal preferences**: `beangle.ui.*` (written by the portal; read-only here, plus listeners).
- **Portal workspace**: `beangle.ems.nav_tabs`, `beangle.ems.multi_tab`,
  `beangle.ems.nav_sticky_header` (owned by the host; declared here as constants so that neither
  side writes its own literals).
