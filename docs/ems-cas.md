# EMS 统一认证（CAS）接入指南

本文面向业务应用、第三方系统及独立设备的开发人员，介绍 EMS 统一认证的接入接口、请求格式、响应格式与用法。所有请求以 CAS 部署地址为前缀（如 `https://cas.example.com`）。

> 内部实现（模块划分、代码位置、页面定制）请参阅 [ems-cas-impl.md](ems-cas-impl.md)。

---

## 1. 接入方式总览

EMS 提供三种认证接入方式，按应用形态选择：

| 方式 | 适用场景 | 用户认证 | 交付物 |
|------|----------|----------|--------|
| **CAS 单点登录** | 传统 Web 应用（服务端整页跳转） | EMS 托管登录页（密码 / 短信） | service ticket + 用户身份 XML |
| **OAuth2 授权码** | 独立应用 / API 服务，需获取访问令牌 | EMS 托管授权页（勾选授权范围） | JWT access token |
| **扫码登录** | 独立设备（另一应用 / 浏览器）登录 | 手机端扫码确认（已登录用户） | 设备独立会话 + service ticket |

登录成功后，浏览器/设备在 EMS 域内建立**统一会话（SSO）**：访问任一已接入系统无需重复登录；会话以 cookie 绑定 CAS 域，跨系统共享。

---

## 2. 接入准备

- **注册应用**：在 EMS 管理端登记应用。OAuth2 方式需取得 **client_id** 与 **redirect_uri**；CAS 方式需登记登录回跳的 **service 地址**。
- **service 白名单**：所有登录回跳地址必须在 EMS 的 service 白名单内。未登记的回跳目标会被拒绝（`Invalid Client`）。
- **HTTPS**：生产环境要求通过 HTTPS 访问 CAS，保证票据与会话安全。

---

## 3. CAS 单点登录（页面跳转）

适用于服务端渲染的 Web 应用。整个流程由浏览器整页跳转完成，应用负责接收并校验 service ticket。

### 3.1 登录流程

```mermaid
sequenceDiagram
  participant U as 用户浏览器
  participant App as 业务应用
  participant CAS as EMS CAS
  U->>App: 访问受保护资源
  App-->>U: 302 → /cas/login?service={应用回跳地址}
  U->>CAS: GET /cas/login?service=...
  CAS-->>U: 渲染登录页（已登录则直接回跳）
  U->>CAS: 提交账号密码 / 短信验证码
  CAS-->>U: 302 → {service}?ticket=ST-xxx
  U->>App: 携带 ticket 访问 service
  App->>CAS: GET /cas/serviceValidate?service=...&ticket=...
  CAS-->>App: XML（用户身份 / 失败）
  App-->>U: 建立本地会话，放行
```

### 3.2 登录跳转接口

**请求**

```text
GET /cas/login?service={service}
```

| 参数 | 必填 | 说明 |
|------|------|------|
| `service` | 是 | 登录成功后回跳的应用地址，须在 service 白名单内；不带时仅完成登录、不回跳业务系统 |

**响应（浏览器层面）**

- 未登录：渲染登录页（登录表单由 EMS 托管，用户名/密码由 EMS 页面前端加密提交，应用无需处理）；
- 已登录：`302 → {service}`。回跳地址**不一定带 `ticket`**：
  - service 尚未进入共享会话（跨域应用首次访问的典型场景）→ 回跳地址追加 `ticket=ST-{uuid}`；
  - 会话已共享（同域应用等）→ 直接回跳，不带 ticket。

应用端处理：回跳带 `ticket` 时按 §3.3 校验；不带 `ticket` 说明本次登录已通过会话共享完成，无需再校验。

### 3.3 票据校验接口（serviceValidate）

应用收到 `ticket` 参数后，在**服务端**调用此接口校验。

**请求**

```text
GET /cas/serviceValidate?service={service}&ticket={ticket}
```

| 参数 | 必填 | 说明 |
|------|------|------|
| `service` | 是 | 必须与登录跳转时的值完全一致（含 URL 编码形式） |
| `ticket` | 是 | 登录回跳携带的 service ticket |

curl 示例：

```bash
curl "https://cas.example.com/cas/serviceValidate?service=https%3A%2F%2Fapp.example.com%2Fhome&ticket=ST-8a2b..."
```

**成功响应**（HTTP 200，`Content-Type: text/xml; charset=utf-8`）

```xml
<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>
  <cas:authenticationSuccess>
    <cas:user>zhangsan</cas:user>
    <cas:attributes>
      <cas:userName>张三</cas:userName>
      <cas:authorities>admin,teacher</cas:authorities>
    </cas:attributes>
  </cas:authenticationSuccess>
</cas:serviceResponse>
```

| 节点 | 说明 |
|------|------|
| `cas:user` | 用户登录名（唯一标识，如工号/学号） |
| `cas:attributes/cas:userName` | 用户显示名 |
| `cas:attributes/cas:authorities` | 用户角色，逗号分隔 |
| `cas:attributes/cas:permissions` | 用户权限码，逗号分隔 |

**失败响应**（HTTP 200，`cas:authenticationFailure`）

```xml
<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>
  <cas:authenticationFailure code='1'>Cannot find ticket</cas:authenticationFailure>
</cas:serviceResponse>
```

**错误码表**

| `code` | 含义 |
|--------|------|
| `-1` | 缺少 `ticket` 或 `service` 参数 |
| `1` | 找不到票据（不存在、已消费或已过期） |
| `2` | `service` 与签发时的地址不匹配 |
| `403` | `service` 不在白名单内（Invalid Client） |

**应用侧用法示例**

```python
# Python 示例：校验 ticket 后建立本地会话
import requests, xml.etree.ElementTree as ET

ticket = request.args.get("ticket")   # 登录回跳携带
resp = requests.get(f"https://cas.example.com/cas/serviceValidate",
                    params={"service": service_url, "ticket": ticket})
root = ET.fromstring(resp.text)
success = root.find(".//{http://www.yale.edu/tp/cas}authenticationSuccess")
if success is not None:
    username = success.find("{http://www.yale.edu/tp/cas}user").text
    session["user"] = username            # 建立本地会话
else:
    abort(401)                            # 校验失败，拒绝访问
```

要点：

- **ticket 一次性**：校验成功后立即失效，不可重复使用；
- 生产环境务必在服务端校验，不要信任浏览器传来的身份信息；
- 校验失败时不建立本地会话。

### 3.4 注销接口

```text
GET /cas/logout?service={service}
```

结束 EMS 域内的统一会话。可选参数 `service`：注销完成后 302 回跳到该地址。业务应用如需感知注销，可在登录时登记单点登出（SLO）回调。

### 3.5 前端会话检测接口（JSON）

前端应用（SPA / 移动端）通过 `/cas/auth/login` 检测当前浏览器是否已登录，支持跨域（CORS）；登出调用 `/cas/auth/logout`。

**请求**

```text
GET /cas/auth/login
```

**响应**

- 已登录（HTTP 200）：

```json
{
  "authenticated": true,
  "token": "a1b2c3d4e5f6...",           // 当前 CAS 会话 ID
  "user": { "code": "zhangsan", "name": "张三" }
}
```

- 未登录（HTTP 401）：

```json
{
  "authenticated": false,
  "error": "not_authenticated",
  "redirectUrl": "https://cas.example.com/cas/login?service=https%3A%2F%2Fapp.example.com"
}
```

| 字段 | 说明 |
|------|------|
| `authenticated` | 是否已登录 |
| `token` | 已登录时的会话 ID，可用于后续会话管理接口 |
| `user.code` / `user.name` | 用户登录名 / 显示名 |
| `redirectUrl` | 未登录时的登录跳转地址（已携带原 service） |

**用法**：前端页面加载时调用，未登录（401）则 `window.location` 跳转 `redirectUrl`；已登录则展示用户信息。

---

## 4. OAuth2 授权码流程（PKCE）

适用于独立应用 / API 服务。应用获取 JWT 访问令牌后，以 `Authorization: Bearer <token>` 访问受保护资源。**强制要求 PKCE（RFC 7636）**。

### 4.1 端点

| 方法 | 路径 | 调用方 | 说明 |
|------|------|--------|------|
| GET | `/cas/oauth/authorize` | 应用（浏览器跳转） | 引导用户打开授权页 |
| POST | `/cas/oauth/approve` | 浏览器自动提交 | 用户在授权页确认/拒绝 |
| POST | `/cas/oauth/token` | 应用（服务端） | 用授权码 + code_verifier 换 access token |

### 4.2 前置条件：生成 PKCE 参数

每次授权请求前生成一组参数：

- `code_verifier`：43~128 位随机串，字符集 `A-Z a-z 0-9 . _ ~`；
- `code_challenge`：`base64urlencode(SHA256(code_verifier))`（S256，仅支持 S256）。

```python
# Python 示例
import base64, hashlib, secrets, string

verifier = "".join(secrets.choice(string.ascii_letters + string.digits + "._~")
                   for _ in range(64))
challenge = base64.urlsafe_b64encode(hashlib.sha256(verifier.encode()).digest()).rstrip(b"=").decode()
```

### 4.3 授权接口（authorize）

**请求**

```text
GET /cas/oauth/authorize
    ?client_id={client_id}
    &code_challenge={code_challenge}
    &redirect_uri={redirect_uri}
    &scope={scope}
    &state={state}
```

| 参数 | 必填 | 说明 |
|------|------|------|
| `client_id` | 是 | 应用注册时取得的客户端编码 |
| `code_challenge` | 是 | PKCE challenge（S256） |
| `redirect_uri` | 否 | 授权回调地址，缺省用注册值；传值时必须以注册值为前缀 |
| `scope` | 否 | 申请的授权范围（对应 EMS 角色 ID），多个用空格分隔；实际授予范围以授权页勾选为准 |
| `state` | 否 | 防 CSRF 的会话状态，原样回传，建议必传 |

**响应（浏览器层面）**

- 用户未登录 → 先跳转 `/cas/login`，登录成功回跳授权页；
- 用户已登录 → 渲染授权页（展示应用名与待授权范围），由用户勾选后点"授权"/"拒绝"；
- 参数非法（`client_id` 未注册、缺少 `code_challenge`、`redirect_uri` 不匹配）→ 渲染错误页，不进入授权页。

**授权回调**

授权结果由浏览器 302 回跳到 `redirect_uri` 并携带参数：

- 授权成功：`{redirect_uri}?code={授权码}&state={state}`；
- 授权失败：`{redirect_uri}?error={access_denied|invalid_client|invalid_request}&state={state}`；
- 无有效 `redirect_uri` 时渲染错误页。

`state` 原样回传，应用应校验其与发起时一致，防止 CSRF。

### 4.4 令牌接口（token）

应用在收到授权回调后，在**服务端**换取令牌。

**请求**

```text
POST /cas/oauth/token
Content-Type: application/x-www-form-urlencoded

client_id={client_id}&code={授权码}&code_verifier={code_verifier}
```

| 参数 | 必填 | 说明 |
|------|------|------|
| `client_id` | 是 | 与授权时一致 |
| `code` | 是 | 授权回调收到的授权码 |
| `code_verifier` | 是 | 与授权时的 `code_challenge` 对应 |

curl 示例：

```bash
curl -X POST "https://cas.example.com/cas/oauth/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=myapp&code=9f8e7d6c&code_verifier=..."
```

**成功响应**（HTTP 200）

```json
{ "access_token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiemhhbmdzYW4i..." }
```

**失败响应**（HTTP 400）

```json
{ "error": "invalid_grant", "error_description": "PKCE verification failed" }
```

**错误码表（token 端点返回，HTTP 400 + JSON）**

| `error` | 含义 |
|---------|------|
| `invalid_request` | 缺少必填参数（client_id/code/code_verifier） |
| `invalid_grant` | 授权码无效、过期、已消费，或 code_verifier 校验失败 |

授权/回跳阶段的失败不在此端点返回：`invalid_client`、`access_denied` 等由 §4.3 授权回调携带 `?error=` 参数回跳。

### 4.5 使用访问令牌

`access_token` 为 JWT（默认 1 小时），载荷含 `user_id`（用户登录名）、`client_id`、`scope`、`jti`（CAS 会话 ID）。请求受保护资源时携带：

```bash
curl "https://api.example.com/resource" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

- 应用需自行校验 JWT 签名（密钥由 EMS 提供）或调用资源服务确认；
- 令牌过期后重新走授权流程。

### 4.6 注意事项

- 授权码**一次性**且 5 分钟有效，验证后立即失效，不能重放；
- `redirect_uri` 传值必须以注册值为前缀（注册值是传值的前缀），否则拒绝回调；
- `code_verifier` 必须与发起授权时的 `code_challenge` 对应，否则换取失败；
- PKCE 是强制的：不带 `code_challenge` 的授权请求直接报错；
- 授权流程要求用户在 EMS 已登录。

---

## 5. 扫码登录（独立设备）

适用于**独立设备**（另一台应用、浏览器或客户端）登录 EMS。由已登录用户用手机扫描设备二维码并确认授权；设备凭一次性授权码向 CAS 换取**自己的会话**，全程不传递手机端会话 ID，两端会话互不关联。

```mermaid
sequenceDiagram
  participant Dev as 设备（待登录）
  participant M as 手机（已登录）
  participant CAS as EMS CAS
  Dev->>CAS: POST /cas/qrcode/create?service
  CAS-->>Dev: {qrcodeId, secret, expireAt, scanUrl}
  Dev-->>M: 展示二维码(scanUrl)
  M->>CAS: GET scanUrl → 确认页
  M->>CAS: POST /cas/qrcode/confirm
  CAS-->>Dev: 轮询到 confirmed + authToken
  Dev->>CAS: GET /cas/qrcode/login?authToken&service
  CAS-->>Dev: 302 → {service}?ticket=ST-xxx
  Dev->>CAS: serviceValidate 校验
```

### 5.1 端点

| 方法 | 路径 | 调用方 | 说明 |
|------|------|--------|------|
| POST | `/cas/qrcode/create?service&name` | 设备 | 创建二维码票据 |
| GET | `/cas/qrcode/status?qrcodeId&secret` | 设备 | 轮询状态 |
| GET | `/cas/qrcode/scan?qrcodeId` | 手机 | 扫码确认页（未登录先跳 `/cas/login`） |
| POST | `/cas/qrcode/confirm?qrcodeId` | 手机 | 确认授权，生成一次性 `authToken` |
| POST | `/cas/qrcode/cancel?qrcodeId` | 手机 | 拒绝授权，状态置为 `cancelled` |
| GET | `/cas/qrcode/login?qrcodeId&authToken&service` | 设备 | 换取会话，302 回 `service?ticket=ST` |

### 5.2 状态机

```
create ──▶ pending ──扫码──▶ scanned ──确认──▶ confirmed ──设备登录──▶ consumed
                  │            │                    │
                  └────── 拒绝 ─┴────── 拒绝 ───────┘ ────────────────▶ cancelled
                                  └────────────── 过期 / 超时 ──────────▶ expired
```

### 5.3 创建二维码（设备）

**请求**

```text
POST /cas/qrcode/create
    ?service={service}
    &name={name}
```

| 参数 | 必填 | 说明 |
|------|------|------|
| `service` | 是 | 设备登录成功后的回跳地址，须在 service 白名单内 |
| `name` | 否 | 应用名（预留字段，当前展示以 EMS 登记信息为准） |

**成功响应**（HTTP 200）

```json
{
  "qrcodeId": "x7Q2mP9kLw5...",          // 二维码标识
  "secret": "S3cr3tK3y...",              // 轮询密钥（仅设备保存）
  "expireAt": 1722758400,                // 过期时间（Unix 秒）
  "scanUrl": "https://cas.example.com/cas/qrcode/scan?qrcodeId=x7Q2mP9kLw5..."
}
```

**失败响应**（HTTP 400）

```json
{ "error": "invalid_service" }
```

| 字段 | 说明 |
|------|------|
| `qrcodeId` | 后续轮询/确认/登录均以此为标识 |
| `secret` | 轮询身份认证密钥，**不得**出现在二维码或日志中 |
| `scanUrl` | 二维码渲染内容：手机扫开后直接进入确认页 |

### 5.4 轮询状态（设备）

**请求**

```text
GET /cas/qrcode/status?qrcodeId={qrcodeId}&secret={secret}
```

| 参数 | 必填 | 说明 |
|------|------|------|
| `qrcodeId` | 是 | 创建时返回 |
| `secret` | 是 | 创建时返回，身份认证 |

**响应**（HTTP 200）

| 状态 | 响应体 | 说明 |
|------|--------|------|
| `pending` | `{ "status": "pending" }` | 尚未被扫描 |
| `scanned` | `{ "status": "scanned" }` | 手机已扫码 |
| `confirmed` | `{ "status": "confirmed", "authToken": "..." }` | 手机已确认，可换取会话 |
| `cancelled` | `{ "status": "cancelled" }` | 手机已拒绝，不可再登录 |
| `expired` | `{ "status": "expired" }` | 超时/不存在/secret 错误 |

- 建议每 2~3 秒轮询一次；
- 票据默认有效期 180 秒，超时返回 `expired`，需重新创建。

### 5.5 扫码确认（手机）

1. 手机打开二维码中的 `scanUrl`；未登录时先跳 `/cas/login`，登录成功回跳确认页；
2. 确认页展示应用名与当前用户；若 service 未在 EMS 管理端登记（`App.base` 前缀不匹配），则视为**非法应用**，直接跳转错误页，错误页会显示该 service 地址，无法进行确认/拒绝；
3. 点击"确认登录"自动提交 `POST /cas/qrcode/confirm`（携带 `qrcodeId`），CAS 生成一次性 `authToken`，状态置为 `confirmed`；
4. 点击"拒绝"提交 `POST /cas/qrcode/cancel`，状态置为 `cancelled`，设备轮询到后停止并提示失败；拒绝仅结束本次授权，**不影响手机端自身登录状态**。

### 5.6 设备登录（换取会话）

轮询到 `confirmed` 后，由**设备的浏览器**发起（须与后续 SSO 会话同一上下文）。

**请求**

```text
GET /cas/qrcode/login?qrcodeId={qrcodeId}&authToken={authToken}&service={service}
```

| 参数 | 必填 | 说明 |
|------|------|------|
| `qrcodeId` | 是 | 创建时返回 |
| `authToken` | 是 | 状态轮询到 `confirmed` 时返回 |
| `service` | 是 | 与创建时一致 |

**响应（浏览器层面）**

- 成功：`302 → {service}?ticket=ST-xxx`，设备按 §3.3 校验 ticket 完成 SSO；
- 失败：渲染错误页（authToken 已消费 / 过期 / service 非法），错误页会显示请求的 service 地址，便于排查。

### 5.7 注意事项

- `authToken` **一次性**，消费后立即失效，不能重放；
- `secret` 泄露仅导致登录进度可被窥探，不会泄露会话；但严禁写入二维码或日志；
- `qrcodeId` 超时后状态返回 `expired`，需重新创建；
- 手机拒绝后状态返回 `cancelled`，该二维码不可再登录；拒绝不影响手机端已登录会话；
- 设备登录不携带手机端会话 ID，两端会话互不关联；
- 扫码确认要求用户在 EMS 已登录；
- 扫码 `service` 除需在白名单内，还须在 EMS 管理端登记（按应用注册的 `base` 前缀匹配），否则确认页显示**非法应用**、无法授权。

---

## 6. 安全约定

对接方应遵守以下安全约定：

- **票据一次性**：ST、OAuth 授权码、扫码 `authToken` 均为一次性，消费后立即失效，禁止重放；
- **服务端校验**：身份信息（ticket、token）一律在服务端向 CAS 校验，不得信任客户端提交的身份数据；
- **PKCE 强制**：OAuth2 必须使用 PKCE S256，不带 `code_challenge` 的授权请求会被拒绝；
- **service 白名单**：所有登录回跳地址须提前登记，未登记的回跳目标会被拒绝；
- **HTTPS**：生产环境通过 HTTPS 访问 CAS，避免票据与令牌在传输中被截获；
- **会话隔离**：扫码登录的设备会话与手机端会话完全独立，互不持有对方的会话上下文。

---

## 附录：常见问题

**Q1：登录回跳后 `ticket` 校验失败？**
确认 `service` 参数与登录跳转时完全一致（含编码），且 ticket 未被重复使用。

**Q2：`/cas/serviceValidate` 返回 `code=403`？**
回跳地址未在 EMS service 白名单内，请在 EMS 管理端登记。

**Q3：OAuth 授权页报缺少 `code_challenge`？**
PKCE 为强制要求，授权请求必须携带 `code_challenge`（S256）。

**Q4：扫码轮询一直停在 `pending`？**
确认二维码内容渲染的是 `scanUrl` 而非 qrcodeId，且手机端已登录 EMS。

**Q5：access token 过期后怎么办？**
JWT 默认 1 小时有效，过期后需重新发起授权流程获取新令牌。
