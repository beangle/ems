# 本地运行 Portal

主类 `org.beangle.sas.engine.tomcat.Bootstrap`（见 `build.sbt` 的 `Compile / mainClass`），由 `beangle-sas-engine` 起内嵌 Tomcat。

```bash
# 方式一：sbt 任务（TomcatPlugin 会自动追加 --dev=true）
sbt ";portal/tomcatStart --port=8082"

# 方式二：自行拼 classpath 后直接跑
java -Dems.profile=lixin.course -cp <classpath> org.beangle.sas.engine.tomcat.Bootstrap --port=8082
```

## 配置来源

- `ems.home`：EMS 安装目录。由 `-Dems.home` 直接指定，或由 `-Dems.base`（默认 `~/.ems`）+ `-Dems.profile` 拼出；
  未指定 `ems.profile` 时取 `~/.ems` 下唯一的目录，有多个则取第一个并打告警。
- `conf.properties`：放在 `ems.home` 下，提供 `base`（门户基地址，缺省 `http://localhost`）、`key`（内部调用签名与加密密钥）、
  以及可选的 `cas`/`portal`/`index`/`api`/`blob`/`webapp`/`static`/`inner_base` 地址。取值见 `EmsEnv.readEnv`。
- `-Dems.profile` 也会被回写成同名系统属性，供运行期读取。

## 开发环境

- 开发库种子数据里的管理员：`imroot/123456`（`init/8-seed.sql`）。
- 前端静态资源包 `ems-shell` 的版本写在 `app/src/main/resources/beangle.xml` 的 `<bundle name="ems-shell" version="...">`，
  与 `packages/ems-shell/package.json` 的 `version` 必须一致；在 `packages/ems-shell` 下 `npm run build` 产出 dist。
- 原生镜像构建与分发（`nativeImage`/`nativeDist`/上传）见 `beangle/build` 的 `docs/native.md`，本仓库只负责 `-Os` 等构建参数。
