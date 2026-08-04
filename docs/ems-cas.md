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
| GET | `/cas/qrcode/stream?qrcodeId&secret` | 设备 | SSE 订阅状态（推荐，服务端实时推送） |
| GET | `/cas/qrcode/status?qrcodeId&secret` | 设备 | 查询状态（降级/调试用，单次轮询） |
| GET | `/cas/qrcode/scan?qrcodeId` | 手机 | 扫码确认页（未登录先跳 `/cas/login`） |
| POST | `/cas/qrcode/confirm?qrcodeId` | 手机 | 确认授权，生成一次性 `authToken` |
| POST | `/cas/qrcode/cancel?qrcodeId` | 手机 | 拒绝授权，状态置为 `cancelled` |
| GET | `/cas/qrcode/login?qrcodeId&authToken&service` | 设备 | 换取会话，302 回 `service?ticket=ST` |

`create`、`stream` 与 `status` 均为纯数据接口，**支持跨域（CORS）**：服务端动态回显请求来源 `Origin` 并允许携带凭据，前端可直接跨域调用（无需服务端代理）。

### 5.2 状态机

```
create ──▶ pending ──扫码──▶ scanned ──确认──▶ confirmed ──设备登录──▶ consumed
                  │            │                    │
                  └────── 拒绝 ─┴────── 拒绝 ───────┘ ────────────────▶ cancelled
                                  └────────────── 过期 / 超时 ──────────▶ expired
```

除自然过期外，携带 `lastQrcodeId` 重新 `create` 也会使旧码**立即失效**（`evict`，设备轮询/订阅旧码收到 `expired`），详见 §5.3 刷新。自然过期时记录从缓存消失，同样表现为 `expired`；**设备应基于 `create` 返回的 `expireAt` 本地倒计时主动刷新**，避免走到服务端过期。

### 5.3 创建二维码（设备）

**请求**

```text
POST /cas/qrcode/create
    ?service={service}
    &name={name}
    &lastQrcodeId={qrcodeId}   // 可选
```

| 参数 | 必填 | 说明 |
|------|------|------|
| `service` | 是 | 设备登录成功后的回跳地址，须在 service 白名单内 |
| `name` | 是 | 应用名，须与 EMS 管理端登记的 `App.name` 一致（确认页按此识别应用） |
| `lastQrcodeId` | 否 | 上次创建的二维码标识；传入时旧码立即作废、再生成新码（刷新场景），可避免旧码继续被扫描/轮询 |

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

**刷新（refresh）**

二维码的过期时间由 `create` 返回的 `expireAt`（Unix 秒）决定，默认 180 秒。**设备应基于 `expireAt` 在本地倒计时，到期前主动携带上次的 `qrcodeId` 重新 `create`**（推荐做法，不要等待服务端过期）：

```text
POST /cas/qrcode/create
    ?service={service}
    &name={name}
    &lastQrcodeId={上一张二维码 qrcodeId}
```

- 新码生成前，`lastQrcodeId` 对应的旧码立即作废（`evict`）：手机上尚未扫码的旧二维码随即不可用，避免新旧两码并存导致状态错乱；此时若旧码仍有 SSE 连接，会收到 `expired` 事件（兜底，见 §5.4）；
- 刷新成功后，设备应改用新返回的 `qrcodeId` 与 `secret` 重新订阅或轮询；
- 不携带 `lastQrcodeId` 直接重新 `create` 时，旧码不会立即作废，将保留至自然过期（记录从缓存消失）；
- 设备本地维护"上一张二维码 `qrcodeId`"，即上一次 `create` 成功返回的 `qrcodeId`；设备重启后已无旧码时，首次 `create` 不携带即可。

**对接示例（JavaScript）**

```javascript
let currentQrcode = null; // 当前有效的 { qrcodeId, secret, expireAt }
let refreshTimer = null;  // 到期自动刷新定时器

// 创建二维码；lastQrcodeId 为空时即首次创建
async function createQrcode(service, name, lastQrcodeId) {
  const params = new URLSearchParams({ service, name });
  if (lastQrcodeId) params.set("lastQrcodeId", lastQrcodeId);
  const resp = await fetch(`/cas/qrcode/create?${params}`, { method: "POST", credentials: "include" });
  return resp.json(); // { qrcodeId, secret, expireAt, scanUrl }
}

// 首次创建：不携带 lastQrcodeId
async function createFirst(service, name) {
  currentQrcode = await createQrcode(service, name);
  scheduleRefresh(service, name); // 按 expireAt 本地倒计时
  subscribeStatus(currentQrcode.qrcodeId, currentQrcode.secret, service); // 见 §5.4
}

// 基于 expireAt 本地倒计时，到期前主动刷新（建议提前 5~10 秒），避免依赖服务端过期
function scheduleRefresh(service, name) {
  clearTimeout(refreshTimer);
  const remainMs = currentQrcode.expireAt * 1000 - Date.now() - 10000;
  refreshTimer = setTimeout(() => refreshQrcode(service, name), Math.max(0, remainMs));
}

// 页面刷新/用户点击"刷新二维码"：携带 lastQrcodeId，作废旧码再生成新码
async function refreshQrcode(service, name) {
  const last = currentQrcode ? currentQrcode.qrcodeId : null;
  const next = await createQrcode(service, name, last);
  currentQrcode = next; // 立即切换到新码
  scheduleRefresh(service, name);
  // 用新 qrcodeId/secret 重新订阅或轮询；旧连接会收到 expired 并自行结束
  subscribeStatus(next.qrcodeId, next.secret, service);
}
```

对接要点：

- `lastQrcodeId` 由**设备本地维护**，即上一次 `create` 成功返回的 `qrcodeId`；设备重启后已无旧码时，首次 `create` 不携带即可；
- **推荐**：依据 `create` 返回的 `expireAt` 本地倒计时，在到期前主动刷新二维码（如提前 5~10 秒调用刷新），避免二维码在用户面前过期、也避免依赖服务端推送过期事件；
- 刷新成功后**必须改用新返回的 `qrcodeId`/`secret`** 重新订阅或轮询，旧码已作废不可继续使用；
- 若并发提交了多次刷新，仅最后一次 `create` 返回的码有效（前序刷新会把各自的 `lastQrcodeId` 旧码作废）。

### 5.4 订阅状态（设备，SSE 推荐）

设备创建二维码后，推荐用 **SSE（Server-Sent Events）** 订阅状态：连接建立后由服务端实时推送状态变化，设备无需轮询。

**请求**

```text
GET /cas/qrcode/stream?qrcodeId={qrcodeId}&secret={secret}
```

参数：`qrcodeId`（创建时返回）、`secret`（创建时返回，身份认证），均必填。客户端用标准 `EventSource` 连接（GET + `text/event-stream`），跨域时由服务端回显 `Origin`。

**事件**（`Content-Type: text/event-stream`，每条为命名事件 + JSON data）：

| 事件名 | data | 触发时机 |
|------|------|------|
| `pending` | `{ "status": "pending" }` | 已创建、尚未被扫描 |
| `scanned` | `{ "status": "scanned" }` | 手机已扫码 |
| `confirmed` | `{ "status": "confirmed", "authToken": "..." }` | 手机已确认，可换取会话（服务端随即关闭连接） |
| `cancelled` | `{ "status": "cancelled" }` | 手机已拒绝（服务端随即关闭连接） |
| `expired` | `{ "status": "expired" }` | 记录不存在 / secret 错误 / 旧码被 `lastQrcodeId` 刷新作废（兜底，见下方说明） |

- 收到 `pending`/`scanned` 后连接保持，服务端每 15 秒发送一次注释心跳防止代理/容器断开空闲连接；
- 收到 `confirmed` 后按 §5.6 用 `authToken` 换取会话；收到 `cancelled` 后停止并提示用户；
- **过期不由服务端通知，由设备自行把控**：设备应基于 `create` 返回的 `expireAt` 本地倒计时，到期前主动携带 `lastQrcodeId` 重新 `create`（§5.3），再用新码重新订阅。`expired` 事件仅为兜底：出现异常（如旧码被刷新作废时遗留的 SSE 连接），设备收到后应重新走 `create` + 订阅；
- `EventSource` 断线自动重连；断线重连后服务端会重推当前状态。

**对接示例（JavaScript，浏览器 `EventSource`）**

```javascript
// 1. 创建二维码并订阅（createQrcode 定义见 §5.3 对接示例）
const { qrcodeId, secret } = await createQrcode(service, name);
subscribeStatus(qrcodeId, secret, service);

// 2. SSE 订阅状态，直至 confirmed / cancelled（expired 为兜底）
function subscribeStatus(qrcodeId, secret, service) {
  const es = new EventSource(`/cas/qrcode/stream?qrcodeId=${qrcodeId}&secret=${secret}`);

  // 命名事件用 addEventListener 精确监听
  es.addEventListener("confirmed", (e) => {
    const { authToken } = JSON.parse(e.data);
    es.close(); // 收到终态，客户端主动关闭
    // 3. 用一次性 authToken 换取会话（§5.6），跳转到 service 并附带 ticket
    location.href = `/cas/qrcode/login?qrcodeId=${qrcodeId}&authToken=${authToken}&service=${encodeURIComponent(service)}`;
  });

  es.addEventListener("cancelled", () => {
    es.close();
    // 手机端已拒绝，停止流程并提示用户
  });

  es.addEventListener("expired", () => {
    es.close();
    // 兜底：连接异常（如旧码被 lastQrcodeId 刷新作废）时触发；
    // 正常过期由本地 expireAt 倒计时提前刷新（§5.3），一般不会走到这里
  });

  // EventSource 断线自动重连；如需要可监听 es.onerror 记录日志
}
```

**降级：轮询 `status`**

不适用 SSE 的环境可退化为轮询（保留原端点，单次查询语义）：

```text
GET /cas/qrcode/status?qrcodeId={qrcodeId}&secret={secret}
```

返回 `{ "status": ... }`，状态取值同上述事件表（`confirmed` 时附 `authToken`）；建议每 2~3 秒轮询一次。轮询到 `expired` 时同样为兜底场景，正常情况下应在本地按 `expireAt` 倒计时主动刷新（§5.3）。

### 5.5 扫码确认（手机）

1. 手机打开二维码中的 `scanUrl`；未登录时先跳 `/cas/login`，登录成功回跳确认页；
2. 确认页展示应用名与当前用户；若应用名未在 EMS 管理端登记（`App.name` 不匹配），则视为**非法应用**，直接跳转错误页，错误页会显示该 service 地址，无法进行确认/拒绝；
3. 点击"确认登录"自动提交 `POST /cas/qrcode/confirm`（携带 `qrcodeId`），CAS 生成一次性 `authToken`，状态置为 `confirmed`；
4. 点击"拒绝"提交 `POST /cas/qrcode/cancel`，状态置为 `cancelled`，设备轮询到后停止并提示失败；拒绝仅结束本次授权，**不影响手机端自身登录状态**。

### 5.6 设备登录（换取会话）

订阅到 `confirmed` 后，由**设备的浏览器**发起（须与后续 SSO 会话同一上下文）。

**请求**

```text
GET /cas/qrcode/login?qrcodeId={qrcodeId}&authToken={authToken}&service={service}
```

| 参数 | 必填 | 说明 |
|------|------|------|
| `qrcodeId` | 是 | 创建时返回 |
| `authToken` | 是 | 订阅/轮询到 `confirmed` 时返回 |
| `service` | 是 | 与创建时一致 |

**响应（浏览器层面）**

- 成功：`302 → {service}?ticket=ST-xxx`，设备按 §3.3 校验 ticket 完成 SSO；
- 失败：渲染错误页（authToken 已消费 / 过期 / service 非法），错误页会显示请求的 service 地址，便于排查。

### 5.7 注意事项

- `authToken` **一次性**，消费后立即失效，不能重放；
- `secret` 泄露仅导致登录进度可被窥探，不会泄露会话；但严禁写入二维码或日志；
- 二维码过期（默认 180 秒）以 `create` 返回的 `expireAt` 为准；**建议设备基于 `expireAt` 本地倒计时主动刷新**（提前携带 `lastQrcodeId` 重新 `create`），不要依赖服务端过期推送；
- 通过 `lastQrcodeId` 刷新创建时旧码立即作废，请确保只订阅/轮询最新创建的 `qrcodeId`；
- SSE 连接随二维码终态（`confirmed`/`cancelled`）自动关闭；`expired`（旧码作废等兜底场景）也会关闭。设备端断开后 EventSource 默认自动重连，重连后服务端会重推当前状态；
- 手机拒绝后状态返回 `cancelled`，该二维码不可再登录；拒绝不影响手机端已登录会话；
- 设备登录不携带手机端会话 ID，两端会话互不关联；
- 扫码确认要求用户在 EMS 已登录；
- 扫码 `service` 除需在白名单内，还须在 EMS 管理端登记应用名（按 `App.name` 精确匹配），否则确认页显示**非法应用**、无法授权。

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

**Q4：扫码订阅一直停在 `pending`？**
确认二维码内容渲染的是 `scanUrl` 而非 qrcodeId，且手机端已登录 EMS。

**Q5：SSE 连接意外断开？**
`EventSource` 默认自动重连，重连后服务端会重推当前状态（见 §5.7）；若频繁断开，请确认代理/网关未对 `text/event-stream` 做缓冲或过短的 idle 超时（服务端已每 15 秒发送心跳）。

**Q6：access token 过期后怎么办？**
JWT 默认 1 小时有效，过期后需重新发起授权流程获取新令牌。
