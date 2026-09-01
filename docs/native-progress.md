# EMS Native-Image 工作进展

> 最后更新：2026-09-01

## 一、当前状态

| 项目 | 状态 | 说明 |
|---|---|---|
| Native image 构建 | ✅ 成功 | 218MB，4m37s |
| Tomcat 启动 | ✅ 正常 | 端口 8082，0.317s |
| Spring 容器 | ✅ 正常 | 225 beans，285ms |
| Hibernate | ✅ 正常 | 连接 PostgreSQL |
| HikariCP | ✅ 正常 | 连接池工作 |
| Action 扫描 | ✅ 正常 | 94 actions，537 mappings |
| 登录页面 | ✅ 正常 | HTTP 302 → 200 |
| HTTP 客户端调用 | ✅ 正常 | `--initialize-at-run-time=java.net.http` 后内部调用成功 |
| 菜单 JSON 序列化 | ✅ 已修复 | `DomainMenus` 等注册反射后菜单完整，见 3.1 |

## 二、已完成工作

### 2.1 反射配置注册（AOT Hints）

| 项目 | 文件 | 注册类数 | 状态 |
|---|---|---|---|
| commons | `BuiltinAotHints.scala` | 45 | ✅ |
| bui | `BuiAotHints.scala` | 53 | ✅ |
| cdi | `SpringAotHints.scala` | 已有 | ✅ |
| cache | `CacheAotHints.scala` | 2 + Caffeine jar扫描 | ✅ |
| data/model | `ModelAotHints.scala` | 18 | ✅ |
| data/hibernate | `BeangleAotHints.scala` | 已有 | ✅ |
| webmvc | `WebmvcAotHints.scala` | 81 | ✅ |
| template | `TemplateAotHints.scala` | 14 | ✅ |
| security | `SecurityAotHints.scala` | ~20 | ✅ |
| serializer | `SerializerAotHints.scala` | 10 | ✅ |
| ids | `IdsAotHints.scala` | 12 | ✅ |
| she | `SheAotHints.scala` | 6 | ✅ |
| web | `WebAotHints.scala` | 9 | ✅ |
| event | `EventAotHints.scala` | 5 | ✅ |
| ems | 4 个 registrar 按模块放置（app/portal/native，见 2.5） | 78 | ✅ |

### 2.2 静态 JSON 配置

| 文件 | 条目数 | 说明 |
|---|---|---|
| `hibernate/reflect-config.json` | 372 | Hibernate 核心 + 43 新增 |
| `sas/engine/reflect-config.json` | 45 | Tomcat/Coyote 类 |
| `commons/.../scala/reflect-config.json` | 34 | Scala 集合/反射类 |
| `ems/native/reflect-config.json` | 3 | Scala MapWrapper |

### 2.3 代码改动

| 文件 | 改动 |
|---|---|
| `ems/build.sbt` | `--enable-url-protocols=jar,resource,http,https` |
| `data/ConfigurationBuilder.scala` | `hibernate.jpa.static_metamodel.population=disabled` |
| `template/BeangleObjectWrapper.scala` | `FriendlyMapModel` 改写，绕过 `ClassIntrospector` |
| `template/FriendlyMapModel.scala` | 新文件，直接实现 `TemplateHashModelEx` |
| `commons/BuiltinAotHints.scala` | 新增 45 个 commons 类 + logback 4 个类 |
| `cache/CacheAotHints.scala` | 新文件，Caffeine jar 扫描 + jcache 注册 |
| `app/.../org/beangle/ems/app/aot/EmsAppMetaRegistrar.scala` | 新增：BeanMeta 固化进 beanmeta.idx（Flows DTO/模板模型/tag 组件，随 app 模块发布） |
| `portal/.../org/beangle/ems/core/aot/EmsCoreMetaRegistrar.scala` | 新增：BeanMeta 固化进 beanmeta.idx（菜单 DTO/会话日志模型，随 portal 模块发布） |
| `app`/`portal` 的 `META-INF/beangle/meta-registrars.txt` | 新增：声明各模块 MetaRegistrar（MetaPlugin 生成 idx，AotPlugin 同时收集反射提示） |

### 2.4 收敛清单

| 指标 | 数量 |
|---|---|
| agent 采集总量 | 731 |
| 已注册（reflect-config） | 832 |
| 已注册（AOT hints） | 435 |
| 剩余未注册 | 494（大部分已被 AOT hints 覆盖，正则假阴性） |

### 2.5 反射注册按模块放置（ems 应用侧）

> 各 registrar 放在"被注册类所在包"对应模块：app 模块注册 `org.beangle.ems.app.*`，
> portal 模块注册 `org.beangle.ems.core.*`/`org.beangle.ems.portal.*`（两包源码均在
> portal 模块），第三方基础设施 `EmsInfraAotHints` 无对应仓库模块、保留在 native
> 模块；`org.beangle.security.*` 的注册（`SecurityAotHints`）随 beangle-security 库
> 发布，native 的 `aot-registrars.txt` 不再声明。
>
> **BeanMeta 固化（beanmeta.idx）**：会走 `BeanInfos.get` 的模板模型/DTO/tag 组件
> （Flows case class、菜单 DTO、SessionInfo/AppLogEntry、AvatarTag/UserTag、NavContext
> 等）由各模块 `MetaRegistrar` 子类（`EmsAppMetaRegistrar`/`EmsCoreMetaRegistrar`，
> 经 `src/main/resources/META-INF/beangle/meta-registrars.txt` 声明）在构建期用
> `register(classOf[...])`（inline 宏静态 dig）写入 `beanmeta.idx` 随 jar 发布。
> 运行时 `BeanInfos.get` 命中索引后只经 `getMethods`/`getConstructors` 重建访问器，
> 因此反射注册只需默认策略（public 方法 + public 构造器）——native 下
> `getDeclaredMethods` 依赖 `allDeclaredMethods` 注册，仅注册 public 方法时取不到，
> 这正是 `MetaLoader.load` 回退路径不可用的原因，也是 DTO 不再注册 declared 成员
> （不再需要 fullPolicy）的原因。`MetaPlugin` 生成 idx、`AotPlugin` 同时读取
> `meta-registrars.txt` 收集反射提示，两插件均按模块独立产出、随 jar 发布、native
> 构建时自动合并。公共策略与枚举注册助手在基类 `EmsAotSupport`（app 模块
> `org.beangle.ems.app.aot`，portal/native 的 registrar 经模块依赖引用）；Scala 3
> 枚举直接用库侧 `AotHints.registerEnum(classOf[...])`（注册枚举类/伴生对象/全部
> 值类并同步序列化登记）。

| registrar | 所在模块 | 对应包 | 注册内容 | 策略 |
|---|---|---|---|---|
| `EmsAppMetaRegistrar` | app（`org.beangle.ems.app.aot`） | `org.beangle.ems.app.*` | BeanMeta（18 类）：模板模型 `NavContext`/`App`/`Ems$`/`Ems$Org`/`Ems$Domain`/`Ems$Theme`；标签库 `EmsModels`/`AvatarTag`/`UserTag`；工作流 DTO `Flows$Flow/Activity/User/Group/Task/Process/Comment/Attachment/Payload`；枚举 `app.log.Level` | BeanMeta 入 idx；反射默认策略（public） |
| `EmsCoreMetaRegistrar` | portal（`org.beangle.ems.core.aot`） | `org.beangle.ems.core.*` | BeanMeta（5 类）：菜单 DTO `DomainMenus`/`GroupMenus`/`AppMenus`、会话/日志模型 `SessionInfo`/`AppLogEntry`；`LogDbAppender`（`list()` 非宏绑定，不在 idx）与枚举 `EmbedMode`/`FlowStatus`/`NoticeStatus` 反射注册 | BeanMeta 入 idx；反射默认策略（public） |
| `EmsPortalAotHints` | portal（`org.beangle.ems.portal.aot`） | `org.beangle.ems.portal.*` | portal 辅助类 `DomainSupport` | service |
| security 库 `SecurityAotHints` | security（`org.beangle.security.aot`） | `org.beangle.security.*` | 账户/档案 `DefaultAccount`/`Profile`；枚举 `EventType`/`Scope`（随 beangle-security jar 发布） | default；枚举 enum |
| `EmsInfraAotHints` | native | 第三方/基础设施 | caffeine jcache + jar 全量扫描；`RedisClientFactory` + Jedis/pool2；FreeMarker 支撑（`SinglePage`/`RequestFacade`/`ResponseFacade`/`HashAdapter$1$1$1`）；Hibernate 查询缓存序列化 + `java.time.Ser` + 基础序列化类型；资源 pattern（beanmeta.idx/jdbc keywords/jedis pom/typesafe 配置） | caffeine declared；jedis query-public；枚举 enum |

## 三、已知问题

### 3.1 菜单接口 JSON 序列化输出空对象（✅ 已修复）

**现象**：页面菜单为空。`RemoteService.getDomainMenusJson` 内部调用**成功**返回
`"{}"`（页面 `emsShell.createNav(app,portal,{},params,false)`），即菜单接口
`MenuWS.user` 的 JSON 序列化把 `DomainMenus` 写成了空对象。

**根因**：`DomainMenus`/`GroupMenus`/`AppMenus`（`MenuService.scala` 中的 case class）
既不在 `beanmeta.idx`（已核对 portal 的 idx，244 条无此类），也未注册 native
反射元数据。序列化走 `BeanInfos.get` → `MetaModels.get`（None）→ `MetaLoader.load`
运行时反射；native 下只注册 public 方法时 `getDeclaredMethods` 仍然取不到
（GraalVM 的 `allPublicMethods` 不支撑 declared 查询，declared 成员需显式注册
`allDeclaredMethods`/`allDeclaredFields`），`BeanMarshaller` 写出 `{}`
（JVM 反射可用，输出正常 JSON）。

**佐证**：
- `curl .../menus/user/imroot.json?forDomain=1` → `{}`（空对象）
- `curl .../menus/user/imroot`（无 .json 后缀）→ 完整 `DomainMenus` toString（数据在）
- `curl .../domains.json` / `themes.json` 正常 → 实体类在 beanmeta.idx，序列化 OK
- JVM 独立验证：`BeanInfos.get(classOf[DomainMenus]).properties` =
  `Map(domain -> ..., groups -> ...)`，`JsonSerializer` 输出
  `{"domain":{...},"groups":[...]}`，`MetaModels.get` = None

**修复**：新增 `EmsCoreMetaRegistrar`（`MetaRegistrar` 子类，portal 模块
`meta-registrars.txt` 声明），用 `register(classOf[...])` 把三个 case class 固化进
portal 的 `beanmeta.idx`：运行时 `BeanInfos.get` 命中索引，`BeanInfo.from` 只经
public 方法重建访问器，反射注册降为默认策略（public 方法 + public 构造器），
不再需要 declared 成员，也不再需要注册伴生对象（`findDefaultCtorParams` 仅存在于
`MetaLoader` 回退路径）。

**验证**（native 镜像，隔离 profile `lixin.native` 端口 8083）：
- `.../menus/user/imroot.json?forDomain=1` 返回 16391 字节完整 JSON
  （4 组应用、全部菜单），修复前为 `{}`（2 字节）
- 登录后 `/portal/index` 200，页面内嵌 `createNav(app,portal,{"domain":...})`，
  修复前为 `createNav(app,portal,{})`

**同类对象**：`Flows` 的 case class（`Flows$Flow/Activity/User/Group/Task/
Process/Comment/Attachment/Payload`）同样会被 `Payload.toJson` 序列化（外部应用
调用 `Flows.start/complete` 路径），已由 app 模块 `EmsAppMetaRegistrar` 一并固化进
app 的 `beanmeta.idx`。`AvatarTag`/`UserTag`（`Properties.set` 走 BeanInfos）与
`SessionInfo`/`AppLogEntry`（模板属性取值）同理。`security.authc.Profile` 属外部库
beangle-security，由 security 库 `SecurityAotHints` 随 jar 发布注册。

**历史误判**：早期记录的 `HttpUtils.get` → null（httpClient 失败）已过时；
`--initialize-at-run-time=java.net.http` 加入后内部调用正常。

### 3.4 模板裸 `ems` 求值 null/missing（✅ 已修复）

**现象**：登录后 `newly.ftl`/`todo/newly.ftl` 报
`The following has evaluated to null or missing: ==> ems.webapp`。

**根因**：模板里裸 `ems` 不是 `Ems` 伴生对象，而是 TagLibrary 模型
`EmsTagLibrary.models` 返回的 `EmsModels` 实例（`FreemarkerModelBuilder` 把
`mvc.TagLibrary.ems` 的 `models` 以 `ems` 为 key 放入模型）。BeansWrapper 经
`getMethods` 内省其 public 无参方法（webapp/api/avatar/user/permitted），native
下 `EmsModels` 未注册反射 → 内省为空 → `${ems.webapp}` 报 null/missing。
`b` 标签库的 `BeangleModels` 由 bui 库 `BuiMetaRegistrar` 注册所以正常，
`Ems$`（`nav.ems.api`）由 app 模块 `EmsAppMetaRegistrar` 一并固化。

**修复**：app 模块 `EmsAppMetaRegistrar` 注册 `EmsModels` 并固化 `AvatarTag`/`UserTag`
进 `beanmeta.idx`（`EmsModels.avatar/user` 返回的 ems 自有 tag 组件，
`DefaultTagTemplateEngine` 经 `getConstructor(ComponentContext)` 反射实例化、
`Properties.set` 经 BeanInfos 写属性）。BeansWrapper 内省只需 public 方法，
`register()` 的默认策略即覆盖，无需 declared 成员。

**验证**：native 镜像下 `/portal/user/message/newly`、`/portal/user/todo/newly`
由 500 恢复 200，`ems.webapp` 正常渲染为 `http://local.openurp.net`。


### 3.2 Tomcat MBean 描述符解析错误（🟡 非致命）

**现象**：启动时大量 `MbeansDescriptorsDigesterSource` 解析错误。

**原因**：Tomcat 的 MBean 描述符 XML 解析在 native-image 下的已知兼容性问题。

**影响**：不影响核心功能，Tomcat 正常启动。

### 3.3 `hibernate.jpa.static_metamodel.population=disabled`（✅ 已解决）

**改动**：在 `ConfigurationBuilder.addDefaultProperties()` 中添加。

**效果**：消除 66 个 JPA 静态元模型类（`X_`）的反射探测。

## 四、待办事项

### 高优先级
1. **`menus.json` 索引接口返回 `[]`** — `MenuWS.index` 的 `getTopMenus(app)` 空，
   JVM/native 行为一致，疑为 HQL 导航（`menu.channel.app`）独立问题，不影响
   主菜单加载（页面走 `user/{user}` 接口），待排查

### 中优先级
3. **清理反射配置** — 从 reflect-config.json 中移除已确认不需要的类
4. **优化启动时间** — 当前 0.317s 启动，可进一步优化

### 低优先级
5. **Tomcat MBean 错误** — 评估是否需要修复
6. **Caffeine jcache 类注册** — 已在 `EmsInfraAotHints` 中处理

## 五、关键文件路径

| 文件 | 用途 |
|---|---|
| `ems/build.sbt` | Native image 构建配置 |
| `app/.../org/beangle/ems/app/aot/`、`portal/.../org/beangle/ems/core/aot/`、`portal/.../org/beangle/ems/portal/aot/`、`ems/native/.../aot/` | EMS 应用 AOT hints（按模块放置，见 2.5） |
| `ems/native/docs/reflection-converged.md` | 反射配置收敛清单 |
| `ems/native/docs/system-url-list.md` | 系统 URL 功能列表 |
| `commons/src/main/scala/.../BuiltinAotHints.scala` | Commons AOT hints |
| `data/ConfigurationBuilder.scala` | Hibernate 配置 |

## 六、构建命令

```bash
# 构建 native image
cd ~/workspace/beangle/ems
sbt "native/compile; native/nativeImage"

# 运行 native image
./target/out/jvm/u/beangle-ems-native/native-image/beangle-ems-native \
  -Dems.profile=lixin.course --port=8082

# 测试
curl http://localhost:8082/
```
