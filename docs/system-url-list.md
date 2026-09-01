# EMS Portal 系统 URL 功能列表

> 基于 `ems/portal/src/main/scala/org/beangle/ems` 下的 Action / WS 类整理。
> URL 规则：beangle webmvc SEO 风格（camelCase → kebab-case），WS 使用 plur-seo 风格（包名复数化）。

## 一、首页与认证

| URL | 方法 | 功能 |
|---|---|---|
| `/` | IndexAction.index() | 系统首页 |
| `/cas` | IndexAction.index() | CAS 登录页 |
| `/cas/edit` | EditAction.index() | CAS 信息编辑 |
| `/cas/edit/save` | EditAction.save() | CAS 信息保存 |
| `/portal/index` | IndexAction.index() | Portal 首页 |
| `/portal/index/logout` | IndexAction.logout() | 退出登录 |

## 二、管理后台 — 用户管理 (`/portal/admin/user`)

| URL | 方法 | 功能 |
|---|---|---|
| `/portal/admin/user` | UserAction.index() | 用户列表 |
| `/portal/admin/user/info` | UserAction.info() | 用户详情 |
| `/portal/admin/user/save` | UserAction.save() | 保存用户 |
| `/portal/admin/user/remove` | UserAction.remove() | 删除用户 |
| `/portal/admin/user/activate` | UserAction.activate() | 启用/禁用用户 |
| `/portal/admin/user/dashboard` | UserAction.dashboard() | 用户仪表盘 |
| `/portal/admin/user/avatar` | AvatarAction.index() | 头像管理 |
| `/portal/admin/user/avatar/info` | AvatarAction.info() | 头像详情 |
| `/portal/admin/user/depart` | DepartAction.index() | 部门管理 |
| `/portal/admin/user/dimension` | DimensionAction.index() | 维度管理 |
| `/portal/admin/user/group` | GroupAction.index() | 用户组管理 |
| `/portal/admin/user/password-config` | PasswordConfigAction.index() | 密码策略 |
| `/portal/admin/user/profile` | ProfileAction.index() | 用户档案管理 |
| `/portal/admin/user/root` | RootAction.index() | 根用户管理 |
| `/portal/admin/user/role/search` | RoleAction.search() | 角色搜索 |
| `/portal/admin/user/role/info` | RoleAction.info() | 角色详情 |
| `/portal/admin/user/role/list` | RoleAction.list() | 角色列表 |
| `/portal/admin/user/role/remove` | RoleAction.remove() | 删除角色 |

## 三、管理后台 — 安全管理 (`/portal/admin/security`)

| URL | 方法 | 功能 |
|---|---|---|
| `/portal/admin/security/dashboard/stat` | DashboardAction.stat() | 安全统计 |
| `/portal/admin/security/func-resource/search` | FuncResourceAction.search() | 功能资源搜索 |
| `/portal/admin/security/func-resource/info` | FuncResourceAction.info() | 功能资源详情 |
| `/portal/admin/security/func-resource/activate` | FuncResourceAction.activate() | 启用/禁用功能资源 |
| `/portal/admin/security/data-resource/info` | DataResourceAction.info() | 数据资源详情 |
| `/portal/admin/security/data-resource/activate` | DataResourceAction.activate() | 启用/禁用数据资源 |
| `/portal/admin/security/menu/search` | MenuAction.search() | 菜单搜索 |
| `/portal/admin/security/menu/info` | MenuAction.info() | 菜单详情 |
| `/portal/admin/security/menu/activate` | MenuAction.activate() | 启用/禁用菜单 |
| `/portal/admin/security/channel/search` | ChannelAction.search() | 渠道搜索 |
| `/portal/admin/security/permission/edit` | PermissionAction.edit() | 权限编辑 |
| `/portal/admin/security/permission/save` | PermissionAction.save() | 保存权限 |
| `/portal/admin/security/data-permission` | DataPermissionAction.index() | 数据权限管理 |

## 四、管理后台 — 配置管理 (`/portal/admin/config`)

| URL | 方法 | 功能 |
|---|---|---|
| `/portal/admin/config/app-group` | AppGroupAction.index() | 应用分组管理 |
| `/portal/admin/config/app/search` | AppAction.search() | 应用搜索 |
| `/portal/admin/config/app/info` | AppAction.info() | 应用详情 |
| `/portal/admin/config/business` | BusinessAction.index() | 业务管理 |
| `/portal/admin/config/credential` | CredentialAction.index() | 凭证管理 |
| `/portal/admin/config/db` | DbAction.index() | 数据库管理 |
| `/portal/admin/config/env` | EnvAction.index() | 环境管理 |
| `/portal/admin/config/file/info` | FileAction.info() | 文件详情 |
| `/portal/admin/config/portalet` | PortaletAction.index() | Portlet 管理 |
| `/portal/admin/config/rule` | RuleAction.index() | 规则管理 |
| `/portal/admin/config/rule-meta` | RuleMetaAction.index() | 规则元数据管理 |
| `/portal/admin/config/text-bundle` | TextBundleAction.index() | 文本包管理 |
| `/portal/admin/config/theme` | ThemeAction.index() | 主题管理 |
| `/portal/admin/config/third-party-app` | ThirdPartyAppAction.index() | 第三方应用管理 |

## 五、管理后台 — OA 办公 (`/portal/admin/oa`)

| URL | 方法 | 功能 |
|---|---|---|
| `/portal/admin/oa/doc` | DocAction.index() | 公文管理 |
| `/portal/admin/oa/flow` | FlowAction.index() | 流程管理 |
| `/portal/admin/oa/flow-process` | FlowProcessAction.index() | 流程配置 |
| `/portal/admin/oa/flow-active-process` | FlowActiveProcessAction.index() | 活跃流程管理 |
| `/portal/admin/oa/message-template` | MessageTemplateAction.index() | 消息模板管理 |
| `/portal/admin/oa/news` | NewsAction.index() | 新闻管理 |
| `/portal/admin/oa/notice` | NoticeAction.index() | 通知管理 |
| `/portal/admin/oa/notice-audit` | NoticeAuditAction.index() | 通知审核 |
| `/portal/admin/oa/notice-audit/search` | NoticeAuditAction.search() | 审核搜索 |
| `/portal/admin/oa/notice-audit/info` | NoticeAuditAction.info() | 审核详情 |
| `/portal/admin/oa/notice-audit/audit` | NoticeAuditAction.audit() | 执行审核 |
| `/portal/admin/oa/signature/search` | SignatureAction.search() | 签章搜索 |
| `/portal/admin/oa/signature/save` | SignatureAction.save() | 保存签章 |
| `/portal/admin/oa/todo` | TodoAction.index() | 待办管理 |

## 六、管理后台 — 其他

| URL | 方法 | 功能 |
|---|---|---|
| `/portal/admin/log/business` | BusinessAction.index() | 业务日志 |
| `/portal/admin/log/error` | ErrorAction.index() | 错误日志 |
| `/portal/admin/job/task` | TaskAction.index() | 定时任务管理 |
| `/portal/admin/job/task/save` | TaskAction.save() | 保存定时任务 |
| `/portal/admin/job/log` | LogAction.index() | 任务日志 |
| `/portal/admin/job/log/info` | LogAction.info() | 任务日志详情 |
| `/portal/admin/blob/meta/info` | MetaAction.info() | 二元元数据详情 |
| `/portal/admin/blob/profile` | ProfileAction.index() | 二元档案管理 |
| `/portal/admin/session/index` | IndexAction.index() | 会话管理 |
| `/portal/admin/session/config` | ConfigAction.index() | 会话配置 |
| `/portal/admin/session/event` | EventAction.index() | 会话事件 |

## 七、用户自助 (`/portal/user`)

| URL | 方法 | 功能 |
|---|---|---|
| `/portal/user/avatar` | AvatarAction.index() | 我的头像 |
| `/portal/user/doc` | DocAction.index() | 我的文档 |
| `/portal/user/doc/info` | DocAction.info() | 文档详情 |
| `/portal/user/message/info` | MessageAction.info() | 消息详情 |
| `/portal/user/notice` | NoticeAction.index() | 我的通知 |
| `/portal/user/notice/info` | NoticeAction.info() | 通知详情 |
| `/portal/user/todo` | TodoAction.index() | 我的待办 |

## 八、RESTful API (`/api/platform`)

### 8.1 用户相关 (`/api/platform/users`)

| URL | 方法 | 功能 |
|---|---|---|
| `/api/platform/users/user` | UserWS.index() | 用户信息 |
| `/api/platform/users/account` | AccountWS.index() | 账户信息 |
| `/api/platform/users/app` | AppWS.index() | 用户应用列表 |
| `/api/platform/users/avatar/info` | AvatarWS.info() | 用户头像 |
| `/api/platform/users/credential` | CredentialWS.index() | 用户凭证 |
| `/api/platform/users/dimension` | DimensionWS.index() | 用户维度 |
| `/api/platform/users/profile` | ProfileWS.index() | 用户档案 |
| `/api/platform/users/root` | RootWS.index() | 根用户信息 |

### 8.2 安全相关 (`/api/platform/securities`)

| URL | 方法 | 功能 |
|---|---|---|
| `/api/platform/securities/funcs/menu` | MenuWS.index() | 功能菜单 |
| `/api/platform/securities/funcs/resource` | ResourceWS.index() | 功能资源列表 |
| `/api/platform/securities/funcs/resource/info` | ResourceWS.info() | 功能资源详情 |
| `/api/platform/securities/funcs/permission` | PermissionWS.index() | 功能权限 |
| `/api/platform/securities/datas/resource/info` | ResourceWS.info() | 数据资源详情 |
| `/api/platform/securities/datas/permission` | PermissionWS.index() | 数据权限 |

### 8.3 配置相关 (`/api/platform/configs`)

| URL | 方法 | 功能 |
|---|---|---|
| `/api/platform/configs/datasource` | DatasourceWS.index() | 数据源配置 |
| `/api/platform/configs/domain` | DomainWS.index() | 域配置 |
| `/api/platform/configs/file` | FileWS.index() | 文件列表 |
| `/api/platform/configs/file/info` | FileWS.info() | 文件详情 |
| `/api/platform/configs/org` | OrgWS.index() | 组织配置 |
| `/api/platform/configs/redis` | RedisWS.index() | Redis 配置 |
| `/api/platform/configs/rule/list` | RuleWS.list() | 规则列表 |
| `/api/platform/configs/textbundle/list` | TextBundleWS.list() | 文本包列表 |
| `/api/platform/configs/theme` | ThemeWS.index() | 主题配置 |

### 8.4 OA 相关 (`/api/platform/oas`)

| URL | 方法 | 功能 |
|---|---|---|
| `/api/platform/oas/doc/list` | DocWS.list() | 公文列表 |
| `/api/platform/oas/doc/info` | DocWS.info() | 公文详情 |
| `/api/platform/oas/flow/info` | FlowWS.info() | 流程详情 |
| `/api/platform/oas/notice/list` | NoticeWS.list() | 通知列表 |
| `/api/platform/oas/notice/info` | NoticeWS.info() | 通知详情 |
| `/api/platform/oas/signature/info` | SignatureWS.info() | 签章详情 |
| `/api/platform/oas/sms` | SmsWS.index() | 短信服务 |

### 8.5 其他 API

| URL | 方法 | 功能 |
|---|---|---|
| `/api/platform/blobs/file` | FileWS.index() | 二元文件 |
| `/api/platform/logs/list` | ListWS.index() | 日志列表 |
| `/api/platform/logs/push` | PushWS.index() | 日志推送 |
| `/api/platform/oauths/login` | LoginWS.index() | OAuth 登录 |
