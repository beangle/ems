# EMS CAS 实现要点（内部开发参考）

本文面向 **EMS / beangle-ids 的维护与二次开发人员**，说明统一认证（CAS）在代码层面的实现方式、模块划分、配置项与页面定制机制。对接方（业务应用、第三方系统）请参阅 [ems-cas.md](ems-cas.md)。

---

## 1. 整体架构

EMS 的认证能力由 **beangle-ids** 库提供（CAS 服务端协议实现），EMS 侧完成账号存储、策略配置与页面呈现：

- **beangle-ids**：无状态接入层，提供登录/退出/授权/扫码/票据等 Action 与 Web 服务端点；自身不实现页面（模板由部署方提供）。
- **EMS portal**：绑定 ids 的 Action，提供登录页、短信登录页、OAuth 授权页等 **FreeMarker 模板**；作为部署宿主承载 `/cas` 路径。
- **数据层**：Redis 承载 TGT / OAuth 授权码 / 扫码记录等会话态数据；DB 承载用户、第三方应用、OAuthToken 等持久数据。

```
浏览器 / 移动端 / 第三方应用
        │
        ▼
  ┌─────────────┐   绑定 Action + FTL 页面    ┌──────────────┐
  │  EMS portal  │ ────────────────────────▶  │  beangle-ids │
  │  (/cas 路径)  │     账号/策略/页面         │  (CAS 协议)   │
  └─────────────┘                            └──────────────┘
        │
        ▼
   Redis（TGT/授权码/扫码记录缓存） · DB（用户/第三方应用）
```

---

## 2. 模块与绑定

EMS 侧通过 cdi `BindModule` 装配 CAS 能力，四个核心模块：

| 模块 | 职责 |
|------|------|
| `core/cas/DefaultModule.scala` | CAS 入口配置：远程 CAS/LTPA/OpenID 切换、SecurityInterceptor、`CasSetting`、service 白名单、`OAuthServiceImpl` |
| `core/cas/TicketModule.scala` | 票据服务：`DefaultTicketCacheService`、`DefaultTicketRegistry`、`CasServiceImpl`、ST id 生成器、`DefaultQrcodeService`、`EmsCasAppInfoProvider` |
| `core/cas/SeurityModule.scala` | 会话与认证：`DBSessionRegistry`、会话 ID 策略、`DefaultAccountRealm`、`RealmAuthenticator`、`DefaultSecurityContextBuilder`、定时清理器 |
| `core/cas/CredentialModule.scala` | 凭据校验：`DefaultDBCredentialStore` / `DBLdapCredentialChecker` |

页面 Action 绑定在 `cas/action/DefaultModule.scala`（IndexAction、EditAction、OAuthAction、QrcodeAction 等），ids 自带 Action（LoginAction、SmsLoginAction、QrcodeAction、AuthAction 等）直接 `bind` 即可自动注册路由。

**路由 profile**：`beangle.xml` 中 `org.beangle.ids.cas.web.action` 与 `org.beangle.ems.cas.action` 两个 profile 均映射 `/cas`（`style="seo"`），挂 `web.Interceptor.cas`（SecurityInterceptor）与 `web.Interceptor.hibernate`。`org.beangle.ids.cas.web.ws`（`ServiceValidateAction`）**刻意不挂** SecurityInterceptor：业务应用服务端需匿名调用 `/cas/serviceValidate` 校验 ticket，挂拦截器会使匿名请求被跳转登录/返回 401；其安全性由票据本身保证（一次性、绑定 service）。

---

## 3. 密码登录实现要点

- **前端加密**：登录页用 CryptoJS AES-ECB 加密密码，密钥取自 `login.key`，提交 `username` + 加密后的 `password`。
- **账号校验**：`DefaultDBCredentialStore` 校验 DB 账号；配置 LDAP 时 `DBLdapCredentialChecker` 走外部密码库（同时强制 `passwordReadOnly=true`）。
- **失败锁定**：`LoginRetryService` 按用户名/IP 计数，超过 `maxAuthTries` 锁定 15 分钟。
- **图形验证码**：`login.enableCaptcha` 开启，`CaptchaHelper` 生成/校验。
- **远程统一认证**：`app.xml` 的 `remote/cas|ltpa|openid` 节点决定是否挂 `CasPreauthFilter` / `LtpaPreauthFilter` / `OpenidPreauthFilter`；登录页 `displayLoginSwitch` 控制切换开关。
- **登录成功统一出口**：`LoginHelper.forwardService` 对 service 白名单内目标签发 ST 并 302 回跳；同域成员直接回跳，跨域合法 client 追加 `sessionId` 参数。

---

## 4. 会话与票据实现

- **会话建立**：`WebSecurityManager.login(request, response, token)` 写 TGT cookie（`CookieSessionIdPolicy`，EMS 可定制 `DefaultEmsSessionIdPolicy`）。
- **会话存储**：`DBSessionRegistry`（表 `ems.se_session_infoes`），protobuf 序列化会话/账号/Agent/Profile。
- **会话 WS**：`/cas/session/{id}` 查询、`/{principal}/ids` 按用户查会话、`/{id}/expire|access` 过期/更新访问时间。
- **服务票据**：`DefaultTicketRegistry` 生成一次性 ST（Redis 缓存 `ticket_registry`），`serviceValidate` 校验后即 evict。
- **service 白名单**：`CasSetting.clients`（含 `Ems.base` 与 `app.xml` 注册的 `client/base`），`CasService.isValidClient` 校验。
- **注销**：`/cas/logout` 注销 TGT，`LogoutEvent` 触发 `DefaultTicketRegistry.onEvent` 清除该会话的服务缓存。

---

## 5. OAuth2 授权码实现

采用授权码 + 强制 PKCE（RFC 7636）模式，分层实现：

- **`AbstractOAuthService`（ids）** — 通用逻辑：
  - `generateAuthCode`：校验 `code_challenge` 必传，生成 UUID 授权码，JSON 序列化后存 Redis 缓存 `oauth2_code`（TTL = `codeTTL`，默认 5 分钟）；
  - `exchangeCode`：参数校验 → 取码并 **evict**（一次性）→ 过期/客户端不匹配/PKCE S256 校验 → 委托 `onCodeValidated`；
  - `buildAccessToken`：JWT 签发（`user_id`、`client_id`、`scope`、`jti=sessionId`，TTL = `tokenTTL`）；
  - `verifyPkceS256`：SHA-256 + Base64 URL 无填充编码。
- **`OAuthServiceImpl`（ems）** — 实现抽象钩子：
  - `secret`：直接使用 `Ems.key`（`conf.properties` 的 `key`，即 `EmsEnv` 密钥）作为 JWT 签名密钥；
  - `findClient` / `getAuthResources`：按当前 Domain 查 `ThirdPartyApp` / `User` 角色；
  - `onCodeValidated`：`securityManager.login` 建立无 Cookie 会话（`CookieUtils.DisableCookie=true`）、签发 JWT、`OAuthToken` 落库。
- **令牌持久化**：`OAuthToken`（LongId 实体）记录 token/用户/客户端/scope/有效期，`OAuthTokenCleaner` 定时（每 10 分钟）清理过期记录。

**OAuthAction**（ids）提供三个端点：`authorize`（授权页）、`approve`（确认/拒绝，生成授权码回跳）、`token`（换令牌，直接返回 JSON）。`/cas/oauth` 在 `ProtectedAuthorizer` 白名单中整体放行（action 名精确匹配）：`authorize`/`approve` 是浏览器页面流程，未登录由 action 内部跳转 `/cas/login`；`token` 是业务应用**服务端匿名调用**，挂安全拦截会被跳转/401 导致无法换令牌。

---

## 6. 扫码登录实现与集成

### 6.1 ids 侧实现（已完成）

- **`QrcodeRecord`**：Externalizable 序列化，字段含 `qrcodeId`/`secret`/`service`/`name`/`status`/`username`/`authToken`/`deviceIp`/`expireAt`/`createAt`；状态机 `pending → scanned → confirmed → consumed`，`pending/scanned/confirmed(未消费) → cancelled`。
- **`DefaultQrcodeService`**：Redis 缓存 `cas_qrcodes` 存放记录（TTL 180 秒），全程不落库；`create`/`get`（过期自动清除）/`markScanned`/`confirm`（生成一次性 authToken）/`reject`（置 cancelled）/`consume`（匹配后置 consumed）。
- **`QrcodeAction`**：六个端点 `create`/`status`/`scan`/`confirm`/`cancel`/`login`；依赖 `WebSecurityManager`、`TicketRegistry`（构造器）与 `casSetting`、`casService`、`qrcodeService`、`appInfoProvider`、`securityContextBuilder`（字段注入）；写操作 `confirm`/`cancel` 经 `CsrfDefender` 校验。
- **`CasAppInfoProvider`**：确认页应用名提供接口，由部署方实现（ems 用 `EmsCasAppInfoProvider`）。`appInfoProvider` 无法识别 service 时视为**非法应用**，直接渲染错误页，不做兜底；错误页同时携带 `service` 变量展示具体地址。
- **错误页 `error.ftl`**：`QrcodeAction` 在 `scan`/`confirm`/`cancel` 解析不到应用、以及 `login` 的 service 非法等分支，均 `put("service", ...)` 后 `forward("error")`；模板在 `service??` 存在时以 `<code>` 展示请求的服务地址。

### 6.2 ems 集成步骤（已完成）

前置依赖：`beangle-ids` 升级到包含 Qrcode 相关类的版本（当前 `0.4.20-SNAPSHOT`，`project/Build.scala` 中 `beangle_ids` 依赖已指向该版本）。

1. **绑定二维码服务** — `core/cas/TicketModule.scala`：
   ```scala
   bind(classOf[DefaultQrcodeService]).constructor(ref("redis.Factory"))
   ```
2. **绑定扫码 Action** — `cas/action/DefaultModule.scala`：
   ```scala
   bind(classOf[QrcodeAction])
   ```
3. **实现并绑定应用信息提供者** — `EmsCasAppInfoProvider`（`portal/.../core/cas/service/EmsCasAppInfoProvider.scala`），实现 `CasAppInfoProvider`，按本域 `App.base` 前缀匹配 `service`，返回 `App.title`（缺省 `App.name`）、`App.logoUrl`、`App.base` 作为确认页展示信息；在 `core/cas/TicketModule.scala` 一并绑定。
4. **放行匿名端点** — `core/cas/DefaultModule.scala` 的 `ProtectedAuthorizer` 白名单加入 `/cas/qrcode`（放行整个扫码 action，与 `localLogin`、`smsLogin`、`authLogin` 合并）。注意：`ProtectedAuthorizer` 按 **action 名**精确匹配（`MvcRequestConvertor` 生成 `resource=action.name`），不能精确到方法级路径；`scan`/`confirm`/`cancel` 在 action 内部自行校验会话并跳转登录、写操作另有 CSRF 校验，整体放行安全。
5. **提供扫码页面模板** — `portal/src/main/resources/org/beangle/ids/cas/web/action/qrcode/` 下 `scan.ftl`（确认页，含向 `/cas/qrcode/confirm` 的确认 POST 表单与向 `/cas/qrcode/cancel` 的拒绝 POST 表单，CSRF 校验走 `CSRF_TOKEN` cookie）、`confirmed.ftl`、`cancelled.ftl`（拒绝成功提示页）、`error.ftl`。
6. **配置** — `CasSetting` 已含 `qrcodeExpireSeconds`（默认 180）与 `enableQrcodeLogin`（默认 true），一般无需额外配置。

**验证清单**：create 返回四字段 → 手机打开 scanUrl 状态变 `scanned` → 确认后轮询到 `confirmed`+`authToken` → 设备 login 302 回 service?ticket → serviceValidate 通过 → 二次消费 authToken 失败。

---

## 7. 页面与模板定制

### 7.1 模板清单

| 模板（`org/beangle/ids/cas/web/action/` 下） | 端点 |
|------|------|
| `login/index.ftl`、`login/success.ftl` | `/cas/login` |
| `smsLogin/index.ftl`、`smsLogin/success.ftl` | `/cas/sms-login` |
| `oAuth/authorize.ftl`、`oAuth/error.ftl` | `/cas/oauth/authorize` |
| `edit/index.ftl`、`edit/success.ftl` | `/cas/edit` 改密 |
| `qrcode/scan.ftl`、`confirmed.ftl`、`cancelled.ftl`、`error.ftl` | `/cas/qrcode/*`（ems 已提供） |

### 7.2 模板解析规则

模板按 **`HierarchicalTemplateResolver`** 加载：从 Action 类全限定名映射资源路径（FULL_VIEWPATH），并沿类继承链向上查找，因此 ems 侧以相同包路径放置模板即可覆盖 ids 内置模板。

### 7.3 客户自定义页面（template_path 覆盖）

`template_path` 配置（`ContextFreemarkerConfigurator`）是**逗号分隔的模板加载器列表**，`MultiTemplateLoader` 按声明顺序优先命中：

- `class://` — 类路径（默认，含 ids 与 ems 内置模板）；
- `file:///path/to/templates` — 文件系统目录；
- `webapp:///WEB-INF/templates` — Web 应用目录。

部署方在 `template_path` **前面追加** `file://` 路径即可覆盖同名模板（如 `login.ftl`），**无需重编译**：

```xml
<property name="template_path" value="file:///opt/ems/custom-templates,class://"/>
```

自定义模板放置到 `file:///opt/ems/custom-templates/org/beangle/ids/cas/web/action/login/index.ftl` 即覆盖内置登录页。

---

## 8. 配置项（CasSetting）

| 配置键 | 说明 | 默认 |
|--------|------|------|
| `login.enableCaptcha` | 启用图形验证码 | false |
| `login.forceHttps` | 强制 HTTPS | false |
| `login.displayLoginSwitch` | 显示本地/远程登录切换 | false |
| `login.key` | 密码 AES 加密密钥 | Ems.base |
| `login.origin` | CSRF 允许来源 | Ems.base |
| `login.checkPasswordStrength` | 校验密码强度 | true |
| `login.passwordReadOnly` | 密码只读（走远程认证） | false |
| `login.enableSmsLogin` | 启用短信登录 | false |
| `login.remoteLoginUrl` / `login.remoteLogoutUrl` | 远程统一认证地址 | 无 |
| `qrcodeExpireSeconds` | 扫码票据有效期 | 180 |
| `oauth.secret` | OAuth JWT 签名密钥（`OAuthServiceImpl` 直接取 `Ems.key`，即 `conf.properties` 的 `key`） | 无独立配置 |

配置来源：`core/cas/DefaultModule.scala` 从 `app.xml` 的 `config/login` 节点读取（`Config.Provider`），缺省用 `Ems.base`。

---

## 9. 安全实现要点

- **CSRF**：`CsrfDefender` 校验（token + origin），用于登录/扫码确认等写操作。
- **一次性票据**：ST、OAuth 授权码、扫码 `authToken` 均消费即失效。
- **PKCE 强制**：OAuth 流程不接收无 `code_challenge` 的授权请求。
- **service 白名单**：登录回跳目标需在 `clients` 白名单内，防止开放重定向。
- **失败锁定**：密码/短信验证码错误超过阈值锁定 15 分钟。
- **会话隔离**：扫码登录的设备会话独立建立，不携带手机端会话上下文。
- **密钥管理**：OAuth JWT 签名密钥取 `Ems.key`（`conf.properties` 的 `key`，与密码 AES 密钥 `login.key` 相互独立，生产环境需妥善配置）。

---

## 10. 关键代码位置

| 类别 | 路径 |
|------|------|
| CAS 协议 Action | `ids/.../cas/web/action/LoginAction.scala`、`SmsLoginAction.scala`、`LogoutAction.scala`、`AuthAction.scala` |
| OAuth Action | `ids/.../cas/web/action/OAuthAction.scala` |
| 扫码 Action | `ids/.../cas/web/action/QrcodeAction.scala` |
| 协议 WS | `ids/.../cas/web/ws/ServiceValidateAction.scala`、`SessionAction.scala` |
| OAuth 通用服务 | `ids/.../cas/service/AbstractOAuthService.scala`、`OAuthService.scala`、`OAuthCode.scala` |
| 扫码服务 | `ids/.../cas/service/QrcodeService.scala`、`service/impl/DefaultQrcodeService.scala`、`ticket/QrcodeRecord.scala` |
| 应用信息提供者 | `ids/.../cas/service/CasAppInfoProvider.scala`；EMS 实现 `portal/.../core/cas/service/EmsCasAppInfoProvider.scala` |
| 票据 | `ids/.../cas/ticket/TicketRegistry.scala`、`DefaultTicketRegistry.scala`、`TicketCacheService.scala` |
| 配置 | `ids/.../cas/CasSetting.scala` |
| EMS 绑定 | `portal/.../core/cas/DefaultModule.scala`、`TicketModule.scala`、`SeurityModule.scala`、`CredentialModule.scala`、`cas/action/DefaultModule.scala` |
| EMS 实现 | `portal/.../core/security/service/impl/OAuthServiceImpl.scala`、`OAuthTokenCleaner.scala` |
| 账号/凭据 | `portal/.../core/cas/service/DaoAccountStore.scala`、`DefaultDBCredentialStore.scala`、`DBLdapCredentialChecker.scala` |
| 会话策略 | `portal/.../core/cas/service/DefaultEmsSessionIdPolicy.scala`、`ids/.../cas/web/helper/DefaultCasSessionIdPolicy.scala` |
| 页面模板 | `portal/src/main/resources/org/beangle/ids/cas/web/action/**` |
