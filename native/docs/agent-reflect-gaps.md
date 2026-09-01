# EMS Native 反射缺口清单（agent 采集 × 构建配置对比）

> 来源：`/tmp/agent-portal-20260831/`（native-image-agent 采集 JVM 全量 portal 访问，110 个真实 URL，登录 `imroot/123456`，`-Dems.profile=lixin.course`）

> 对比基线：构建期合并 reflect-config（`target/out/jvm/u/beangle-ems-native/resource_managed/main/META-INF/native-image/beangle/reflect-config.json`）

> 含义：以下条目是 **JVM 上真实发生过反射、但当前 native 合并配置中缺失** 的类。带形态（`AM`=allPublicMethods、`QM`=queryAllPublicMethods、`AC`=构造器、`AF`=字段、`methods:N`=具体方法数）的条目可信度高；`class-only` 多为 `Class.forName` 探测。已去除 JPA 静态元模型噪声（`hibernate.jpa.static_metamodel.population=disabled`）和 JDK Introspector BeanInfo/Customizer 探测噪声。

> 生成方式：`python3 /tmp/compare_agent.py /tmp/agent-portal-20260831`；明细文件 `/tmp/missing_agent-portal-20260831.json`、`/tmp/jdk_missing.json`、`/tmp/arr_missing.json`。

## 统计总览

| 类别 | 数量 | 说明 |
|---|---|---|
| 非 JDK 缺失（classpath 存在） | **391** | 应用/框架类，agent 捕获、合并配置无（已去除 JPA 元模型 + BeanInfo/Customizer 噪声） |
| └ scala 相关 | 33 | 需静态 JSON 注册（registrar 的 `jdkPrefixes` 含 `scala.`，无法用 registrar 注册） |
| JDK 相关缺失 | 202 | agent 捕获的 `java.*`/`javax.*`/`jdk.*`/`sun.*`/`com.sun.*`，多数为 JMX/序列化探测 |
| 数组类缺失 | 52 | `Array.newInstance`/`getComponentType` 反射面（含 `[Ljava.util.Map$Entry;`，P0 相关） |

## 一、非 JDK 应用类缺失（391，按包分组）

> 完整类名列表，按包分组；组内按字母序。

### `ch.qos.logback`（4）

- `ch.qos.logback.core.OutputStreamAppender`
- `ch.qos.logback.core.encoder.Encoder`
- `ch.qos.logback.core.pattern.PatternLayoutEncoderBase`
- `ch.qos.logback.core.spi.ContextAware`

### `com.github.benmanes`（3）

- `com.github.benmanes.caffeine.cache.BBHeader$ReadAndWriteCounterRef`
- `com.github.benmanes.caffeine.cache.BBHeader$ReadCounterRef`
- `com.github.benmanes.caffeine.cache.BLCHeader$DrainStatusRef`

### `com.google.protobuf`（1）

- `com.google.protobuf.ExtensionRegistry`

### `com.zaxxer.hikari`（2）

- `com.zaxxer.hikari.HikariConfig$`
- `com.zaxxer.hikari.HikariConfigMXBean`

### `freemarker.ext.beans`（2）


### `freemarker.ext.jython`（1）

- `freemarker.ext.jython.JythonModel`

### `freemarker.template.Configuration`（1）

- `freemarker.template.Configuration`

### `org.apache.catalina`（11）

- `org.apache.catalina.LifecycleState`
- `org.apache.catalina.connector.Request`
- `org.apache.catalina.loader.JdbcLeakPrevention`
- `org.apache.catalina.mbeans.ClassNameMBean`
- `org.apache.catalina.mbeans.ConnectorMBean`
- `org.apache.catalina.mbeans.ContainerMBean`
- `org.apache.catalina.mbeans.ContextMBean`
- `org.apache.catalina.mbeans.NamingResourcesMBean`
- `org.apache.catalina.mbeans.ServiceMBean`
- `org.apache.catalina.startup.Tomcat$SimpleRealm`
- `org.apache.catalina.util.CharsetMapper`

### `org.apache.commons`（1）

- `org.apache.commons.jexl3.JexlArithmetic`

### `org.apache.coyote`（3）

- `org.apache.coyote.Adapter`
- `org.apache.coyote.ContinueResponseTiming`
- `org.apache.coyote.Processor`

### `org.apache.tomcat`（3）

- `org.apache.tomcat.util.http.parser.HttpParser`
- `org.apache.tomcat.util.modeler.FeatureInfo`
- `org.apache.tomcat.util.net.AbstractEndpoint$Handler`

### `org.beangle.bui`（24）

- `org.beangle.bui.Anchor$`
- `org.beangle.bui.Date$`
- `org.beangle.bui.DefaultModule$`
- `org.beangle.bui.Div$`
- `org.beangle.bui.Form$`
- `org.beangle.bui.Grid$`
- `org.beangle.bui.Grid$Bar$`
- `org.beangle.bui.Grid$Col$`
- `org.beangle.bui.Grid$Treecol$`
- `org.beangle.bui.Messages$`
- `org.beangle.bui.Nav$`
- `org.beangle.bui.Navitem$`
- `org.beangle.bui.Select$`
- `org.beangle.bui.Submit$`
- `org.beangle.bui.Textfield$`
- `org.beangle.bui.Textfields$`
- `org.beangle.bui.Toolbar$`
- `org.beangle.bui.WebUIBean`

### `org.beangle.cache`（1）

- `org.beangle.cache.AbstractCacheManager`

### `org.beangle.cache.redis`（1）

- `org.beangle.cache.redis.RedisClientFactory$`

### `org.beangle.cdi.config`（1）

- `org.beangle.cdi.config.ContainerEventMulticaster$`

### `org.beangle.cdi.spring`（1）

- `org.beangle.cdi.spring.FactoryBeanProxy$`

### `org.beangle.commons.activation`（1）

- `org.beangle.commons.activation.MediaType`

### `org.beangle.commons.aot`（1）

- `org.beangle.commons.aot.AotHintRegistrar`

### `org.beangle.commons.bean`（11）

- `org.beangle.commons.bean.Disposable`
- `org.beangle.commons.bean.Disposable$`
- `org.beangle.commons.bean.Factory`
- `org.beangle.commons.bean.Factory$`
- `org.beangle.commons.bean.Initializing`
- `org.beangle.commons.bean.Initializing$`
- `org.beangle.commons.bean.Refreshable`
- `org.beangle.commons.bean.Refreshable$`
- `org.beangle.commons.bean.Scheduled`
- `org.beangle.commons.bean.Scheduled$`
- `org.beangle.commons.bean.meta.MetaModels$`

### `org.beangle.commons.cache`（2）

- `org.beangle.commons.cache.Cache`
- `org.beangle.commons.cache.CacheManager`

### `org.beangle.commons.cdi`（3）

- `org.beangle.commons.cdi.CdiEventListener`
- `org.beangle.commons.cdi.CdiEventListener$`
- `org.beangle.commons.cdi.Container`

### `org.beangle.commons.collection`（2）

- `org.beangle.commons.collection.page.Page`
- `org.beangle.commons.collection.page.SinglePage$`

### `org.beangle.commons.config`（1）

- `org.beangle.commons.config.XmlConfigs`

### `org.beangle.commons.event`（7）

- `org.beangle.commons.event.DefaultEventMulticaster`
- `org.beangle.commons.event.EventListener`
- `org.beangle.commons.event.EventListener$`
- `org.beangle.commons.event.EventMulticaster`
- `org.beangle.commons.event.EventMulticaster$`
- `org.beangle.commons.event.EventPublisher`
- `org.beangle.commons.event.EventPublisher$`

### `org.beangle.commons.io`（1）

- `org.beangle.commons.io.BinarySerializer`

### `org.beangle.commons.json`（2）

- `org.beangle.commons.json.JsonArray`
- `org.beangle.commons.json.JsonObject`

### `org.beangle.commons.lang`（3）

- `org.beangle.commons.lang.JVM$`

### `org.beangle.commons.script`（1）

- `org.beangle.commons.script.ExprEvaluator`

### `org.beangle.commons.text`（3）

- `org.beangle.commons.text.i18n.TextBundleLoader`
- `org.beangle.commons.text.i18n.TextBundleLoader$`
- `org.beangle.commons.text.i18n.TextFormatter`

### `org.beangle.data.dao`（3）

- `org.beangle.data.dao.EntityDao`
- `org.beangle.data.dao.EntityDao$`
- `org.beangle.data.dao.OqlBuilder`

### `org.beangle.data.hibernate`（1）

- `org.beangle.data.hibernate.HibernateEntityDao$`

### `org.beangle.data.model`（13）

- `org.beangle.data.model.Entity`
- `org.beangle.data.model.IntId`
- `org.beangle.data.model.IntIdEntity`
- `org.beangle.data.model.LongId`
- `org.beangle.data.model.LongIdEntity`
- `org.beangle.data.model.meta.Domain`
- `org.beangle.data.model.pojo.Coded`
- `org.beangle.data.model.pojo.Enabled`
- `org.beangle.data.model.pojo.Hierarchical`
- `org.beangle.data.model.pojo.Named`
- `org.beangle.data.model.pojo.Remark`
- `org.beangle.data.model.pojo.TemporalOn`
- `org.beangle.data.model.pojo.Updatable`

### `org.beangle.data.orm`（2）

- `org.beangle.data.orm.AbstractDaoTask`
- `org.beangle.data.orm.MappingModule$`

### `org.beangle.ems.app`（13）

- `org.beangle.ems.app.log.Appender`
- `org.beangle.ems.app.log.BusinessLogger`
- `org.beangle.ems.app.log.BusinessLogger$`
- `org.beangle.ems.app.log.ErrorLogger`
- `org.beangle.ems.app.log.ErrorLogger$`
- `org.beangle.ems.app.oa.Flows$Attachment`
- `org.beangle.ems.app.oa.Flows$Payload`

### `org.beangle.ems.cas`（1）

- `org.beangle.ems.cas.action.DefaultModule$`

### `org.beangle.ems.core`（129）

- `org.beangle.ems.core.CacheModule$`
- `org.beangle.ems.core.CronModule$`
- `org.beangle.ems.core.EventModule$`
- `org.beangle.ems.core.LogModule$`
- `org.beangle.ems.core.SmsModule$`
- `org.beangle.ems.core.WebReconfigModule$`
- `org.beangle.ems.core.cas.CredentialModule$`
- `org.beangle.ems.core.cas.SeurityModule$`
- `org.beangle.ems.core.cas.TicketModule$`
- `org.beangle.ems.core.config.model.HasEnvIds`
- `org.beangle.ems.core.config.model.LocaleTitle`
- `org.beangle.ems.core.config.service.AppService`
- `org.beangle.ems.core.config.service.AppService$`
- `org.beangle.ems.core.config.service.CredentialService`
- `org.beangle.ems.core.config.service.CredentialService$`
- `org.beangle.ems.core.config.service.DataSourceManager`
- `org.beangle.ems.core.config.service.DbService`
- `org.beangle.ems.core.config.service.DbService$`
- `org.beangle.ems.core.config.service.DefaultModule$`
- `org.beangle.ems.core.config.service.DomainService`
- `org.beangle.ems.core.config.service.DomainService$`
- `org.beangle.ems.core.log.service.LogDbAppender$`
- `org.beangle.ems.core.oa.service.DefaultModule$`
- `org.beangle.ems.core.oa.service.DocService`
- `org.beangle.ems.core.oa.service.DocService$`
- `org.beangle.ems.core.oa.service.FlowService`
- `org.beangle.ems.core.oa.service.FlowService$`
- `org.beangle.ems.core.oa.service.MessageService`
- `org.beangle.ems.core.oa.service.MessageService$`
- `org.beangle.ems.core.oa.service.TodoService`
- `org.beangle.ems.core.oa.service.TodoService$`
- `org.beangle.ems.core.security.model.SessionInfo$`
- `org.beangle.ems.core.security.service.DefaultModule$`
- `org.beangle.ems.core.security.service.FuncPermissionService`
- `org.beangle.ems.core.security.service.FuncPermissionService$`
- `org.beangle.ems.core.security.service.MenuService`
- `org.beangle.ems.core.security.service.MenuService$`
- `org.beangle.ems.core.security.service.ProfileService`
- `org.beangle.ems.core.security.service.ProfileService$`
- `org.beangle.ems.core.security.service.SessionInfoService`
- `org.beangle.ems.core.security.service.SessionInfoService$`
- `org.beangle.ems.core.user.service.AvatarService`
- `org.beangle.ems.core.user.service.AvatarService$`
- `org.beangle.ems.core.user.service.DefaultModule$`
- `org.beangle.ems.core.user.service.DimensionService`
- `org.beangle.ems.core.user.service.DimensionService$`
- `org.beangle.ems.core.user.service.PasswordConfigService`
- `org.beangle.ems.core.user.service.PasswordConfigService$`
- `org.beangle.ems.core.user.service.RoleService`
- `org.beangle.ems.core.user.service.RoleService$`
- `org.beangle.ems.core.user.service.UserService`
- `org.beangle.ems.core.user.service.UserService$`

### `org.beangle.ems.index`（1）

- `org.beangle.ems.index.action.DefaultModule$`

### `org.beangle.ems.nativeapp`（1）


### `org.beangle.ems.portal`（10）

- `org.beangle.ems.portal.action.DefaultModule$`
- `org.beangle.ems.portal.action.admin.BlobModule$`
- `org.beangle.ems.portal.action.admin.ConfigModule$`
- `org.beangle.ems.portal.action.admin.DomainSupport`
- `org.beangle.ems.portal.action.admin.DomainSupport$`
- `org.beangle.ems.portal.action.admin.JobModule$`
- `org.beangle.ems.portal.action.admin.LogModule$`
- `org.beangle.ems.portal.action.admin.OAModule$`
- `org.beangle.ems.portal.action.admin.SecurityModule$`
- `org.beangle.ems.portal.action.user.DefaultModule$`

### `org.beangle.ems.ws`（1）

- `org.beangle.ems.ws.WSModule$`

### `org.beangle.event.bus`（2）

- `org.beangle.event.bus.DataEvent`
- `org.beangle.event.bus.DataEventBus`

### `org.beangle.event.mq`（4）

- `org.beangle.event.mq.AbstractChannelQueue`
- `org.beangle.event.mq.ChannelQueue`
- `org.beangle.event.mq.ChannelQueue$`
- `org.beangle.event.mq.EventSerializer`

### `org.beangle.ids.cas`（19）

- `org.beangle.ids.cas.id.ServiceTicketIdGenerator`
- `org.beangle.ids.cas.service.AbstractOAuthService`
- `org.beangle.ids.cas.service.CasAppInfoProvider`
- `org.beangle.ids.cas.service.CasAppInfoProvider$`
- `org.beangle.ids.cas.service.CasService`
- `org.beangle.ids.cas.service.CasService$`
- `org.beangle.ids.cas.service.LoginRetryService`
- `org.beangle.ids.cas.service.LoginRetryService$`
- `org.beangle.ids.cas.service.OAuthService`
- `org.beangle.ids.cas.service.OAuthService$`
- `org.beangle.ids.cas.service.QrcodeService`
- `org.beangle.ids.cas.service.QrcodeService$`
- `org.beangle.ids.cas.service.Services`
- `org.beangle.ids.cas.service.UserMobileProvider`
- `org.beangle.ids.cas.service.UserMobileProvider$`
- `org.beangle.ids.cas.ticket.DefaultServiceTicket`
- `org.beangle.ids.cas.ticket.TicketCacheService`
- `org.beangle.ids.cas.ticket.TicketRegistry`
- `org.beangle.ids.cas.ticket.TicketRegistry$`

### `org.beangle.jdbc.ds`（1）

- `org.beangle.jdbc.ds.DataSourceFactory`

### `org.beangle.notify.sms`（1）

- `org.beangle.notify.sms.SmsCodeService`

### `org.beangle.security.authc`（13）

- `org.beangle.security.authc.AbstractAuthenticator`
- `org.beangle.security.authc.AccountStore`
- `org.beangle.security.authc.AuthenticationListener`
- `org.beangle.security.authc.Authenticator`
- `org.beangle.security.authc.CredentialChecker`
- `org.beangle.security.authc.CredentialChecker$`
- `org.beangle.security.authc.CredentialStore`
- `org.beangle.security.authc.CredentialStore$`
- `org.beangle.security.authc.DBCredentialStore`
- `org.beangle.security.authc.DBCredentialStore$`
- `org.beangle.security.authc.PasswordPolicy`
- `org.beangle.security.authc.PasswordPolicyProvider`
- `org.beangle.security.authc.PasswordPolicyProvider$`

### `org.beangle.security.authz`（3）

- `org.beangle.security.authz.AbstractRoleBasedAuthorizer`
- `org.beangle.security.authz.Authorizer`
- `org.beangle.security.authz.Authorizer$`

### `org.beangle.security.mgt`（2）

- `org.beangle.security.mgt.SecurityManager`
- `org.beangle.security.mgt.SecurityManager$`

### `org.beangle.security.realm`（2）

- `org.beangle.security.realm.Realm`
- `org.beangle.security.realm.ldap.LdapCredentialStore`

### `org.beangle.security.session`（9）

- `org.beangle.security.session.SessionProfileProvider`
- `org.beangle.security.session.SessionProfileProvider$`
- `org.beangle.security.session.SessionRegistry`
- `org.beangle.security.session.SessionRegistry$`
- `org.beangle.security.session.SessionRepo`
- `org.beangle.security.session.SessionRepo$`
- `org.beangle.security.session.cache.CacheSessionRepo`
- `org.beangle.security.session.jdbc.DomainProvider`
- `org.beangle.security.session.jdbc.DomainProvider$`

### `org.beangle.security.web`（11）

- `org.beangle.security.web.EntryPoint`
- `org.beangle.security.web.UrlEntryPoint`
- `org.beangle.security.web.access.AccessDeniedHandler`
- `org.beangle.security.web.access.SecurityContextBuilder`
- `org.beangle.security.web.access.SecurityContextBuilder$`
- `org.beangle.security.web.access.SecurityFilter`
- `org.beangle.security.web.session.CookieSessionIdPolicy`
- `org.beangle.security.web.session.SessionIdPolicy`
- `org.beangle.security.web.session.SessionIdPolicy$`
- `org.beangle.security.web.session.SessionIdReader`
- `org.beangle.security.web.session.SessionIdReader$`

### `org.beangle.serializer.json`（1）

- `org.beangle.serializer.json.JsonDriver$`

### `org.beangle.serializer.text`（1）

- `org.beangle.serializer.text.io.StreamDriver$`

### `org.beangle.serializer.xml`（1）

- `org.beangle.serializer.xml.XmlDriver$`

### `org.beangle.she.hibernate`（1）

- `org.beangle.she.hibernate.OrmModule$`

### `org.beangle.she.webmvc`（7）

- `org.beangle.she.webmvc.EntityAction`
- `org.beangle.she.webmvc.EntityAction$`
- `org.beangle.she.webmvc.ExportSupport`
- `org.beangle.she.webmvc.ExportSupport$`
- `org.beangle.she.webmvc.JsonAPISupport`
- `org.beangle.she.webmvc.JsonAPISupport$`
- `org.beangle.she.webmvc.RestfulAction`

### `org.beangle.template.api`（6）

- `org.beangle.template.api.ComponentContextAware`
- `org.beangle.template.api.ModelBuilder`
- `org.beangle.template.api.TagLibrary`
- `org.beangle.template.api.TagLibrary$`
- `org.beangle.template.api.TagLibraryProvider`
- `org.beangle.template.api.TagTemplateEngine`

### `org.beangle.template.freemarker`（2）

- `org.beangle.template.freemarker.AbstractTemplateEngine`
- `org.beangle.template.freemarker.Configurator`

### `org.beangle.web.servlet`（6）

- `org.beangle.web.servlet.filter.GenericHttpFilter`
- `org.beangle.web.servlet.http.accept.ContentNegotiationManager`
- `org.beangle.web.servlet.intercept.Interceptor`
- `org.beangle.web.servlet.intercept.Interceptor$`
- `org.beangle.web.servlet.security.RequestConvertor`
- `org.beangle.web.servlet.util.CookieGenerator`

### `org.beangle.webmvc`（2）

- `org.beangle.webmvc.DevModule$`
- `org.beangle.webmvc.ViewModule$`

### `org.beangle.webmvc.config`（11）

- `org.beangle.webmvc.config.ActionMapping`
- `org.beangle.webmvc.config.ActionMappingBuilder`
- `org.beangle.webmvc.config.ActionMappingBuilder$`
- `org.beangle.webmvc.config.Buildable`
- `org.beangle.webmvc.config.Buildable$`
- `org.beangle.webmvc.config.Configurator`
- `org.beangle.webmvc.config.Configurator$`
- `org.beangle.webmvc.config.Profile`
- `org.beangle.webmvc.config.ProfileConfig$`
- `org.beangle.webmvc.config.ProfileProvider`
- `org.beangle.webmvc.config.ProfileProvider$`

### `org.beangle.webmvc.context`（3）

- `org.beangle.webmvc.context.ActionContextProperty`
- `org.beangle.webmvc.context.ActionContextProperty$`
- `org.beangle.webmvc.context.LocaleResolver`

### `org.beangle.webmvc.dispatch`（10）

- `org.beangle.webmvc.dispatch.AbstractExceptionHandler`
- `org.beangle.webmvc.dispatch.ActionUriRender`
- `org.beangle.webmvc.dispatch.ActionUriRender$`
- `org.beangle.webmvc.dispatch.ExceptionHandler`
- `org.beangle.webmvc.dispatch.ExceptionHandler$`
- `org.beangle.webmvc.dispatch.RequestMapper`
- `org.beangle.webmvc.dispatch.RequestMapper$`
- `org.beangle.webmvc.dispatch.Route`
- `org.beangle.webmvc.dispatch.RouteProvider`
- `org.beangle.webmvc.dispatch.RouteProvider$`

### `org.beangle.webmvc.execution`（3）

- `org.beangle.webmvc.execution.CacheResult`
- `org.beangle.webmvc.execution.InvokerBuilder`
- `org.beangle.webmvc.execution.ResponseCache`

### `org.beangle.webmvc.i18n`（3）

- `org.beangle.webmvc.i18n.ActionTextCache`
- `org.beangle.webmvc.i18n.TextResourceProvider`
- `org.beangle.webmvc.i18n.TextResourceProvider$`

### `org.beangle.webmvc.support`（11）

- `org.beangle.webmvc.support.ActionSupport`
- `org.beangle.webmvc.support.EntitySupport`
- `org.beangle.webmvc.support.EntitySupport$`
- `org.beangle.webmvc.support.MessageSupport`
- `org.beangle.webmvc.support.MessageSupport$`
- `org.beangle.webmvc.support.ParamSupport`
- `org.beangle.webmvc.support.ParamSupport$`
- `org.beangle.webmvc.support.RouteSupport`
- `org.beangle.webmvc.support.RouteSupport$`
- `org.beangle.webmvc.support.ServletSupport`
- `org.beangle.webmvc.support.ServletSupport$`

### `org.beangle.webmvc.view`（9）

- `org.beangle.webmvc.view.TemplatePathMapper`
- `org.beangle.webmvc.view.TemplateResolver`
- `org.beangle.webmvc.view.TypeViewBuilder`
- `org.beangle.webmvc.view.ViewBuilder`
- `org.beangle.webmvc.view.ViewManager`
- `org.beangle.webmvc.view.ViewManager$`
- `org.beangle.webmvc.view.ViewRender`
- `org.beangle.webmvc.view.ViewResolver`
- `org.beangle.webmvc.view.tag.AbstractTagLibrary`

### `org.h2.Driver`（1）

- `org.h2.Driver`

### `org.hibernate.SessionFactory`（1）

- `org.hibernate.SessionFactory`

### `org.hibernate.boot`（10）

- `org.hibernate.boot.cfgxml.internal.CfgXmlAccessServiceImpl`
- `org.hibernate.boot.internal.Abstract`
- `org.hibernate.boot.internal.AnyKeyType`
- `org.hibernate.boot.internal.CollectionClassification`
- `org.hibernate.boot.internal.Extends`
- `org.hibernate.boot.internal.Target`
- `org.hibernate.boot.models.DialectOverrideAnnotations`
- `org.hibernate.boot.models.HibernateAnnotations`
- `org.hibernate.boot.models.JpaAnnotations`
- `org.hibernate.boot.models.XmlAnnotations`

### `org.hibernate.cache`（6）

- `org.hibernate.cache.internal.BasicCacheKeyImplementation`
- `org.hibernate.cache.internal.EnabledCaching`
- `org.hibernate.cache.jcache.internal.StrategyRegistrationProviderImpl`
- `org.hibernate.cache.spi.entry.CollectionCacheEntry`
- `org.hibernate.cache.spi.entry.StandardCacheEntryImpl`
- `org.hibernate.cache.spi.support.AbstractReadWriteAccess$Item`

### `org.hibernate.engine`（11）

- `org.hibernate.engine.config.internal.ConfigurationServiceImpl`
- `org.hibernate.engine.extension.internal.ExtensionIntegrationServiceImpl`
- `org.hibernate.engine.jdbc.batch.internal.BatchBuilderImpl`
- `org.hibernate.engine.jdbc.dialect.internal.DialectFactoryImpl`
- `org.hibernate.engine.jdbc.dialect.internal.DialectResolverSet`
- `org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentImpl`
- `org.hibernate.engine.jdbc.internal.JdbcServicesImpl`
- `org.hibernate.engine.jdbc.mutation.internal.StandardMutationExecutorService`
- `org.hibernate.engine.jndi.internal.JndiServiceImpl`
- `org.hibernate.engine.query.internal.NativeQueryInterpreterStandardImpl`
- `org.hibernate.engine.spi.PrimeAmongSecondarySupertypes`

### `org.hibernate.internal`（1）

- `org.hibernate.internal.util.cache.InternalCacheFactoryImpl`

### `org.hibernate.loader`（1）

- `org.hibernate.loader.ast.internal.StandardBatchLoaderFactory`

### `org.hibernate.persister`（2）

- `org.hibernate.persister.internal.PersisterFactoryImpl`
- `org.hibernate.persister.internal.StandardPersisterClassResolver`

### `org.hibernate.property`（1）

- `org.hibernate.property.access.internal.PropertyAccessStrategyResolverStandardImpl`

### `org.hibernate.proxy`（2）

- `org.hibernate.proxy.HibernateProxy`
- `org.hibernate.proxy.ProxyConfiguration`

### `org.hibernate.query`（1）

- `org.hibernate.query.sqm.mutation.internal.SqmMultiTableMutationStrategyProviderStandard`

### `org.hibernate.resource`（1）

- `org.hibernate.resource.beans.internal.ManagedBeanRegistryImpl`

### `org.hibernate.service`（1）

- `org.hibernate.service.internal.SessionFactoryServiceRegistryFactoryImpl`

### `org.hibernate.sql`（2）

- `org.hibernate.sql.ast.internal.ParameterMarkerStrategyStandard`
- `org.hibernate.sql.results.jdbc.internal.JdbcValuesMappingProducerProviderStandard`

### `org.hibernate.stat`（1）

- `org.hibernate.stat.internal.StatisticsImpl`

### `org.hibernate.temporal`（1）

- `org.hibernate.temporal.internal.ChangesetCoordinatorImpl`

### `org.slf4j.bridge`（1）

- `org.slf4j.bridge.SLF4JBridgeHandler`

### `org.springframework.aop`（4）

- `org.springframework.aop.framework.AbstractSingletonProxyFactoryBean`
- `org.springframework.aop.framework.Advised`
- `org.springframework.aop.framework.ProxyConfig`
- `org.springframework.aop.scope.ScopedObject`

### `org.springframework.beans`（8）

- `org.springframework.beans.factory.Aware`
- `org.springframework.beans.factory.Aware$`
- `org.springframework.beans.factory.BeanClassLoaderAware`
- `org.springframework.beans.factory.BeanClassLoaderAware$`
- `org.springframework.beans.factory.FactoryBean`
- `org.springframework.beans.factory.FactoryBean$`
- `org.springframework.beans.factory.InitializingBean`
- `org.springframework.beans.factory.InitializingBean$`

### `org.springframework.core`（1）

- `org.springframework.core.DecoratingProxy`

### `org.springframework.transaction`（11）

- `org.springframework.transaction.ConfigurableTransactionManager`
- `org.springframework.transaction.ConfigurableTransactionManager$`
- `org.springframework.transaction.PlatformTransactionManager`
- `org.springframework.transaction.PlatformTransactionManager$`
- `org.springframework.transaction.TransactionExecutionListener`
- `org.springframework.transaction.TransactionManager`
- `org.springframework.transaction.TransactionManager$`
- `org.springframework.transaction.interceptor.TransactionalProxy`
- `org.springframework.transaction.support.AbstractPlatformTransactionManager`
- `org.springframework.transaction.support.ResourceTransactionManager`
- `org.springframework.transaction.support.ResourceTransactionManager$`

## 二、scala 相关（33）

> 无法经 registrar 注册（`jdkPrefixes` 含 `scala.`），需追加到静态 `native/src/main/resources/META-INF/native-image/ems-native/reflect-config.json`。多数为序列化对象图（`JavaSerializationCopier`）或代理/接口探测，class-only 即可；带字段/方法形态的按需注册。

- `scala.Equals` — QAllPublicM, QAllDeclaredM
- `scala.Function1` — QAllDeclaredM
- `scala.Option` — class-only
- `scala.PartialFunction` — QAllDeclaredM
- `scala.Product` — QAllPublicM
- `scala.collection.Iterable` — QAllDeclaredM
- `scala.collection.IterableFactoryDefaults` — QAllDeclaredM
- `scala.collection.IterableOnce` — QAllDeclaredM
- `scala.collection.IterableOnceOps` — QAllDeclaredM
- `scala.collection.IterableOps` — QAllDeclaredM
- `scala.collection.Seq` — QAllDeclaredM
- `scala.collection.SeqOps` — QAllDeclaredM
- `scala.collection.concurrent.CNodeBase` — fields:1
- `scala.collection.concurrent.INodeBase` — fields:1
- `scala.collection.concurrent.MainNode` — fields:1
- `scala.collection.concurrent.TrieMap` — fields:1
- `scala.collection.immutable.Iterable` — QAllDeclaredM
- `scala.collection.immutable.List` — class-only
- `scala.collection.immutable.Map` — class-only
- `scala.collection.immutable.Seq` — QAllDeclaredM
- `scala.collection.immutable.SeqOps` — QAllDeclaredM
- `scala.collection.immutable.Set` — class-only
- `scala.collection.mutable.Buffer` — class-only
- `scala.collection.mutable.HashMap` — class-only
- `scala.collection.mutable.Map` — class-only
- `scala.collection.mutable.Seq` — class-only
- `scala.collection.mutable.Set` — class-only
- `scala.deriving.Mirror` — QAllPublicM
- `scala.deriving.Mirror$Product` — QAllPublicM
- `scala.deriving.Mirror$Singleton` — QAllPublicM
- `scala.math.Ordered` — methods:5
- `scala.reflect.Enum` — QAllPublicM
- `scala.runtime.EnumValue` — QAllPublicM

## 三、JDK 相关缺失（202）

> 已剔除合并配置已覆盖项；按用途分五档。

### 3.1 与 portal 功能直接相关（高优先）（1）

> `java.util.Map$Entry` 是 `select.ftl` `${item[tag.keyName]}` 失败的直接根因
> （FreeMarker 把非 public 匿名类 `HashAdapter$1$1$1` 的 getter 上溯到 public 接口后
> 反射调用）；其数组类 `[Ljava.util.Map$Entry;` 见「四、数组类缺失 4.5」。

- `java.util.Map$Entry` — QAllPublicM, methods:5

### 3.2 序列化/时间相关（缓存与 JDBC 值）（33）

- `java.io.Serializable` — QAllPublicM, QAllDeclaredM
- `java.lang.Enum` — class-only
- `java.lang.Number` — class-only
- `java.math.BigDecimal` — class-only
- `java.math.BigInteger` — class-only
- `java.sql.Blob` — class-only
- `java.sql.Clob` — class-only
- `java.sql.Date` — class-only
- `java.sql.Driver` — class-only
- `java.sql.DriverManager` — class-only
- `java.sql.NClob` — class-only
- `java.sql.SQLException` — fields:1
- `java.sql.Time` — class-only
- `java.sql.Timestamp` — class-only
- `java.sql.Types` — AF
- `java.time.Duration` — class-only
- `java.time.Instant` — class-only
- `java.time.LocalDate` — class-only
- `java.time.LocalDateTime` — class-only
- `java.time.LocalTime` — class-only
- `java.time.OffsetDateTime` — class-only
- `java.time.OffsetTime` — class-only
- `java.time.Ser` — class-only
- `java.time.ZonedDateTime` — class-only
- `java.time.chrono.ChronoLocalDate` — class-only
- `java.time.chrono.ChronoLocalDateTime` — class-only
- `java.time.chrono.ChronoZonedDateTime` — class-only
- `java.time.temporal.Temporal` — class-only
- `java.time.temporal.TemporalAccessor` — class-only
- `java.time.temporal.TemporalAdjuster` — class-only
- `java.util.Date` — class-only
- `java.util.TimeZone` — class-only
- `java.util.UUID` — class-only

### 3.3 JMX/Tomcat 探测（低优先，多为 MBean 注册噪声）（31）

- `com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl` — methods:1
- `com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl` — methods:1
- `java.awt.Component` — QAllPublicM
- `java.awt.Desktop` — methods:3
- `java.awt.MenuContainer` — QAllPublicM
- `java.awt.image.ImageObserver` — QAllPublicM
- `javax.management.MBeanOperationInfo` — QAllPublicM, methods:1
- `javax.management.MBeanServerBuilder` — methods:1
- `javax.management.ObjectName` — class-only
- `javax.management.StandardEmitterMBean` — methods:3
- `javax.management.openmbean.CompositeData` — class-only
- `javax.management.openmbean.OpenMBeanOperationInfoSupport` — class-only
- `javax.management.openmbean.TabularData` — class-only
- `jdk.internal.misc.Unsafe` — class-only
- `jdk.management.jfr.ConfigurationInfo` — QAllPublicM
- `jdk.management.jfr.EventTypeInfo` — QAllPublicM
- `jdk.management.jfr.FlightRecorderMXBean` — QAllPublicM
- `jdk.management.jfr.FlightRecorderMXBeanImpl` — QAllPublicC, methods:4
- `jdk.management.jfr.RecordingInfo` — QAllPublicM
- `jdk.management.jfr.SettingDescriptorInfo` — QAllPublicM
- `sun.java2d.marlin.DMarlinRenderingEngine` — methods:1
- `sun.management.ClassLoadingImpl` — QAllPublicC
- `sun.management.CompilationImpl` — QAllPublicC
- `sun.management.ManagementFactoryHelper$1` — QAllPublicC
- `sun.management.ManagementFactoryHelper$PlatformLoggingImpl` — QAllPublicC
- `sun.management.MemoryImpl` — QAllPublicC
- `sun.management.MemoryManagerImpl` — QAllPublicC
- `sun.management.MemoryPoolImpl` — QAllPublicC
- `sun.management.RuntimeImpl` — QAllPublicC
- `sun.misc.Unsafe` — fields:1
- `sun.rmi.transport.Target` — fields:1

### 3.4 安全/加密（JCE provider 探测）（24）

- `com.sun.crypto.provider.AESCipher$General` — methods:1
- `com.sun.crypto.provider.HmacCore$HmacSHA256` — methods:1
- `com.sun.crypto.provider.PBEKeyFactory$PBEWithMD5AndDES` — methods:1
- `com.sun.crypto.provider.PBEWithMD5AndDESCipher` — methods:1
- `com.sun.crypto.provider.PBKDF2Core$HmacSHA1` — methods:1
- `com.sun.crypto.provider.PBKDF2Core$HmacSHA224` — methods:1
- `com.sun.crypto.provider.PBKDF2Core$HmacSHA256` — methods:1
- `com.sun.crypto.provider.PBKDF2Core$HmacSHA384` — methods:1
- `com.sun.crypto.provider.PBKDF2Core$HmacSHA512` — methods:1
- `java.security.AccessController` — methods:1
- `java.security.Principal` — QAllPublicM, methods:1
- `java.security.ProtectionDomain` — class-only
- `java.security.SecureRandom` — class-only
- `java.security.SecureRandomParameters` — class-only
- `javax.net.ssl.SSLParameters` — methods:1
- `javax.security.auth.Subject` — methods:1
- `sun.security.provider.MD5` — methods:1
- `sun.security.provider.NativePRNG` — methods:2
- `sun.security.provider.SHA` — methods:1
- `sun.security.provider.SHA2$SHA224` — methods:1
- `sun.security.provider.SHA2$SHA256` — methods:1
- `sun.security.provider.SHA5$SHA384` — methods:1
- `sun.security.provider.SHA5$SHA512` — methods:1
- `sun.security.provider.SecureRandom` — methods:2

### 3.5 其余 java.* 接口/工具类（多为代理与探测）（118）

- `com.sun.management.GarbageCollectorMXBean` — QAllPublicM
- `com.sun.management.GcInfo` — QAllPublicM
- `com.sun.management.HotSpotDiagnosticMXBean` — QAllPublicM
- `com.sun.management.ThreadMXBean` — QAllPublicM
- `com.sun.management.UnixOperatingSystemMXBean` — QAllPublicM
- `com.sun.management.VMOption` — QAllPublicM
- `com.sun.management.internal.GarbageCollectorExtImpl` — QAllPublicC
- `com.sun.management.internal.HotSpotDiagnostic` — QAllPublicC
- `com.sun.management.internal.HotSpotThreadImpl` — QAllPublicC
- `com.sun.management.internal.OperatingSystemImpl` — QAllPublicC
- `java.beans.PropertyVetoException` — class-only
- `java.io.Externalizable` — class-only
- `java.lang.Boolean` — fields:1
- `java.lang.Byte` — fields:1
- `java.lang.CharSequence` — class-only
- `java.lang.Character` — fields:1
- `java.lang.Class` — methods:64
- `java.lang.ClassLoader` — methods:1
- `java.lang.ClassValue` — class-only
- `java.lang.Cloneable` — class-only
- `java.lang.Comparable` — class-only
- `java.lang.Deprecated` — QAllPublicM
- `java.lang.Double` — fields:1
- `java.lang.Float` — fields:1
- `java.lang.FunctionalInterface` — class-only
- `java.lang.Integer` — fields:1
- `java.lang.Iterable` — class-only
- `java.lang.Long` — fields:1
- `java.lang.Object` — QAllPublicM, QAllDeclaredM, QAllPublicC, AF, methods:18
- `java.lang.Package` — methods:18
- `java.lang.Runnable` — QAllPublicM, QAllDeclaredM
- `java.lang.Runtime` — methods:5
- `java.lang.SecurityManager` — methods:1
- `java.lang.Short` — fields:1
- `java.lang.StackTraceElement` — QAllPublicM
- `java.lang.String` — methods:1, fields:1
- `java.lang.System` — methods:10
- `java.lang.Thread` — QAllPublicM, QAllPublicC, AF, methods:15, fields:2
- `java.lang.Thread$Builder` — methods:2
- `java.lang.ThreadGroup` — methods:9
- `java.lang.Void` — fields:1
- `java.lang.WrongThreadException` — class-only
- `java.lang.annotation.Annotation` — methods:1
- `java.lang.annotation.Documented` — class-only
- `java.lang.annotation.ElementType` — class-only
- `java.lang.annotation.Inherited` — class-only
- `java.lang.annotation.Retention` — class-only
- `java.lang.annotation.RetentionPolicy` — class-only
- `java.lang.annotation.Target` — class-only
- `java.lang.constant.ClassDesc` — methods:9
- `java.lang.invoke.VarHandle` — methods:1
- `java.lang.management.BufferPoolMXBean` — QAllPublicM
- `java.lang.management.ClassLoadingMXBean` — QAllPublicM
- `java.lang.management.CompilationMXBean` — QAllPublicM
- `java.lang.management.LockInfo` — QAllPublicM
- `java.lang.management.ManagementPermission` — methods:1
- `java.lang.management.MemoryMXBean` — QAllPublicM
- `java.lang.management.MemoryManagerMXBean` — QAllPublicM
- `java.lang.management.MemoryPoolMXBean` — QAllPublicM
- `java.lang.management.MemoryUsage` — QAllPublicM
- `java.lang.management.MonitorInfo` — QAllPublicM
- `java.lang.management.PlatformLoggingMXBean` — QAllPublicM, methods:4
- `java.lang.management.RuntimeMXBean` — QAllPublicM
- `java.lang.management.ThreadInfo` — QAllPublicM
- `java.lang.reflect.AccessibleObject` — QAllPublicM, QAllPublicC, AF, methods:9
- `java.lang.reflect.AnnotatedElement` — methods:7
- `java.lang.reflect.AnnotatedType` — methods:5
- `java.lang.reflect.Constructor` — methods:17
- `java.lang.reflect.Executable` — methods:21
- `java.lang.reflect.Field` — methods:12
- `java.lang.reflect.GenericArrayType` — methods:1
- `java.lang.reflect.GenericDeclaration` — methods:1
- `java.lang.reflect.Member` — methods:4
- `java.lang.reflect.Method` — methods:21
- `java.lang.reflect.Parameter` — methods:16
- `java.lang.reflect.ParameterizedType` — methods:1
- `java.lang.reflect.Type` — methods:1
- `java.lang.reflect.TypeVariable` — methods:4
- `java.net.URI` — methods:27
- `java.net.URL` — methods:13
- `java.util.AbstractMap` — QAllPublicM
- `java.util.ArrayList` — QAllPublicM, methods:1
- `java.util.Collection` — class-only
- `java.util.Comparator` — class-only
- `java.util.Enumeration` — class-only
- `java.util.EventListener` — QAllPublicM, QAllDeclaredM
- `java.util.HashSet` — class-only
- `java.util.List` — class-only
- `java.util.Locale` — class-only
- `java.util.Map` — QAllPublicM
- `java.util.Properties` — methods:1
- `java.util.PropertyPermission` — methods:1
- `java.util.SequencedCollection` — class-only
- `java.util.Set` — class-only
- `java.util.SortedMap` — class-only
- `java.util.SortedSet` — class-only
- `java.util.concurrent.ConcurrentMap` — methods:1
- `java.util.concurrent.ForkJoinTask` — fields:2
- `java.util.concurrent.ScheduledThreadPoolExecutor` — QAllPublicM
- `java.util.concurrent.atomic.AtomicBoolean` — fields:1
- `java.util.concurrent.atomic.AtomicReference` — fields:1
- `java.util.concurrent.atomic.Striped64` — fields:2
- `java.util.function.BiConsumer` — class-only
- `java.util.function.BiFunction` — class-only
- `java.util.function.Consumer` — class-only
- `java.util.function.Function` — class-only
- `java.util.function.Supplier` — class-only
- `java.util.function.SupplierEditor` — class-only
- `java.util.logging.LogManager` — methods:1
- `java.util.logging.LoggingMXBean` — QAllPublicM
- `java.util.logging.SimpleFormatter` — methods:1
- `javax.sql.DataSource` — class-only
- `javax.xml.datatype.Duration` — class-only

## 四、数组类缺失（52）

> `Array.newInstance` 反射创建数组需注册数组类（reflect-config 的 `name` 用 JVM 内部名，如 `[Ljava.lang.String;`）；`unsafeAllocated` 视分配路径按需加。元素类另需其自身注册。

### 4.1 原始类型数组（8）

- `[B` — class-only
- `[C` — class-only
- `[D` — class-only
- `[F` — class-only
- `[I` — class-only
- `[J` — class-only
- `[S` — class-only
- `[Z` — class-only

### 4.2 应用/框架对象数组（28）

- `[Lorg.beangle.commons.bean.meta.MetaModel$Ctor;` — class-only
- `[Lorg.beangle.commons.bean.meta.MetaModel$Param;` — class-only
- `[Lorg.beangle.commons.lang.reflect.TypeInfo;` — class-only
- `[Lorg.beangle.web.servlet.intercept.Interceptor;` — class-only
- `[Lorg.beangle.webmvc.context.Argument;` — class-only
- `[Lorg.beangle.webmvc.dispatch.Route;` — class-only
- `[Lorg.beangle.webmvc.view.ViewDecorator;` — class-only
- `[Lorg.hibernate.event.spi.AutoFlushEventListener;` — class-only
- `[Lorg.hibernate.event.spi.DeleteEventListener;` — class-only
- `[Lorg.hibernate.event.spi.DirtyCheckEventListener;` — class-only
- `[Lorg.hibernate.event.spi.EvictEventListener;` — class-only
- `[Lorg.hibernate.event.spi.FlushEntityEventListener;` — class-only
- `[Lorg.hibernate.event.spi.FlushEventListener;` — class-only
- `[Lorg.hibernate.event.spi.InitializeCollectionEventListener;` — class-only
- `[Lorg.hibernate.event.spi.LoadEventListener;` — class-only
- `[Lorg.hibernate.event.spi.LockEventListener;` — class-only
- `[Lorg.hibernate.event.spi.MergeEventListener;` — class-only
- `[Lorg.hibernate.event.spi.PersistEventListener;` — class-only
- `[Lorg.hibernate.event.spi.PostDeleteEventListener;` — class-only
- `[Lorg.hibernate.event.spi.PostInsertEventListener;` — class-only
- `[Lorg.hibernate.event.spi.PostLoadEventListener;` — class-only
- `[Lorg.hibernate.event.spi.PostUpdateEventListener;` — class-only
- `[Lorg.hibernate.event.spi.PostUpsertEventListener;` — class-only
- `[Lorg.hibernate.event.spi.PreFlushEventListener;` — class-only
- `[Lorg.hibernate.event.spi.PreLoadEventListener;` — class-only
- `[Lorg.hibernate.event.spi.RefreshEventListener;` — class-only
- `[Lorg.hibernate.event.spi.ReplicateEventListener;` — class-only
- `[Lorg.springframework.util.ConcurrentReferenceHashMap$Segment;` — class-only

### 4.3 scala 对象数组（1）

- `[Lscala.Tuple2;` — class-only

### 4.4 多维数组（1）

- `[[Ljava.lang.Object;` — class-only

### 4.5 JDK 对象数组（14）

- `[Lcom.zaxxer.hikari.util.ConcurrentBag$IConcurrentBagEntry;` — class-only
- `[Lfreemarker.cache.TemplateLoader;` — class-only
- `[Ljava.beans.PropertyDescriptor;` — class-only
- `[Ljava.io.Serializable;` — class-only
- `[Ljava.lang.Class;` — class-only
- `[Ljava.lang.Integer;` — class-only
- `[Ljava.lang.Long;` — class-only
- `[Ljava.lang.Object;` — class-only
- `[Ljava.lang.String;` — class-only
- `[Ljava.lang.reflect.Method;` — class-only
- `[Ljava.net.URL;` — class-only
- `[Ljava.sql.Statement;` — class-only
- `[Ljava.util.Map$Entry;` — class-only
- `[Ljavax.management.openmbean.CompositeData;` — class-only
