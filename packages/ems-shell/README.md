# @beangle/ems-shell

Beangle EMS 门户壳层：侧栏导航、工作台多标签、主题与布局。

源码仓库：[github.com/beangle/ems](https://github.com/beangle/ems)（`packages/ems-shell`）

## 安装

```bash
npm install @beangle/ems-shell
```

npm 包同时包含 **TypeScript 源码** 与 **预编译静态资源**，安装后目录结构：

```
@beangle/ems-shell/
  LICENSE
  README.md
  dist/                 # 编译产物（可直接部署）
    js/ems-shell-min.js
    js/ems-shell.js
    js/ems-shell.js.map
    css/ems-shell-min.css
    css/ems-shell.css
    css/login.css
  src/                  # TS / CSS 源码
  test/                 # 单元测试
  build.mjs
  package.mjs
  tsconfig.json
```

## 使用编译产物

| 文件 | 说明 |
|------|------|
| `dist/js/ems-shell-min.js` | 生产 JS（门户默认引用） |
| `dist/css/ems-shell-min.css` | 生产 CSS |
| `dist/css/login.css` | 登录页样式 |
| `dist/js/ems-shell.js` | 开发版 JS（含 source map） |
| `dist/css/ems-shell.css` | 开发版 CSS |

## Beangle 门户集成

`app/src/main/resources/beangle.xml` 中声明 bundle（版本与 `package.json` 一致）：

```xml
<bundle name="ems-shell" version="0.0.1">
  <module name="ems-shell"
          js="js/ems-shell-min.js"
          css="css/ems-shell-min.css"
          depends="wujie"/>
</bundle>
```

**静态资源由独立静态服务器提供**，不打进 portal/app 的 JAR。将 `dist/` 部署到静态站点目录：

```text
{static_base}/ems-shell/{version}/
  js/
  css/
  images/
```

`{static_base}` 由运行环境配置（如 `Ems.static`，默认 `{base}/static`）。执行 `npm run package` 会生成 `release/ems-shell-{version}.zip`，解压到静态站点根目录即可得到 `ems-shell/{version}/` 目录结构。

页面侧：

```javascript
beangle.require(["wujie", "ems-shell"], function (wujie, emsShell) {
  // emsShell.createNav(...)
});
```

全局对象：`window.emsShell`。

## 从源码构建

在已安装的包目录或 monorepo 中：

```bash
npm install
npm run build      # → dist/
npm run typecheck
npm test
npm run package    # → release/ems-shell-{version}.zip
```

## 多标签工作台设计说明

工作台标签的行为约定与**刻意不实现**的增强见 [docs/workspace-tabs.md](./docs/workspace-tabs.md)。

要点：

- **iframe 标签的 URL 与标题**保持打开时的原值，不跟随 iframe 内页内跳转更新。
- **标签过多时不做** chevron 滚动、「全部标签」列表等扩展；仅横向滚动，不鼓励多开标签。

详见 [docs/workspace-tabs.md](./docs/workspace-tabs.md)。

## 许可证

Apache License 2.0 — 见 [LICENSE](./LICENSE)。
