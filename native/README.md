# Beangle EMS Native Module

GraalVM native-image module for Beangle EMS with Tomcat support.

## Overview

This module builds a native image for the Beangle EMS application using GraalVM with TomcatPlugin support. It includes:

- Tomcat embedded server (via TomcatPlugin)
- Hibernate ORM integration
- JCache/Caffeine second-level cache support
- GraalVM native-image compilation

The main entry point is `org.beangle.sas.engine.tomcat.Bootstrap`.

## Prerequisites

- GraalVM JDK 21+ (set `GRAALVM_HOME` or `JAVA_HOME` environment variable)
- sbt 2.x

## Building

### Build JAR

```bash
sbt "native/compile"
```

### Build Native Image

```bash
# Using the build script
./native/build-native.sh

# Or using sbt
sbt "native/nativeImage"
```

### Run Native Image

```bash
# Run the native image
./native/target/native/ems-native

# Run with system properties (same as JVM)
./native/target/native/ems-native -Dems.profile=dev

# Or run with sbt
sbt "native/run"
```

## Project Structure

```
native/
├── build-native.sh           # Build script for native image
├── README.md                 # This file
└── src/main/
    ├── resources/
    │   ├── beangle.xml       # ORM mapping configuration (EmsMapping)
    │   ├── logback.xml       # Logging configuration
    │   └── META-INF/beangle/
    │       └── aot-registrars.txt  # native 侧 AOT 注册器（第三方）
    └── scala/org/beangle/ems/nativeapp/
        ├── aot/
        │   └── EmsInfraAotHints.scala     # caffeine/jedis/hibernate 等第三方
        ├── EmsMapping.scala  # ORM entity mappings
        ├── NativeApp.scala   # Test application entry point
        └── TestUser.scala    # Test entity class
```

Note: 应用侧 AOT 注册器按"被注册类所在包"放置到对应模块（与库侧 bui/template
惯例一致），native 构建时各 jar 内嵌的 `META-INF/native-image/beangle` 配置自动合并：
- `app/.../org/beangle/ems/app/aot/EmsAotSupport.scala` + `EmsAppMetaRegistrar.scala`
  （`org.beangle.ems.app.*`，BeanMeta 固化进 beanmeta.idx，随 beangle-ems-app.jar 发布）
- `portal/.../org/beangle/ems/core/aot/EmsCoreMetaRegistrar.scala` +
  `portal/.../org/beangle/ems/portal/aot/EmsPortalAotHints.scala`
  （`org.beangle.ems.core.*`/`org.beangle.ems.portal.*`，随 beangle-ems-portal.jar 发布）

BeanMeta 注册器经各模块 `src/main/resources/META-INF/beangle/meta-registrars.txt`
声明：`MetaPlugin` 构建期生成 `beanmeta.idx`，`AotPlugin` 同时读取该文件收集反射
提示（默认策略：public 方法 + public 构造器）。native 下 `MetaLoader` 回退依赖
`getDeclaredMethods/getDeclaredFields`，仅注册 public 方法取不到，因此 DTO/模板
模型一律固化进 idx，不再注册 declared 成员。

Note: The main class for native image is `org.beangle.sas.engine.tomcat.Bootstrap` (provided by TomcatPlugin).

## Configuration

### Build Configuration

The native image build is configured in `build.sbt` with the following options:

- `--no-fallback`: Disable fallback to JVM
- `--enable-url-protocols=jar,resource`: Enable URL protocols for classpath resources
- `-H:+AddAllCharsets`: Add all character sets
- `-H:+ReportExceptionStackTraces`: Enable exception stack traces
- `--report-unsupported-elements-at-runtime`: Report unsupported elements at runtime
- `--initialize-at-build-time=ch.qos.logback,org.slf4j`: Initialize logback/slf4j at build time

### AOT Hints

The `aot/` package provides GraalVM AOT compilation hints, organized by the
package of the registered classes:

- `EmsAppMetaRegistrar` — `org.beangle.ems.app.*`：模板模型（`NavContext`/`App`/
  `Ems` 及内嵌类型）、ems 标签库（`EmsModels`/`AvatarTag`/`UserTag`）、OA 工作流
  DTO（`Flows` case class）固化进 beanmeta.idx；枚举 `Level` 用 `registerEnum`
- `EmsCoreMetaRegistrar` — `org.beangle.ems.core.*`：菜单 DTO（`DomainMenus`/
  `GroupMenus`/`AppMenus`）、会话/日志模型（`SessionInfo`/`AppLogEntry`）固化进
  beanmeta.idx；内部 bean `LogDbAppender` 与枚举（`EmbedMode`/`FlowStatus`/
  `NoticeStatus`）反射注册。服务接口不再注册：cdi 侧 `ScalaBeanInfoFactory` 对
  接口返回空属性 BeanInfo，Spring 接口遍历不再触发 `BeanInfos.get(接口)`，接口
  反射面不再构成 native 需求
- `EmsPortalAotHints` — `org.beangle.ems.portal.*`：portal 辅助类 `DomainSupport`
- security 库 `SecurityAotHints` — `org.beangle.security.*`：账户/档案模型
  （`DefaultAccount`/`Profile`）、枚举（`EventType`/`Scope`），随 beangle-security
  jar 的 `aot-registrars.txt` 发布
- `EmsInfraAotHints` — 第三方/基础设施：JCache/Caffeine、Jedis/pool2、FreeMarker
  模板支撑、Hibernate 查询缓存序列化、`java.time.Ser`、资源 pattern

## System Properties

GraalVM native-image supports passing system properties at runtime, just like JVM:

```bash
# Pass -D parameter
./target/native/ems-native -Dems.profile=dev

# Multiple parameters
./target/native/ems-native -Dems.profile=dev -Dlog.level=debug

# Using environment variables (if your app reads them)
EMS_PROFILE=dev ./target/native/ems-native
```

## Dependencies

- `beangle-data-hibernate`: Hibernate integration
- `beangle-she`: Configuration support
- `beangle-sas-engine`: Tomcat integration (via TomcatPlugin)
- `h2`: H2 database driver
- `caffeine`: Caching library
- `jcache`: JCache API
- `logback`: Logging implementation
- `tomcat-embed-core`: Tomcat core
- `tomcat-embed-websocket`: Tomcat WebSocket support

---
## 集成备忘（Integration Memo）

> 记录 2026-08-28 ~ 2026-08-31 为打通 EMS GraalVM native 镜像所做的跨仓库改动、
> 集成要点与验证状态。涉及仓库：`ems`、`sas`、`build`（sbt-beangle-build）、
> `commons`、`cdi`、`data`、`jdbc`、`hibernate`（beangle fork）、`she`。

### 一、总体架构：AOT 提示流水线

native-image 的反射/资源/代理/序列化配置由三路汇合，构建时按
`META-INF/native-image/` 约定自动发现，无需在命令行手写：

1. **库侧内嵌配置**（随 jar 发布，native-image 自动加载）：
   - `beangle-hibernate-core` fork：`reflect-config.json`（jboss-logging logger、
     PG JdbcType、id generator）+ `serialization-config.json`（缓存键/值类）
   - `beangle-sas-engine`：Tomcat 内部反射 + catalina 资源
   - `beangle-commons`：`META-INF/native-image/beangle/`（手工提交，`aotGenerate`
     任务再生成）
2. **库侧 AotHintRegistrar 动态生成**（sbt-beangle-build 的 `AotPlugin` 编译后钩子
   调 `org.beangle.commons.aot.AotHintGenerator` 生成，写入 `Compile / resourceManaged`
   随 jar 打包）：`beangle-cdi` `SpringAotHints`、`beangle-data-hibernate`
   `BeangleAotHints`、`beangle-jdbc` `JdbcAotHints`。
   - 声明来源：`META-INF/beangle/aot-registrars.txt`（每行一个 registrar）+ 各库
     `beangle.xml` 的 `<jpa>/<orm><mapping>`、`<cdi><module>`、`<web><initializer>`
   - web initializer（如 she 的 `ConfigInitializer`）不要求是 registrar，由生成器按
     `--classes` 清单注册（public 构造器 + Scala object 伴生类 `MODULE$`）
3. **应用侧注册器**：ems 4 个 registrar 按"被注册类所在包"放到对应模块（app：
   `EmsAppMetaRegistrar`；portal：`EmsCoreMetaRegistrar`/`EmsPortalAotHints`；native：
   `EmsInfraAotHints`），各模块经自己的 `aot-registrars.txt` 声明，AotPlugin 生成
   `META-INF/native-image/beangle/` 配置随 jar 发布，native 构建时自动合并，覆盖库侧
   不感知的应用面：ems 模板模型/标签库、菜单与工作流 DTO、内部 bean `LogDbAppender`、
   caffeine/jcache、Jedis/pool2、枚举序列化、资源 pattern。security 的注册随
   beangle-security 库发布（`SecurityAotHints`）。

`build-native.sh` 追加的 native-image 参数：`--no-fallback`、
`--enable-url-protocols=jar,resource,http,https`、`-H:+AddAllCharsets`、
`--report-unsupported-elements-at-runtime`、
`--initialize-at-build-time=ch.qos.logback,org.slf4j`。
注意 `build.sbt` 的 `nativeImageOptions` 目前只有 `jar,resource`：若走
`sbt native/nativeImage` 而非脚本构建，需补 `http,https`（运行期经 URL 读资源）。

### 二、各仓库修改历史

| 仓库 | 关键改动 | 目的 / 对应关卡 |
|---|---|---|
| **ems** | `EmsApp.readProperties`：`classpath:beangle.xml` → `classpath*:beangle.xml` | native 下多个 jar（app/portal/she/native）各带 `beangle.xml`，合并后才能同时读到 `<ems>` 元素、web initializer、mvc static 与 jpa mapping |
| **ems** | `build.sbt`/`plugin.sbt`：新增 `native` 子工程（NativeImagePlugin + TomcatPlugin），依赖 sas-engine、tomcat-embed、beangle-data-hibernate、beangle_she、caffeine/jcache | native 构建入口与依赖装配；TomcatPlugin 依赖是 test scope，需在 native 子工程显式声明为 compile scope |
| **ems** | `app/`/`portal/`/`native/` 各自 `aot/` 包（5 个 AOT registrar 按模块放置，见上文第 3 条）+ `aot-registrars.txt` + `native/beangle.xml`（jpa mapping）+ `logback.xml` + `build-native.sh` | 应用侧 AOT 提示、ORM 映射、构建脚本 |
| **sas** | `Server.Config.isNativeImage()` + `guessDocBase()` native 分支 | native 下 `getResource("").getFile()` 探测 IDE 路径不可用，改用默认 docBase |
| **sas** | `TomcatServerBuilder.prepareContext()`：`context.setDocBase(config.docBase)` | 替换 `"classpath:webapp"`（native 下 EmbeddedClassLoader 不支持 classpath: 前缀） |
| **sas** | `Bootstrap`：native 下跳过 `Desktops.openBrowser` | native 下 AWT 不可用（GraphicsEnvironment 初始化触发 JNI 致命错误） |
| **sas** | `DependencyClassLoader`：忽略 `MethodHandles.findSpecial` 失败 | native 下 MethodHandles 受限，容错处理 |
| **sas** | 新增 `engine/src/main/resources/META-INF/native-image/org.beangle.sas/sas-engine/`（reflect/resource/native-image-args） | Tomcat 内部反射与 catalina 资源 |
| **build** | `AotPlugin`：beangle.xml 的 jpa/cdi module 并入 registrar 清单；web initializer 经 `--classes` 按名注册 | 解决 `ConfigInitializer`、`DefaultJsonpDriver` 等运行期 `ClassNotFoundException` |
| **build** | `GeneratorSupport`：退出码 2（声明类未找到）短暂重试最多 10 次 | sbt 2 下 classDirectory 物化可能晚于编译完成 |
| **commons** | `AotHints.registerArrayOf`（按简单类名注册数组 + `unsafeAllocated`） | Hikari `Array.newInstance` 创建 `Statement[]`/`IConcurrentBagEntry[]` |
| **commons** | `AotHints.registerProxyByName`（按接口名注册 JDK 动态代理） | 引用 `private[core]` 的接口（如 `SerializableTypeWrapper`） |
| **commons** | 枚举注册自动补 public 字段（`MODULE$`/`$VALUES`） | Scala 3/Java 枚举运行期字段反射 |
| **cdi** | `SpringAotHints`：`spring.factories` 资源 + `ScalaBeanInfoFactory`/`ScalaBeanInfo` + `FactoryBeanProxy`/`ContainerEventMulticaster`（declared 策略）+ SerializableTypeWrapper 3 个代理 | Spring 7 `CachedIntrospectionResults` 需发现 BeanInfoFactory，否则 Scala 属性 setter（`registry_$eq`）无法识别 |
| **data** | `BeangleAotHints`：4 个容器 bean 按 declared 注册 + EntityDao 事务代理注册 | `HibernateEntityDao.domain` 等属性注入（MetaLoader 反射 dig 需 declared 成员）；`TransactionProxyFactoryBean` 的 JDK 动态代理 |
| **jdbc** | `JdbcAotHints`：HikariCP/PG 反射 + 数组 + `Connection` 代理 + keywords/driverconfig 资源 | 连接池与驱动在 native 下的反射面（Hikari `PropertyElf`、`PGSimpleDataSource` 等） |
| **bui** | 新增 `BuiAotHints`：注册 `BeangleModels`/`BeangleTagLibrary`，并枚举 bui 包全部类注册带 `(ComponentContext)` 构造器的 tag 组件（Head/Form/Grid 等 60+，含 `Grid$Bar` 嵌套类，declared 策略） | 模板 `b` 模型方法（`b.css` 等）与 `DefaultTagTemplateEngine.newTag` 的 `clazz.getConstructor(ComponentContext)` 反射实例化；`TagModel` 经 `Properties.set`/BeanInfos 设置属性需 declared 成员 |
| **webmvc** | `WebmvcAotHints` 增加 `CoreModels`（`base`/`text`/`url`）与 `Static`（`css`/`url`/`load`） | `b` 模型父类方法与 `b.static` 返回对象的反射面 |
| **template** | `TemplateAotHints` 增加 `AbstractModels` 与 UIBean 层级（`Component`/`ComponentContext`/`UIBean`/`ClosingUIBean`/`IterableUIBean`，declared 策略）；资源 pattern 增 `themes/.*` | 模板模型基类方法；tag 组件父类属性的 BeanInfo 反射 dig；主题资源（theme.properties/themes.list）供精确名查找 |
| **template** | `Themes.loadThemeProps` 增加 `themes/themes.list` 精确资源回退（目录扫描结果为空时按清单逐主题加载 theme.properties） | native 下 `getResources("themes/")` 目录枚举不可用，`themes` 注册表为空导致默认主题退化为 `notdefined`，后台页面 `key not found: notdefined` |
| **bui** | `bootstrap` 模块新增 `themes/themes.list`（bootstrap/html|list|mini|search） | 主题清单：native 下替代目录扫描的主题发现来源 |
| **ems** | `EmsInfraAotHints` 按名注册 `java.time.Ser`（`Class.forName`，进 serialization-config） | `LocalDateTime`/`Instant` 等经 `writeReplace` 写为包级私有 `Externalizable` 代理 `Ser`，反序列化 `Class.forName` 失败报 `ClassNotFoundException: java.time.Ser` |
| **hibernate fork** | `reflect-config.json`：24 个 `_$logger` + 7 个 `PostgreSQL*JdbcType` + 4 个 id generator | jboss-logging 日志器、PG 方言类型、主键生成器的运行期构造 |
| **hibernate fork** | 新增 `serialization-config.json`：`BasicCacheKeyImplementation`/`StandardCacheEntryImpl`/`AbstractReadWriteAccess$Item` | 实体二级缓存键/值 Java 序列化 |
| **she** | `beangle.xml` 声明 `<web><initializer>` 与 `<cdi><module>`（`DefaultModule` 等） | 经 AotPlugin 生成 reflect-config，覆盖 `ConfigInitializer` 与 json/text/xml serializer 模块 |

> 注：commons 的 `MetaRegistrar` 曾尝试加 `registerHierarchy` 递归注册（现已还原）——
> 核查后不需要：递归需求由各库 registrar 显式注册 + AotPlugin 的 beangle.xml 扫描
> 覆盖，commons 保持稳定基线，不宜轻易改动。

### 三、关键集成要点

- **beangle.xml 合并**：应用用 `classpath*:beangle.xml` 汇总多 jar 配置；`<ems>`
  元素（app-name）在 portal 的 beangle.xml，web initializer 在 she 的，jpa mapping
  在 native 的，缺一不可。
- **sbt 2 CAS jar 时序**：`compile` 只更新 classes/resource_managed，不重建 jar；
  `fullClasspath` 返回 CAS 内容寻址 jar（指向 `~/.cache/sbt/v2/cas/`）。取 classpath
  前必须先 `packageBin`，并去掉 `>sha256-.../N` 标注。`build-native.sh` Step 1 已内置
  `compile;packageBin` 顺序。详见 `data/docs/sbt2-cas-jar.md`。
- **AOT 资源随 jar 过期**：改了库侧 `AotHintRegistrar` 后，publishLocal 前必须重跑
  生成器，否则 jar 打包过期配置。如 data 仓库：
  `sbt "hibernate/aotHints;hibernate/publishLocal"`（plain publishLocal 不触发）。
- **序列化注册需含 JDK 类型**：`JavaSerializationCopier` 对缓存键/值做序列化往返，
  `java.lang.String` 等 JDK 类型不注册会报
  `SerializationConstructorAccessor class not found`；枚举值（含带主体匿名子类）
  也要进 serialization-config。
- **java.time 序列化代理**：`LocalDate`/`LocalDateTime`/`Instant` 经 `writeReplace`
  写为包级私有 `Externalizable` 类 `java.time.Ser`，反序列化时 `ObjectInputStream`
  `Class.forName("java.time.Ser")` 失败会报 `ClassNotFoundException`。无法 `classOf`
  引用，生成期用 `Class.forName` 按名注册进 serialization-config（reflect 侧会被
  `AotHints.isJdk` 过滤，serialization-config 注册即足够：native-image 会纳入镜像并
  注册默认构造器）。
- **native 下 classpath 目录枚举不可用**：`getResources("themes/")` 在 native 返回
  `resource:` URL，commons 的 `ResourcePatternResolver` 按 jar/file 协议枚举子项，
  native 下无法展开（切勿为此改 commons）。主题发现改为「扫描 +
  `themes/themes.list` 回退」：扫描结果为空时按精确资源名读取清单，再逐个加载
  `theme.properties`；`themes/.*` 资源 pattern 使精确名查找在运行期可用。曾试
  `--initialize-at-build-time=...Themes$`，会触发 scala/commons 传递性初始化级联，
  被 GraalVM proven-safe 检查拒绝，放弃。
- **tag 组件反射**：`b.head` 等经 `DefaultTagTemplateEngine.newTag` 反射
  `clazz.getConstructor(classOf[ComponentContext])` 实例化，`TagModel` 再经
  `Properties.set`（BeanInfos → MetaLoader，declared 成员）设置模板参数。组件类多
  （60+）且含嵌套类，注册器里枚举 bui 包全部带该构造器的类统一注册；父类层级
  （template api 的 UIBean 系列）也需 declared 注册。
- **代理按接口注册，顺序与运行期一致**：JDK 动态代理类按接口列表缓存；`private[core]`
  接口用 `registerProxyByName`，接口顺序必须与 `ProxyFactory` 一致。
- **数组类型**：`Array.newInstance` 反射创建数组需注册数组类并标记
  `unsafeAllocated`（`registerArrayOf`）。
- **条件绑定 bean**：`if (redis.nonEmpty)` 之类条件 bind 宏在构建期配置为空时不执行，
  bean 不会经 MetaRegistrar 自动注册，需显式 `registerType`
  （如 `RedisClientFactory`、内部 bean `LogDbAppender`）。
- **caffeine 策略类**：运行期按 builder 配置拼接类名 `findClass`（如 `SSMSAW`），
  无法静态推导，构建期枚举 caffeine jar 全部顶层类注册（declared 策略）。
- **日志器**：logback/slf4j 构建期初始化；hibernate 的 `_$logger` 需注册
  `(org.jboss.logging.Logger)` 构造器。
- **Tomcat JMX**：SAS 已设 `org.apache.tomcat.util.modeler.disable=true`，modeler
  描述符注册仍有报错噪音（见下），不影响连接器启动。

### 四、验证状态（2026-08-31）

已完整验证（连真实库 `-Dems.profile=lixin.course`）：

- Spring 容器完整加载（web initializer 关卡通过，含 `ConfigInitializer`）
- Hikari 连 `platform_lixin`、Redis/Jedis 正常、JCache/Caffeine 二级缓存序列化正常
- Tomcat 绑定端口并响应 HTTP（`curl -i http://127.0.0.1:8080/` 返回 302）
- Action scan 94 actions / 537 mappings，定时任务正常
- Jedis `pom.properties` 资源缺失报错已消除（`registerPattern`）
- 登录页完整渲染 200（`b.css`/`b.static_url`/`b.text` 正常）；登录流程经
  `java.time.Ser` 反序列化关卡与 `b.head` 等 tag 组件实例化关卡
  （验证页面 `/cas/qrcode/scan` 已无 `Head.<init>` 反射错误）

已知非致命噪音：

- Tomcat modeler JMX 描述符注册报错（`Cannot find method [add] in ArrayList` /
  `ManagedBean.createOperationKey` NPE）——仅 MBean 注册失败，连接器正常服务。
- `-Dems.profile=local` 因缺表报 SQL 错，属本地环境问题；真实验证用 `lixin.course`。

全量 URL 验证（fix2 镜像 16:00 构建，110 个真实 URL，见第五节）：

- `200 × 55 / 404 × 30 / 500 × 25`；相比旧镜像仅修复 `session/index`
  （500 → 200，SessionInfo declared 注册）；其余 500 以 select.ftl 哈希 entry
  反射为主（P0，见第五节候选清单）。

### 五、JVM agent 反射面采集与对比（2026-08-31 下午）

按「全量跑 portal → native-image-agent 采集 JVM 反射面 → 与当前 native 配置对比」
的思路，把「还有哪些反射没注册」从猜变成清单。产物与脚本在 `/tmp`，不随仓库提交。

#### 采集方法

1. JVM 侧以真实配置启动并登录（`-Dems.profile=lixin.course`，`imroot/123456`）：
   ```bash
   java -agentlib:native-image-agent=config-output-dir=/tmp/agent-portal-20260831 \
     -cp "$(cat target/native/native-image-cp.txt | tr '\n' ':')" \
     -Dems.profile=lixin.course org.beangle.sas.engine.tomcat.Bootstrap
   ```
2. 用 `/tmp/ems_crawl_real.py` 依次访问 `/tmp/real_urls.json` 中 110 个真实 URL
   （portal action 全量 + DB `se_menus` 路由），覆盖列表/搜索/详情各形态。
3. 对比：`python3 /tmp/compare_agent.py /tmp/agent-portal-20260831`
   - agent 采集 1748 类（reflect 404KB）；与构建期合并配置对比，
     非 JDK 且不在配置中、但真实存在于 classpath 的「有意义缺失」共 **520 类**，
     明细落盘 `/tmp/missing_agent-portal-20260831.json`。
   - 分类 Top：`org.beangle.ems.core` 129、`ids.cas` 19、`data.model` 13、
     `ems.app` 13、`security.authc` 13、`catalina` 11、`commons.bean` 11、
     `security.web` 11、`webmvc.config` 11、`webmvc.support` 11、
     `hibernate.engine` 11、`spring.transaction` 11、`security.session` 9、
     `template.api` 6、scala 系列 35。

#### 当前 native 全量验证（fix2 镜像 16:00 构建，8083）

- 110 个 URL：**200 × 55 / 404 × 30 / 500 × 25**（`/tmp/native-real-after2.log`）。
- 已随 fix2 修复：`session/index` 200（SessionInfo declared 注册）。
  本就正常的 200：`user/root`、`security/channel/search`、`config/*` 列表/搜索等。
- 仍 500 分三类：
  - **select.ftl 系列（native 特有，约 9 个）**：`oa/doc`、`oa/notice`、
    `oa/notice-audit`、`security/channel`、`security/menu`、`session/event`、
    `user/depart`、`user/user` 等。报错
    `item[tag.keyName] in themes/bootstrap/search/select.ftl` —— 模板
    `[@b.select items={"1":"是","0":"否"}]` 哈希字面量迭代 entry 反射失败。
  - **JVM 固有 500（不算 native 回归）**：`oa/news`、`oa/todo`（action 无
    index/list 视图）、`user/avatar`、`user/profile`（含 DB 缺 `domain_id` 列的
    SQL 错，`-Dems.profile=local` 同样存在）。
  - 其余 404 为无对应 action 的路由（JVM 同样 404）。

#### 待补注册候选清单（决策用，按优先级）

> 完整缺口清单（520 应用类 + 35 scala + 207 JDK + 52 数组类，含 agent 捕获形态）见
> `docs/agent-reflect-gaps.md`。

**P0 — select.ftl 哈希 entry 反射（9 个 500 的直接根因）**

| 类 | agent 捕获形态 | 当前状态 | 建议 |
|---|---|---|---|
| `freemarker.ext.beans.HashAdapter$1$1$1` | `allPublicFields` + `queryAllPublicMethods` + `queryAllPublicConstructors` | 已注册 allPublic/Declared methods + ctors + declaredFields，**缺 query 标志与 public fields** | 补 `queryAllPublicMethods`/`queryAllPublicConstructors`/`allPublicFields` |
| `java.util.Map$Entry` | `queryAllPublicMethods` + `getKey/getValue/setValue/equals/hashCode` | 完全未注册 | 注册接口方法（或 `allPublicMethods`） |
| `[Ljava.util.Map$Entry;` | class-only | 未注册 | class-only 数组类 |
| `HashAdapter$1$1$1BeanInfo` / `...Customizer` | 名称条目（**类不存在**，JDK Introspector 失败 forName 探测） | 未注册 | 可不注册；按名注册仅保持 CNFE 语义 |

**P1 — FreeMarker/BeanInfo 相关（GenericObjectModel 路径的公共面）**

- bui `Grid$Boxcol/Col/Row`、`Profile$HibernateProxy`、`Db$HibernateProxy`、
  `User$HibernateProxy`、`EmbedMode$$anon$N`、`NoticeStatus$$anon$N` 等
  `XBeanInfo`/`XCustomizer` 共 29 个：除 `org.beangle.commons.lang.reflect.BeanInfo`
  外全部**不存在于 classpath**，同为 Introspector 失败探测，可不注册。
- 若 P0 修复后 select 仍失败，重点观察 FreeMarker `ClassIntrospector` 走
  `Introspector.getBeanInfo` 的目标类是否还需 `queryAllDeclaredMethods` 等。

**P2 — 框架运行期真实反射类（agent 捕获了 methods/declared 形态，多数为单例注入）**

- `security`：`UrlEntryPoint(url)`、`DBCredentialStore`（allDeclared* + query 标志）、
  `DomainProvider(getDomainId)`、`CacheSessionRepo`、`CookieSessionIdPolicy` 等。
- `webmvc.config`：`ActionMappingBuilder`、`Configurator`、`ProfileProvider` 等
  （多为接口 + 伴生对象，`Object` 的 `MODULE$` 是否已由 AotPlugin 覆盖需核对）。
- `template.api`：`ModelBuilder`、`TagLibrary`、`TagTemplateEngine` 等接口
  （可能为代理注册场景，需核对 proxy-config）。
- `data.model`：`Entity`、`IntIdEntity`（`queryAllPublicMethods`）等父接口/基类。

**P3 — scala 集合（35 个，需静态 JSON，registrar 的 jdkPrefixes 含 `scala.` 无法注册）**

- `scala.collection.immutable.List/Map/Set/Seq`、`scala.Option`、`scala.Product`、
  `scala.collection.mutable.*`、`concurrent.TrieMap(root 字段)`、`convert` wrapper、
  `deriving.Mirror`、`math.Ordered`、`reflect.Enum`、`runtime.EnumValue` 等。
- 多为序列化对象图（`JavaSerializationCopier`）或代理探测，class-only / 字段级即可。

**无需注册（agent 噪声）**

- `X_` metamodel 66 个（`App_`/`User_`/`Role_`…）：**classpath 不存在**，JPA 静态
  元模型 forName 失败探测。
- 各类 `XBeanInfo/XCustomizer` 29 个：同上，Introspector 失败探测。
- `HashAdapter$1$1$1BeanInfo` 等失败 forName 名称条目：native 下同样抛 CNFE，
  行为与 JVM 一致，无需注册。

#### 待决策/待验证

1. **beanmeta.idx 是否真正加载**：native 镜像内已确认含 `BBXI` 字节，JVM 侧
   `MetaModels` 载荷 282 类、`App`/`Depart` 命中；但 native 内 `MetaModels.cache`
   可能因 `Resources.load("classpath*:META-INF/beangle/beanmeta.idx")` 多资源枚举
   语义差异而加载失败，导致 `BeanInfos.get` 回退 `MetaLoader`（丢失 `hasNext`/
   `totalPages` 等 def 型 getter）。建议用最小 native 探针
   （`/tmp/ProbeNative.java`，验证 `MetaModels.contains(App)`）确认后再决定是否
   调整加载方式（commons 不宜轻易改动，优先考虑 native 侧注册/装配方案）。
2. **P0 修复后重建**：补 `Map$Entry` + query 标志后重建镜像，复跑
   `/tmp/ems_crawl_real.py 8083 imroot 123456`，目标 200 数从 55 提升（select.ftl
   系列清零），并与 JVM 基线对齐（JVM 固有 500 不计回归）。

### 六、命令速查


```bash
# 构建 native 镜像（约 4-5 分钟；内部先 compile 再 packageBin）
cd /home/chaostone/workspace/beangle/ems && ./native/build-native.sh

# 运行（默认 8080；可指定端口）
./native/target/native/ems-native -Dems.profile=lixin.course --port=8082

# 库侧改 registrar 后重新 publishLocal（顺序：commons → build → cdi/data/jdbc → sas → she）
sbt -Dsbt.version=2.0.8 "commons/publishLocal"          # 部分模块需 -Dsbt.version 规避服务版本 mismatch
sbt "hibernate/aotHints;hibernate/publishLocal"         # data 仓库必须显式重跑 aotHints

# 调试：native-image-agent 采集运行期反射
java -agentlib:native-image-agent=config-output-dir=/tmp/agent-course \
  -cp "$(cat target/native/native-image-cp.txt | tr '\n' ':')" \
  -Dems.profile=lixin.course org.beangle.sas.engine.tomcat.Bootstrap

# 全量访问 portal 采集反射面（agent 输出目录含 reflect/resource/proxy/serialization 四件）
python3 /tmp/ems_crawl_real.py 8083 imroot 123456        # 需先以 agent 方式启动 JVM

# 与当前 native 合并配置对比，输出缺失清单
python3 /tmp/compare_agent.py /tmp/agent-portal-20260831
# 缺失明细：/tmp/missing_agent-portal-20260831.json
```

### 参考

- `data/docs/sbt2-cas-jar.md`：sbt 2 CAS jar 时序隐患详情
- `build/docs/aot.md`：AotPlugin 配置生成机制
- [GraalVM Native Image](https://www.graalvm.org/latest/reference-manual/native-image/)
