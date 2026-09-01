# 反射注册收敛清单

> 基于 native-image-agent 采集（2026-08-31）与现有 reflect-config 对比
> 已去除：JPA 元模型噪声、BeanInfo/Customizer 噪声、JDK class-only、数组 class-only
> 缺失（需注册）: 731 个类

## 一、beangle 应用类（419）

| 类名 | 反射形态 |
|---|---|
| `org.beangle.bui.AbstractTextBean` | DF, methods:16 |
| `org.beangle.bui.ActionClosingUIBean` | DF |
| `org.beangle.bui.Anchor` | DF, methods:7 |
| `org.beangle.bui.Anchor$` | fields:1 |
| `org.beangle.bui.BeangleModels` | AF, methods:26 |
| `org.beangle.bui.BeangleTagLibrary` | methods:2 |
| `org.beangle.bui.Date` | DF, methods:21 |
| `org.beangle.bui.Date$` | fields:1 |
| `org.beangle.bui.DefaultModule` | methods:1 |
| `org.beangle.bui.DefaultModule$` | class-only |
| `org.beangle.bui.Div` | DF, methods:5 |
| `org.beangle.bui.Div$` | class-only |
| `org.beangle.bui.Foot` | methods:1 |
| `org.beangle.bui.Form` | DF, methods:18 |
| `org.beangle.bui.Form$` | class-only |
| `org.beangle.bui.Grid` | DF, methods:26 |
| `org.beangle.bui.Grid$` | fields:1 |
| `org.beangle.bui.Grid$Bar` | DF, methods:2 |
| `org.beangle.bui.Grid$Bar$` | class-only |
| `org.beangle.bui.Grid$Boxcol` | AF, methods:3 |
| `org.beangle.bui.Grid$Col` | DF, methods:15 |
| `org.beangle.bui.Grid$Col$` | class-only |
| `org.beangle.bui.Grid$Row` | AF, methods:2 |
| `org.beangle.bui.Grid$Treecol` | DF, methods:1 |
| `org.beangle.bui.Grid$Treecol$` | class-only |
| `org.beangle.bui.HairLine` | methods:1 |
| `org.beangle.bui.Head` | AF, methods:3 |
| `org.beangle.bui.Messages` | DF, methods:7 |
| `org.beangle.bui.Messages$` | class-only |
| `org.beangle.bui.Nav` | DF, methods:1 |
| `org.beangle.bui.Nav$` | class-only |
| `org.beangle.bui.Navitem` | DF, methods:9 |
| `org.beangle.bui.Navitem$` | class-only |
| `org.beangle.bui.Select` | DF, methods:35 |
| `org.beangle.bui.Select$` | class-only |
| `org.beangle.bui.Submit` | DF, methods:11 |
| `org.beangle.bui.Submit$` | class-only |
| `org.beangle.bui.Textfield` | AF, DF, methods:1 |
| `org.beangle.bui.Textfield$` | class-only |
| `org.beangle.bui.Textfields` | DF, methods:5 |
| `org.beangle.bui.Textfields$` | class-only |
| `org.beangle.bui.Toolbar` | DF, methods:3 |
| `org.beangle.bui.Toolbar$` | class-only |
| `org.beangle.bui.WebUIBean` | class-only |
| `org.beangle.cache.AbstractCacheManager` | methods:1 |
| `org.beangle.cache.redis.RedisClientFactory` | DF, methods:2 |
| `org.beangle.cache.redis.RedisClientFactory$` | fields:1 |
| `org.beangle.cdi.config.ContainerEventMulticaster` | DF, methods:4 |
| `org.beangle.cdi.config.ContainerEventMulticaster$` | class-only |
| `org.beangle.cdi.spring.FactoryBeanProxy` | DF, methods:7 |
| `org.beangle.cdi.spring.FactoryBeanProxy$` | class-only |
| `org.beangle.commons.activation.MediaType` | class-only |
| `org.beangle.commons.aot.AotHintRegistrar` | fields:1 |
| `org.beangle.commons.bean.Disposable` | DF |
| `org.beangle.commons.bean.Disposable$` | class-only |
| `org.beangle.commons.bean.Factory` | DF, methods:1 |
| `org.beangle.commons.bean.Factory$` | fields:1 |
| `org.beangle.commons.bean.Initializing` | DF |
| `org.beangle.commons.bean.Initializing$` | class-only |
| `org.beangle.commons.bean.Refreshable` | DF |
| `org.beangle.commons.bean.Refreshable$` | class-only |
| `org.beangle.commons.bean.Scheduled` | DF |
| `org.beangle.commons.bean.Scheduled$` | class-only |
| `org.beangle.commons.bean.meta.MetaModels$` | fields:1 |
| `org.beangle.commons.cache.Cache` | class-only |
| `org.beangle.commons.cache.CacheManager` | class-only |
| `org.beangle.commons.cdi.CdiEventListener` | DF |
| `org.beangle.commons.cdi.CdiEventListener$` | class-only |
| `org.beangle.commons.cdi.Container` | class-only |
| `org.beangle.commons.collection.page.Page` | class-only |
| `org.beangle.commons.collection.page.SinglePage` | DF, methods:6 |
| `org.beangle.commons.collection.page.SinglePage$` | class-only |
| `org.beangle.commons.config.XmlConfigs` | class-only |
| `org.beangle.commons.conversion.string.BooleanConverter$` | class-only |
| `org.beangle.commons.conversion.string.DurationConverter$` | class-only |
| `org.beangle.commons.conversion.string.LocaleConverter$` | class-only |
| `org.beangle.commons.conversion.string.TimeConverter$` | class-only |
| `org.beangle.commons.conversion.string.ToStringConverter$` | class-only |
| `org.beangle.commons.event.DefaultEventMulticaster` | DF, methods:2 |
| `org.beangle.commons.event.EventListener` | DF |
| `org.beangle.commons.event.EventListener$` | class-only |
| `org.beangle.commons.event.EventMulticaster` | DF |
| `org.beangle.commons.event.EventMulticaster$` | class-only |
| `org.beangle.commons.event.EventPublisher` | DF |
| `org.beangle.commons.event.EventPublisher$` | class-only |
| `org.beangle.commons.io.BinarySerializer` | class-only |
| `org.beangle.commons.io.Serializer` | class-only |
| `org.beangle.commons.json.JsonArray` | class-only |
| `org.beangle.commons.json.JsonObject` | class-only |
| `org.beangle.commons.lang.JVM$` | fields:2 |
| `org.beangle.commons.script.ExprEvaluator` | class-only |
| `org.beangle.commons.text.i18n.DefaultTextBundleLoader` | methods:1, fields:1 |
| `org.beangle.commons.text.i18n.DefaultTextFormatter` | methods:1 |
| `org.beangle.commons.text.i18n.TextBundleLoader` | DF |
| `org.beangle.commons.text.i18n.TextBundleLoader$` | class-only |
| `org.beangle.commons.text.i18n.TextFormatter` | class-only |
| `org.beangle.data.dao.EntityDao` | DF, methods:11 |
| `org.beangle.data.dao.EntityDao$` | class-only |
| `org.beangle.data.dao.OqlBuilder` | class-only |
| `org.beangle.data.hibernate.HibernateEntityDao` | DF, methods:3 |
| `org.beangle.data.hibernate.HibernateEntityDao$` | class-only |
| `org.beangle.data.hibernate.ScalaPropertyAccessStrategy` | methods:1 |
| `org.beangle.data.hibernate.SpringSessionContext` | methods:1 |
| `org.beangle.data.hibernate.cfg.BindMetadataBuilderFactory` | class-only |
| `org.beangle.data.hibernate.format.BeangleJsonFormatMapper` | methods:2 |
| `org.beangle.data.hibernate.format.BeangleXmlFormatMapper` | methods:2 |
| `org.beangle.data.hibernate.id.AutoIncrementGenerator` | methods:1 |
| `org.beangle.data.hibernate.id.DateTimeStyleGenerator` | methods:1 |
| `org.beangle.data.hibernate.proxy.PrebuiltProxyProvider` | class-only |
| `org.beangle.data.hibernate.udt.BagType` | methods:1 |
| `org.beangle.data.hibernate.udt.MapType` | methods:1 |
| `org.beangle.data.hibernate.udt.SetType` | methods:1 |
| `org.beangle.data.model.Entity` | methods:1 |
| `org.beangle.data.model.IntId` | class-only |
| `org.beangle.data.model.IntIdEntity` | class-only |
| `org.beangle.data.model.LongId` | class-only |
| `org.beangle.data.model.LongIdEntity` | class-only |
| `org.beangle.data.model.NumId` | methods:4 |
| `org.beangle.data.model.StringId` | methods:4 |
| `org.beangle.data.model.annotation.log` | class-only |
| `org.beangle.data.model.meta.Domain` | class-only |
| `org.beangle.data.model.pojo.Coded` | class-only |
| `org.beangle.data.model.pojo.Enabled` | class-only |
| `org.beangle.data.model.pojo.Hierarchical` | methods:3 |
| `org.beangle.data.model.pojo.Named` | class-only |
| `org.beangle.data.model.pojo.Remark` | class-only |
| `org.beangle.data.model.pojo.TemporalOn` | methods:3 |
| `org.beangle.data.model.pojo.Updatable` | class-only |
| `org.beangle.data.orm.AbstractDaoTask` | methods:2 |
| `org.beangle.data.orm.MappingModule$` | fields:1 |
| `org.beangle.ems.app.log.Appender` | class-only |
| `org.beangle.ems.app.log.BusinessLogger` | DF |
| `org.beangle.ems.app.log.BusinessLogger$` | fields:1 |
| `org.beangle.ems.app.log.ErrorLogger` | DF |
| `org.beangle.ems.app.log.ErrorLogger$` | class-only |
| `org.beangle.ems.app.oa.Flows$Attachment` | class-only |
| `org.beangle.ems.app.oa.Flows$Payload` | class-only |
| `org.beangle.ems.cas.action.DefaultModule$` | class-only |
| `org.beangle.ems.core.CacheModule$` | class-only |
| `org.beangle.ems.core.CronModule$` | class-only |
| `org.beangle.ems.core.EventModule$` | class-only |
| `org.beangle.ems.core.LogModule$` | class-only |
| `org.beangle.ems.core.SmsModule$` | class-only |
| `org.beangle.ems.core.WebReconfigModule$` | class-only |
| `org.beangle.ems.core.cas.CredentialModule$` | class-only |
| `org.beangle.ems.core.cas.SeurityModule$` | class-only |
| `org.beangle.ems.core.cas.TicketModule$` | class-only |
| `org.beangle.ems.core.config.model.HasEnvIds` | methods:3 |
| `org.beangle.ems.core.config.model.LocaleTitle` | methods:1 |
| `org.beangle.ems.core.config.service.AppService` | DF, methods:4 |
| `org.beangle.ems.core.config.service.AppService$` | class-only |
| `org.beangle.ems.core.config.service.CredentialService` | DF, methods:1 |
| `org.beangle.ems.core.config.service.CredentialService$` | class-only |
| `org.beangle.ems.core.config.service.DataSourceManager` | class-only |
| `org.beangle.ems.core.config.service.DbService` | DF, methods:1 |
| `org.beangle.ems.core.config.service.DbService$` | class-only |
| `org.beangle.ems.core.config.service.DefaultModule$` | class-only |
| `org.beangle.ems.core.config.service.DomainService` | DF, methods:2 |
| `org.beangle.ems.core.config.service.DomainService$` | class-only |
| `org.beangle.ems.core.log.model.AppLogEntry` | methods:2 |
| `org.beangle.ems.core.log.service.LogDbAppender` | DF, methods:1 |
| `org.beangle.ems.core.log.service.LogDbAppender$` | class-only |
| `org.beangle.ems.core.oa.service.DefaultModule$` | class-only |
| `org.beangle.ems.core.oa.service.DocService` | DF |
| `org.beangle.ems.core.oa.service.DocService$` | class-only |
| `org.beangle.ems.core.oa.service.FlowService` | DF |
| `org.beangle.ems.core.oa.service.FlowService$` | class-only |
| `org.beangle.ems.core.oa.service.MessageService` | DF |
| `org.beangle.ems.core.oa.service.MessageService$` | class-only |
| `org.beangle.ems.core.oa.service.TodoService` | DF |
| `org.beangle.ems.core.oa.service.TodoService$` | class-only |
| `org.beangle.ems.core.security.model.SessionInfo` | DF, methods:20 |
| `org.beangle.ems.core.security.model.SessionInfo$` | class-only |
| `org.beangle.ems.core.security.service.DefaultModule$` | class-only |
| `org.beangle.ems.core.security.service.FuncPermissionService` | DF |
| `org.beangle.ems.core.security.service.FuncPermissionService$` | class-only |
| `org.beangle.ems.core.security.service.MenuService` | DF |
| `org.beangle.ems.core.security.service.MenuService$` | class-only |
| `org.beangle.ems.core.security.service.ProfileService` | DF |
| `org.beangle.ems.core.security.service.ProfileService$` | class-only |
| `org.beangle.ems.core.security.service.SessionInfoService` | DF |
| `org.beangle.ems.core.security.service.SessionInfoService$` | class-only |
| `org.beangle.ems.core.user.service.AvatarService` | DF |
| `org.beangle.ems.core.user.service.AvatarService$` | class-only |
| `org.beangle.ems.core.user.service.DefaultModule$` | class-only |
| `org.beangle.ems.core.user.service.DimensionService` | DF, methods:2 |
| `org.beangle.ems.core.user.service.DimensionService$` | class-only |
| `org.beangle.ems.core.user.service.PasswordConfigService` | DF |
| `org.beangle.ems.core.user.service.PasswordConfigService$` | class-only |
| `org.beangle.ems.core.user.service.RoleService` | DF |
| `org.beangle.ems.core.user.service.RoleService$` | class-only |
| `org.beangle.ems.core.user.service.UserService` | DF, methods:1 |
| `org.beangle.ems.core.user.service.UserService$` | class-only |
| `org.beangle.ems.index.action.DefaultModule$` | class-only |
| `org.beangle.ems.nativeapp.EmsMapping$` | fields:1 |
| `org.beangle.ems.nativeapp.TestUser` | methods:7 |
| `org.beangle.ems.nativeapp.TestUser$HibernateProxy` | class-only |
| `org.beangle.ems.portal.action.DefaultModule$` | class-only |
| `org.beangle.ems.portal.action.admin.BlobModule$` | class-only |
| `org.beangle.ems.portal.action.admin.ConfigModule$` | class-only |
| `org.beangle.ems.portal.action.admin.DomainSupport` | DF |
| `org.beangle.ems.portal.action.admin.DomainSupport$` | class-only |
| `org.beangle.ems.portal.action.admin.JobModule$` | class-only |
| `org.beangle.ems.portal.action.admin.LogModule$` | class-only |
| `org.beangle.ems.portal.action.admin.OAModule$` | class-only |
| `org.beangle.ems.portal.action.admin.SecurityModule$` | class-only |
| `org.beangle.ems.portal.action.user.DefaultModule$` | class-only |
| `org.beangle.ems.ws.WSModule$` | class-only |
| `org.beangle.event.bus.DataEvent` | class-only |
| `org.beangle.event.bus.DataEventBus` | class-only |
| `org.beangle.event.mq.AbstractChannelQueue` | methods:4, fields:1 |
| `org.beangle.event.mq.ChannelQueue` | DF |
| `org.beangle.event.mq.ChannelQueue$` | class-only |
| `org.beangle.event.mq.EventSerializer` | class-only |
| `org.beangle.ids.cas.id.ServiceTicketIdGenerator` | class-only |
| `org.beangle.ids.cas.service.AbstractOAuthService` | methods:5 |
| `org.beangle.ids.cas.service.CasAppInfoProvider` | DF |
| `org.beangle.ids.cas.service.CasAppInfoProvider$` | class-only |
| `org.beangle.ids.cas.service.CasService` | DF |
| `org.beangle.ids.cas.service.CasService$` | class-only |
| `org.beangle.ids.cas.service.LoginRetryService` | DF |
| `org.beangle.ids.cas.service.LoginRetryService$` | class-only |
| `org.beangle.ids.cas.service.OAuthService` | DF |
| `org.beangle.ids.cas.service.OAuthService$` | class-only |
| `org.beangle.ids.cas.service.QrcodeService` | DF |
| `org.beangle.ids.cas.service.QrcodeService$` | class-only |
| `org.beangle.ids.cas.service.Services` | class-only |
| `org.beangle.ids.cas.service.UserMobileProvider` | DF |
| `org.beangle.ids.cas.service.UserMobileProvider$` | fields:1 |
| `org.beangle.ids.cas.ticket.DefaultServiceTicket` | class-only |
| `org.beangle.ids.cas.ticket.TicketCacheService` | class-only |
| `org.beangle.ids.cas.ticket.TicketRegistry` | DF |
| `org.beangle.ids.cas.ticket.TicketRegistry$` | class-only |
| `org.beangle.jdbc.ds.DataSourceFactory` | methods:16 |
| `org.beangle.notify.sms.SmsCodeService` | class-only |
| `org.beangle.sas.engine.tomcat.EmbeddedClassLoader` | class-only |
| `org.beangle.sas.engine.tomcat.SwallowErrorValve` | methods:1 |
| `org.beangle.security.authc.AbstractAuthenticator` | methods:2 |
| `org.beangle.security.authc.AccountStore` | class-only |
| `org.beangle.security.authc.AuthenticationListener` | class-only |
| `org.beangle.security.authc.Authenticator` | class-only |
| `org.beangle.security.authc.CredentialChecker` | DF |
| `org.beangle.security.authc.CredentialChecker$` | class-only |
| `org.beangle.security.authc.CredentialStore` | DF |
| `org.beangle.security.authc.CredentialStore$` | class-only |
| `org.beangle.security.authc.DBCredentialStore` | DF |
| `org.beangle.security.authc.DBCredentialStore$` | class-only |
| `org.beangle.security.authc.PasswordPolicy` | class-only |
| `org.beangle.security.authc.PasswordPolicyProvider` | DF, methods:1 |
| `org.beangle.security.authc.PasswordPolicyProvider$` | class-only |
| `org.beangle.security.authz.AbstractRoleBasedAuthorizer` | methods:2 |
| `org.beangle.security.authz.Authorizer` | DF |
| `org.beangle.security.authz.Authorizer$` | class-only |
| `org.beangle.security.mgt.SecurityManager` | DF |
| `org.beangle.security.mgt.SecurityManager$` | class-only |
| `org.beangle.security.realm.Realm` | class-only |
| `org.beangle.security.realm.ldap.LdapCredentialStore` | class-only |
| `org.beangle.security.session.SessionProfileProvider` | DF |
| `org.beangle.security.session.SessionProfileProvider$` | class-only |
| `org.beangle.security.session.SessionRegistry` | DF |
| `org.beangle.security.session.SessionRegistry$` | class-only |
| `org.beangle.security.session.SessionRepo` | DF |
| `org.beangle.security.session.SessionRepo$` | class-only |
| `org.beangle.security.session.cache.CacheSessionRepo` | methods:3, fields:2 |
| `org.beangle.security.session.jdbc.DomainProvider` | DF, methods:1 |
| `org.beangle.security.session.jdbc.DomainProvider$` | class-only |
| `org.beangle.security.web.EntryPoint` | class-only |
| `org.beangle.security.web.UrlEntryPoint` | methods:1 |
| `org.beangle.security.web.access.AccessDeniedHandler` | class-only |
| `org.beangle.security.web.access.SecurityContextBuilder` | DF |
| `org.beangle.security.web.access.SecurityContextBuilder$` | class-only |
| `org.beangle.security.web.access.SecurityFilter` | class-only |
| `org.beangle.security.web.session.CookieSessionIdPolicy` | methods:1 |
| `org.beangle.security.web.session.SessionIdPolicy` | DF |
| `org.beangle.security.web.session.SessionIdPolicy$` | class-only |
| `org.beangle.security.web.session.SessionIdReader` | DF |
| `org.beangle.security.web.session.SessionIdReader$` | fields:1 |
| `org.beangle.serializer.json.DefaultJsonDriver` | methods:1 |
| `org.beangle.serializer.json.DefaultJsonpDriver` | methods:1 |
| `org.beangle.serializer.json.DefaultModule` | class-only |
| `org.beangle.serializer.json.DefaultModule$` | fields:1 |
| `org.beangle.serializer.json.JsonDriver` | DF |
| `org.beangle.serializer.json.JsonDriver$` | class-only |
| `org.beangle.serializer.json.JsonSerializer` | methods:5 |
| `org.beangle.serializer.json.JsonpSerializer` | methods:5 |
| `org.beangle.serializer.text.AbstractSerializer` | methods:1 |
| `org.beangle.serializer.text.DefaultModule` | class-only |
| `org.beangle.serializer.text.DefaultModule$` | fields:1 |
| `org.beangle.serializer.text.io.AbstractDriver` | methods:3 |
| `org.beangle.serializer.text.io.StreamDriver` | DF |
| `org.beangle.serializer.text.io.StreamDriver$` | class-only |
| `org.beangle.serializer.text.mapper.DefaultMapper` | methods:2 |
| `org.beangle.serializer.text.mapper.Mapper` | class-only |
| `org.beangle.serializer.text.marshal.DefaultMarshallerRegistry` | methods:1 |
| `org.beangle.serializer.text.marshal.MarshallerRegistry` | class-only |
| `org.beangle.serializer.xml.DefaultModule` | class-only |
| `org.beangle.serializer.xml.DefaultModule$` | fields:1 |
| `org.beangle.serializer.xml.DomDriver` | methods:1 |
| `org.beangle.serializer.xml.XmlDriver` | DF |
| `org.beangle.serializer.xml.XmlDriver$` | class-only |
| `org.beangle.serializer.xml.XmlSerializer` | methods:5 |
| `org.beangle.she.config.CleanupInitializer` | methods:1 |
| `org.beangle.she.config.ConfigInitializer` | methods:1 |
| `org.beangle.she.hibernate.OrmModule$` | class-only |
| `org.beangle.she.inspect.DefaultModule` | class-only |
| `org.beangle.she.inspect.DefaultModule$` | fields:1 |
| `org.beangle.she.spring.ContainerInitializer` | methods:1 |
| `org.beangle.she.webmvc.EntityAction` | DF, methods:2 |
| `org.beangle.she.webmvc.EntityAction$` | class-only |
| `org.beangle.she.webmvc.ExportSupport` | DF |
| `org.beangle.she.webmvc.ExportSupport$` | class-only |
| `org.beangle.she.webmvc.JsonAPISupport` | DF |
| `org.beangle.she.webmvc.JsonAPISupport$` | class-only |
| `org.beangle.she.webmvc.RestfulAction` | methods:97, fields:7 |
| `org.beangle.she.webmvc.WebmvcInitializer` | methods:1 |
| `org.beangle.template.api.AbstractModels` | class-only |
| `org.beangle.template.api.ClosingUIBean` | DF, methods:2 |
| `org.beangle.template.api.Component` | DF, methods:5 |
| `org.beangle.template.api.ComponentContextAware` | class-only |
| `org.beangle.template.api.IterableUIBean` | class-only |
| `org.beangle.template.api.ModelBuilder` | class-only |
| `org.beangle.template.api.TagLibrary` | DF |
| `org.beangle.template.api.TagLibrary$` | class-only |
| `org.beangle.template.api.TagLibraryProvider` | class-only |
| `org.beangle.template.api.TagTemplateEngine` | class-only |
| `org.beangle.template.api.UIBean` | DF, methods:4 |
| `org.beangle.template.freemarker.AbstractTemplateEngine` | methods:1 |
| `org.beangle.template.freemarker.Configurator` | methods:9 |
| `org.beangle.web.servlet.filter.GenericHttpFilter` | methods:3 |
| `org.beangle.web.servlet.http.accept.ContentNegotiationManager` | class-only |
| `org.beangle.web.servlet.http.accept.ContentNegotiationManagerFactory` | methods:13 |
| `org.beangle.web.servlet.intercept.Interceptor` | DF |
| `org.beangle.web.servlet.intercept.Interceptor$` | class-only |
| `org.beangle.web.servlet.security.RequestConvertor` | class-only |
| `org.beangle.web.servlet.util.CookieGenerator` | methods:13 |
| `org.beangle.webmvc.DefaultModule` | class-only |
| `org.beangle.webmvc.DefaultModule$` | fields:1 |
| `org.beangle.webmvc.DevModule` | class-only |
| `org.beangle.webmvc.DevModule$` | class-only |
| `org.beangle.webmvc.ViewModule` | methods:1 |
| `org.beangle.webmvc.ViewModule$` | class-only |
| `org.beangle.webmvc.asset.Static` | AF, methods:2 |
| `org.beangle.webmvc.asset.StaticFactory` | methods:10 |
| `org.beangle.webmvc.asset.StaticResourceRouteProvider` | methods:5 |
| `org.beangle.webmvc.config.ActionMapping` | class-only |
| `org.beangle.webmvc.config.ActionMappingBuilder` | DF |
| `org.beangle.webmvc.config.ActionMappingBuilder$` | class-only |
| `org.beangle.webmvc.config.Buildable` | DF |
| `org.beangle.webmvc.config.Buildable$` | fields:1 |
| `org.beangle.webmvc.config.Configurator` | DF |
| `org.beangle.webmvc.config.Configurator$` | class-only |
| `org.beangle.webmvc.config.DefaultActionMappingBuilder` | methods:7 |
| `org.beangle.webmvc.config.DefaultConfigurator` | methods:9 |
| `org.beangle.webmvc.config.Profile` | class-only |
| `org.beangle.webmvc.config.ProfileConfig` | DF, methods:23 |
| `org.beangle.webmvc.config.ProfileConfig$` | class-only |
| `org.beangle.webmvc.config.ProfileProvider` | DF |
| `org.beangle.webmvc.config.ProfileProvider$` | class-only |
| `org.beangle.webmvc.config.XmlProfileProvider` | methods:5, fields:1 |
| `org.beangle.webmvc.context.AcceptTypeContextProperty` | methods:4 |
| `org.beangle.webmvc.context.ActionContextProperty` | DF |
| `org.beangle.webmvc.context.ActionContextProperty$` | class-only |
| `org.beangle.webmvc.context.DefaultActionContextBuilder` | methods:1 |
| `org.beangle.webmvc.context.LocaleContextProperty` | methods:4 |
| `org.beangle.webmvc.context.LocaleResolver` | class-only |
| `org.beangle.webmvc.context.ParamLocaleResolver` | methods:4 |
| `org.beangle.webmvc.context.TextResourceContextProperty` | methods:4 |
| `org.beangle.webmvc.dispatch.AbstractExceptionHandler` | methods:7 |
| `org.beangle.webmvc.dispatch.ActionUriRender` | DF |
| `org.beangle.webmvc.dispatch.ActionUriRender$` | class-only |
| `org.beangle.webmvc.dispatch.DefaultActionUriRender` | methods:3 |
| `org.beangle.webmvc.dispatch.DefaultExceptionHandler` | methods:1 |
| `org.beangle.webmvc.dispatch.DefaultRouteProvider` | methods:10 |
| `org.beangle.webmvc.dispatch.ExceptionHandler` | DF |
| `org.beangle.webmvc.dispatch.ExceptionHandler$` | class-only |
| `org.beangle.webmvc.dispatch.HierarchicalUrlMapper` | methods:3 |
| `org.beangle.webmvc.dispatch.MvcRequestConvertor` | methods:1 |
| `org.beangle.webmvc.dispatch.RequestMapper` | DF |
| `org.beangle.webmvc.dispatch.RequestMapper$` | class-only |
| `org.beangle.webmvc.dispatch.Route` | class-only |
| `org.beangle.webmvc.dispatch.RouteProvider` | DF |
| `org.beangle.webmvc.dispatch.RouteProvider$` | class-only |
| `org.beangle.webmvc.execution.CacheResult` | class-only |
| `org.beangle.webmvc.execution.DynaMethodInvokerBuilder` | methods:1 |
| `org.beangle.webmvc.execution.InvokerBuilder` | class-only |
| `org.beangle.webmvc.execution.ResponseCache` | class-only |
| `org.beangle.webmvc.execution.interceptors.CorsInterceptor` | methods:15 |
| `org.beangle.webmvc.i18n.ActionTextCache` | fields:1 |
| `org.beangle.webmvc.i18n.ActionTextResourceProvider` | methods:6 |
| `org.beangle.webmvc.i18n.TextResourceProvider` | DF |
| `org.beangle.webmvc.i18n.TextResourceProvider$` | class-only |
| `org.beangle.webmvc.support.ActionSupport` | methods:93, fields:3 |
| `org.beangle.webmvc.support.EntitySupport` | DF |
| `org.beangle.webmvc.support.EntitySupport$` | class-only |
| `org.beangle.webmvc.support.MessageSupport` | DF |
| `org.beangle.webmvc.support.MessageSupport$` | fields:1 |
| `org.beangle.webmvc.support.ParamSupport` | DF |
| `org.beangle.webmvc.support.ParamSupport$` | class-only |
| `org.beangle.webmvc.support.RouteSupport` | DF |
| `org.beangle.webmvc.support.RouteSupport$` | class-only |
| `org.beangle.webmvc.support.ServletSupport` | DF |
| `org.beangle.webmvc.support.ServletSupport$` | class-only |
| `org.beangle.webmvc.view.DefaultTemplatePathMapper` | methods:1 |
| `org.beangle.webmvc.view.DefaultViewBuilder` | methods:1 |
| `org.beangle.webmvc.view.DefaultViewManager` | methods:8 |
| `org.beangle.webmvc.view.ForwardActionViewBuilder` | methods:2 |
| `org.beangle.webmvc.view.ForwardActionViewRender` | methods:3 |
| `org.beangle.webmvc.view.RedirectActionViewBuilder` | methods:2 |
| `org.beangle.webmvc.view.RedirectActionViewRender` | methods:3 |
| `org.beangle.webmvc.view.TemplatePathMapper` | class-only |
| `org.beangle.webmvc.view.TemplateResolver` | class-only |
| `org.beangle.webmvc.view.TypeViewBuilder` | class-only |
| `org.beangle.webmvc.view.ViewBuilder` | class-only |
| `org.beangle.webmvc.view.ViewManager` | DF |
| `org.beangle.webmvc.view.ViewManager$` | class-only |
| `org.beangle.webmvc.view.ViewRender` | class-only |
| `org.beangle.webmvc.view.ViewResolver` | class-only |
| `org.beangle.webmvc.view.tag.AbstractTagLibrary` | fields:1 |
| `org.beangle.webmvc.view.tag.CoreModels` | methods:5 |

## 二、框架类（227）

| 类名 | 反射形态 |
|---|---|
| `ch.qos.logback.classic.encoder.PatternLayoutEncoder` | methods:1 |
| `ch.qos.logback.classic.util.DefaultJoranConfigurator` | methods:1 |
| `ch.qos.logback.core.ConsoleAppender` | methods:1 |
| `ch.qos.logback.core.OutputStreamAppender` | methods:1 |
| `ch.qos.logback.core.encoder.Encoder` | methods:1 |
| `ch.qos.logback.core.encoder.LayoutWrappingEncoder` | methods:1 |
| `ch.qos.logback.core.pattern.PatternLayoutEncoderBase` | methods:1 |
| `ch.qos.logback.core.spi.ContextAware` | methods:1 |
| `com.fasterxml.jackson.databind.ObjectMapper` | class-only |
| `com.fasterxml.jackson.dataformat.xml.XmlMapper` | class-only |
| `com.github.benmanes.caffeine.cache.BBHeader$ReadAndWriteCounterRef` | fields:1 |
| `com.github.benmanes.caffeine.cache.BBHeader$ReadCounterRef` | fields:1 |
| `com.github.benmanes.caffeine.cache.BLCHeader$DrainStatusRef` | fields:1 |
| `com.github.benmanes.caffeine.cache.BaseMpscLinkedArrayQueueColdProducerFields` | fields:1 |
| `com.github.benmanes.caffeine.cache.BaseMpscLinkedArrayQueueConsumerFields` | fields:1 |
| `com.github.benmanes.caffeine.cache.BaseMpscLinkedArrayQueueProducerFields` | fields:1 |
| `com.github.benmanes.caffeine.cache.BoundedLocalCache` | fields:1 |
| `com.github.benmanes.caffeine.cache.PS` | fields:2 |
| `com.github.benmanes.caffeine.cache.PSA` | fields:1 |
| `com.github.benmanes.caffeine.cache.PSAW` | fields:1 |
| `com.github.benmanes.caffeine.cache.PSAWMS` | methods:1 |
| `com.github.benmanes.caffeine.cache.PSW` | fields:1 |
| `com.github.benmanes.caffeine.cache.PSWMS` | methods:1 |
| `com.github.benmanes.caffeine.cache.SSMS` | fields:2 |
| `com.github.benmanes.caffeine.cache.SSMSA` | fields:2 |
| `com.github.benmanes.caffeine.cache.SSMSAW` | fields:2 |
| `com.github.benmanes.caffeine.cache.StripedBuffer` | fields:1 |
| `com.github.benmanes.caffeine.cache.UnboundedLocalCache` | fields:1 |
| `com.github.benmanes.caffeine.jcache.copy.JavaSerializationCopier` | methods:1 |
| `com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider` | class-only |
| `com.google.protobuf.ExtensionRegistry` | methods:1 |
| `com.zaxxer.hikari.HikariConfig` | DF, methods:84 |
| `com.zaxxer.hikari.HikariConfig$` | class-only |
| `com.zaxxer.hikari.HikariConfigMXBean` | class-only |
| `com.zaxxer.hikari.pool.PoolBase` | fields:1 |
| `com.zaxxer.hikari.pool.PoolEntry` | fields:1 |
| `freemarker.core._2_4_OrLaterMarker` | class-only |
| `freemarker.ext.beans.HashAdapter$1$1$1` | AF |
| `freemarker.ext.jython.JythonModel` | class-only |
| `freemarker.template.Configuration` | class-only |
| `io.vavr.control.Try` | class-only |
| `jakarta.ejb.EJB` | class-only |
| `jakarta.inject.Provider` | class-only |
| `jakarta.persistence.PersistenceContext` | class-only |
| `jakarta.servlet.FilterConfig` | class-only |
| `jakarta.servlet.ServletContext` | class-only |
| `jakarta.servlet.ServletRequest` | class-only |
| `jakarta.servlet.http.HttpServletRequest` | class-only |
| `jakarta.validation.ConstraintViolation` | class-only |
| `jakarta.xml.ws.WebServiceRef` | class-only |
| `kotlin.Metadata` | class-only |
| `kotlin.reflect.full.KClasses` | class-only |
| `kotlinx.coroutines.reactor.MonoKt` | class-only |
| `libcore.io.Memory` | class-only |
| `org.apache.catalina.Container` | methods:1 |
| `org.apache.catalina.CredentialHandler` | methods:1 |
| `org.apache.catalina.LifecycleListener` | methods:1 |
| `org.apache.catalina.LifecycleState` | methods:1 |
| `org.apache.catalina.Valve` | methods:1 |
| `org.apache.catalina.WebResourceRoot` | methods:1 |
| `org.apache.catalina.Wrapper` | methods:1 |
| `org.apache.catalina.connector.Request` | methods:1 |
| `org.apache.catalina.connector.RequestFacade` | AF, methods:1 |
| `org.apache.catalina.loader.JdbcLeakPrevention` | methods:2 |
| `org.apache.catalina.mbeans.ClassNameMBean` | methods:1 |
| `org.apache.catalina.mbeans.ConnectorMBean` | methods:1 |
| `org.apache.catalina.mbeans.ContainerMBean` | methods:1 |
| `org.apache.catalina.mbeans.ContextMBean` | methods:1 |
| `org.apache.catalina.mbeans.NamingResourcesMBean` | methods:1 |
| `org.apache.catalina.mbeans.ServiceMBean` | methods:1 |
| `org.apache.catalina.servlets.DefaultServlet` | methods:1 |
| `org.apache.catalina.startup.Tomcat$SimpleRealm` | class-only |
| `org.apache.catalina.util.CharsetMapper` | methods:1 |
| `org.apache.commons.jexl3.JexlArithmetic` | methods:1 |
| `org.apache.commons.pool2.impl.DefaultEvictionPolicy` | methods:1 |
| `org.apache.commons.pool2.impl.DefaultPooledObjectInfo` | class-only |
| `org.apache.commons.pool2.impl.GenericObjectPoolMXBean` | class-only |
| `org.apache.coyote.AbstractProtocol` | methods:3 |
| `org.apache.coyote.Adapter` | methods:1 |
| `org.apache.coyote.ContinueResponseTiming` | methods:1 |
| `org.apache.coyote.Processor` | methods:1 |
| `org.apache.coyote.Request` | methods:1 |
| `org.apache.coyote.RequestGroupInfo` | methods:1 |
| `org.apache.coyote.RequestInfo` | class-only |
| `org.apache.coyote.Response` | methods:1 |
| `org.apache.coyote.UpgradeProtocol` | methods:1 |
| `org.apache.coyote.http11.AbstractHttp11Protocol` | methods:1 |
| `org.apache.coyote.http11.Http11NioProtocol` | class-only |
| `org.apache.log.Logger` | class-only |
| `org.apache.log4j.Logger` | class-only |
| `org.apache.tomcat.util.buf.StringCache` | class-only |
| `org.apache.tomcat.util.http.parser.HttpParser` | methods:1 |
| `org.apache.tomcat.util.modeler.AttributeInfo` | methods:4 |
| `org.apache.tomcat.util.modeler.BaseModelMBean` | methods:1 |
| `org.apache.tomcat.util.modeler.FeatureInfo` | methods:3 |
| `org.apache.tomcat.util.modeler.ManagedBean` | methods:9 |
| `org.apache.tomcat.util.modeler.OperationInfo` | methods:4 |
| `org.apache.tomcat.util.modeler.ParameterInfo` | methods:1 |
| `org.apache.tomcat.util.modeler.modules.MbeansDescriptorsDigesterSource` | methods:1 |
| `org.apache.tomcat.util.modeler.modules.MbeansDescriptorsIntrospectionSource` | methods:1 |
| `org.apache.tomcat.util.net.AbstractEndpoint` | methods:1 |
| `org.apache.tomcat.util.net.AbstractEndpoint$Handler` | methods:1 |
| `org.apache.tomcat.util.net.NioEndpoint` | class-only |
| `org.apache.tomcat.util.net.SSLHostConfig` | methods:1 |
| `org.apache.tomcat.util.net.SocketProperties` | class-only |
| `org.eclipse.core.runtime.FileLocator` | class-only |
| `org.graalvm.nativeimage.ImageInfo` | methods:1 |
| `org.h2.Driver` | class-only |
| `org.hibernate.SessionFactory` | class-only |
| `org.hibernate.boot.cfgxml.internal.CfgXmlAccessServiceImpl` | class-only |
| `org.hibernate.boot.internal.Abstract` | class-only |
| `org.hibernate.boot.internal.AnyKeyType` | methods:1 |
| `org.hibernate.boot.internal.CollectionClassification` | methods:1 |
| `org.hibernate.boot.internal.Extends` | methods:1 |
| `org.hibernate.boot.internal.Target` | methods:1 |
| `org.hibernate.boot.models.DialectOverrideAnnotations` | AF |
| `org.hibernate.boot.models.HibernateAnnotations` | AF |
| `org.hibernate.boot.models.JpaAnnotations` | AF |
| `org.hibernate.boot.models.XmlAnnotations` | AF |
| `org.hibernate.cache.internal.BasicCacheKeyImplementation` | class-only |
| `org.hibernate.cache.internal.EnabledCaching` | class-only |
| `org.hibernate.cache.jcache.internal.StrategyRegistrationProviderImpl` | class-only |
| `org.hibernate.cache.spi.entry.CollectionCacheEntry` | class-only |
| `org.hibernate.cache.spi.entry.StandardCacheEntryImpl` | class-only |
| `org.hibernate.cache.spi.support.AbstractReadWriteAccess$Item` | class-only |
| `org.hibernate.engine.config.internal.ConfigurationServiceImpl` | class-only |
| `org.hibernate.engine.extension.internal.ExtensionIntegrationServiceImpl` | class-only |
| `org.hibernate.engine.jdbc.batch.internal.BatchBuilderImpl` | class-only |
| `org.hibernate.engine.jdbc.dialect.internal.DialectFactoryImpl` | class-only |
| `org.hibernate.engine.jdbc.dialect.internal.DialectResolverSet` | class-only |
| `org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentImpl` | class-only |
| `org.hibernate.engine.jdbc.internal.JdbcServicesImpl` | class-only |
| `org.hibernate.engine.jdbc.mutation.internal.StandardMutationExecutorService` | class-only |
| `org.hibernate.engine.jndi.internal.JndiServiceImpl` | class-only |
| `org.hibernate.engine.query.internal.NativeQueryInterpreterStandardImpl` | class-only |
| `org.hibernate.engine.spi.PrimeAmongSecondarySupertypes` | class-only |
| `org.hibernate.internal.util.cache.InternalCacheFactoryImpl` | class-only |
| `org.hibernate.loader.ast.internal.StandardBatchLoaderFactory` | class-only |
| `org.hibernate.persister.internal.PersisterFactoryImpl` | class-only |
| `org.hibernate.persister.internal.StandardPersisterClassResolver` | class-only |
| `org.hibernate.property.access.internal.PropertyAccessStrategyResolverStandardImpl` | class-only |
| `org.hibernate.proxy.HibernateProxy` | methods:2 |
| `org.hibernate.proxy.ProxyConfiguration` | class-only |
| `org.hibernate.query.sqm.mutation.internal.SqmMultiTableMutationStrategyProviderStandard` | class-only |
| `org.hibernate.resource.beans.internal.ManagedBeanRegistryImpl` | class-only |
| `org.hibernate.service.internal.SessionFactoryServiceRegistryFactoryImpl` | class-only |
| `org.hibernate.sql.ast.internal.ParameterMarkerStrategyStandard` | class-only |
| `org.hibernate.sql.results.jdbc.internal.JdbcValuesMappingProducerProviderStandard` | class-only |
| `org.hibernate.stat.internal.StatisticsImpl` | class-only |
| `org.hibernate.temporal.internal.ChangesetCoordinatorImpl` | class-only |
| `org.hibernate.type.SqlTypes` | AF |
| `org.ietf.jgss.GSSContext` | methods:1 |
| `org.ietf.jgss.GSSName` | methods:1 |
| `org.postgresql.Driver` | class-only |
| `org.postgresql.core.QueryExecutorCloseAction` | fields:1 |
| `org.postgresql.ds.PGSimpleDataSource` | methods:1 |
| `org.postgresql.ds.common.BaseDataSource` | methods:3 |
| `org.postgresql.jdbc.PgStatement` | fields:3 |
| `org.postgresql.util.PGobject` | class-only |
| `org.python.core.PyObject` | class-only |
| `org.reactivestreams.Publisher` | class-only |
| `org.robolectric.Robolectric` | class-only |
| `org.slf4j.bridge.SLF4JBridgeHandler` | methods:2 |
| `org.slf4j.spi.LocationAwareLogger` | methods:1 |
| `org.springframework.aop.framework.AbstractSingletonProxyFactoryBean` | methods:5 |
| `org.springframework.aop.framework.Advised` | class-only |
| `org.springframework.aop.framework.ProxyConfig` | methods:10, fields:1 |
| `org.springframework.aop.scope.ScopedObject` | class-only |
| `org.springframework.beans.factory.Aware` | DF |
| `org.springframework.beans.factory.Aware$` | class-only |
| `org.springframework.beans.factory.BeanClassLoaderAware` | DF |
| `org.springframework.beans.factory.BeanClassLoaderAware$` | class-only |
| `org.springframework.beans.factory.FactoryBean` | DF, methods:3 |
| `org.springframework.beans.factory.FactoryBean$` | class-only |
| `org.springframework.beans.factory.InitializingBean` | DF |
| `org.springframework.beans.factory.InitializingBean$` | class-only |
| `org.springframework.core.DecoratingProxy` | class-only |
| `org.springframework.transaction.ConfigurableTransactionManager` | DF, methods:2 |
| `org.springframework.transaction.ConfigurableTransactionManager$` | class-only |
| `org.springframework.transaction.PlatformTransactionManager` | DF |
| `org.springframework.transaction.PlatformTransactionManager$` | class-only |
| `org.springframework.transaction.TransactionExecutionListener` | class-only |
| `org.springframework.transaction.TransactionManager` | DF |
| `org.springframework.transaction.TransactionManager$` | class-only |
| `org.springframework.transaction.interceptor.TransactionalProxy` | class-only |
| `org.springframework.transaction.support.AbstractPlatformTransactionManager` | methods:16 |
| `org.springframework.transaction.support.ResourceTransactionManager` | DF, methods:1 |
| `org.springframework.transaction.support.ResourceTransactionManager$` | class-only |
| `org.zeroturnaround.javarebel.ClassEventListener` | class-only |
| `redis.clients.jedis.ConnectionPool` | class-only |
| `redis.clients.jedis.RedisClient` | class-only |
| `scala.Equals` | class-only |
| `scala.Function1` | class-only |
| `scala.Option` | class-only |
| `scala.PartialFunction` | class-only |
| `scala.Product` | class-only |
| `scala.collection.Iterable` | class-only |
| `scala.collection.IterableFactoryDefaults` | class-only |
| `scala.collection.IterableOnce` | class-only |
| `scala.collection.IterableOnceOps` | class-only |
| `scala.collection.IterableOps` | class-only |
| `scala.collection.Seq` | class-only |
| `scala.collection.SeqOps` | class-only |
| `scala.collection.concurrent.CNodeBase` | fields:1 |
| `scala.collection.concurrent.INodeBase` | fields:1 |
| `scala.collection.concurrent.MainNode` | fields:1 |
| `scala.collection.concurrent.TrieMap` | fields:1 |
| `scala.collection.convert.JavaCollectionWrappers$MapWrapper` | AF, methods:1 |
| `scala.collection.immutable.Iterable` | class-only |
| `scala.collection.immutable.List` | class-only |
| `scala.collection.immutable.Map` | class-only |
| `scala.collection.immutable.Seq` | class-only |
| `scala.collection.immutable.SeqOps` | class-only |
| `scala.collection.immutable.Set` | class-only |
| `scala.collection.mutable.Buffer` | class-only |
| `scala.collection.mutable.HashMap` | class-only |
| `scala.collection.mutable.Map` | class-only |
| `scala.collection.mutable.Seq` | class-only |
| `scala.collection.mutable.Set` | class-only |
| `scala.deriving.Mirror` | class-only |
| `scala.deriving.Mirror$Product` | class-only |
| `scala.deriving.Mirror$Singleton` | class-only |
| `scala.math.Ordered` | methods:5 |
| `scala.reflect.Enum` | class-only |
| `scala.runtime.EnumValue` | class-only |
| `tools.jackson.databind.json.JsonMapper` | class-only |
| `tools.jackson.dataformat.xml.XmlMapper` | class-only |

## 三、JDK 类（85）

| 类名 | 反射形态 |
|---|---|
| `com.sun.crypto.provider.AESCipher$General` | methods:1 |
| `com.sun.crypto.provider.HmacCore$HmacSHA256` | methods:1 |
| `com.sun.crypto.provider.PBEKeyFactory$PBEWithMD5AndDES` | methods:1 |
| `com.sun.crypto.provider.PBEWithMD5AndDESCipher` | methods:1 |
| `com.sun.crypto.provider.PBKDF2Core$HmacSHA1` | methods:1 |
| `com.sun.crypto.provider.PBKDF2Core$HmacSHA224` | methods:1 |
| `com.sun.crypto.provider.PBKDF2Core$HmacSHA256` | methods:1 |
| `com.sun.crypto.provider.PBKDF2Core$HmacSHA384` | methods:1 |
| `com.sun.crypto.provider.PBKDF2Core$HmacSHA512` | methods:1 |
| `com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl` | methods:1 |
| `com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl` | methods:1 |
| `java.awt.Desktop` | methods:3 |
| `java.lang.Boolean` | fields:1 |
| `java.lang.Byte` | fields:1 |
| `java.lang.Character` | fields:1 |
| `java.lang.Class` | methods:64 |
| `java.lang.ClassLoader` | methods:1 |
| `java.lang.Double` | fields:1 |
| `java.lang.Float` | fields:1 |
| `java.lang.Integer` | fields:1 |
| `java.lang.Long` | fields:1 |
| `java.lang.Object` | AF, methods:18 |
| `java.lang.Package` | methods:18 |
| `java.lang.Runtime` | methods:5 |
| `java.lang.SecurityManager` | methods:1 |
| `java.lang.Short` | fields:1 |
| `java.lang.String` | methods:1, fields:1 |
| `java.lang.System` | methods:10 |
| `java.lang.Thread` | AF, methods:15, fields:2 |
| `java.lang.Thread$Builder` | methods:2 |
| `java.lang.ThreadGroup` | methods:9 |
| `java.lang.Void` | fields:1 |
| `java.lang.annotation.Annotation` | methods:1 |
| `java.lang.constant.ClassDesc` | methods:9 |
| `java.lang.invoke.VarHandle` | methods:1 |
| `java.lang.management.ManagementPermission` | methods:1 |
| `java.lang.management.PlatformLoggingMXBean` | methods:4 |
| `java.lang.reflect.AccessibleObject` | AF, methods:9 |
| `java.lang.reflect.AnnotatedElement` | methods:7 |
| `java.lang.reflect.AnnotatedType` | methods:5 |
| `java.lang.reflect.Constructor` | methods:17 |
| `java.lang.reflect.Executable` | methods:21 |
| `java.lang.reflect.Field` | methods:12 |
| `java.lang.reflect.GenericArrayType` | methods:1 |
| `java.lang.reflect.GenericDeclaration` | methods:1 |
| `java.lang.reflect.Member` | methods:4 |
| `java.lang.reflect.Method` | methods:21 |
| `java.lang.reflect.Parameter` | methods:16 |
| `java.lang.reflect.ParameterizedType` | methods:1 |
| `java.lang.reflect.Type` | methods:1 |
| `java.lang.reflect.TypeVariable` | methods:4 |
| `java.net.URI` | methods:27 |
| `java.net.URL` | methods:13 |
| `java.security.AccessController` | methods:1 |
| `java.security.Principal` | methods:1 |
| `java.sql.SQLException` | fields:1 |
| `java.sql.Types` | AF |
| `java.util.ArrayList` | methods:1 |
| `java.util.Map$Entry` | methods:5 |
| `java.util.Properties` | methods:1 |
| `java.util.PropertyPermission` | methods:1 |
| `java.util.concurrent.ConcurrentMap` | methods:1 |
| `java.util.concurrent.ForkJoinTask` | fields:2 |
| `java.util.concurrent.atomic.AtomicBoolean` | fields:1 |
| `java.util.concurrent.atomic.AtomicReference` | fields:1 |
| `java.util.concurrent.atomic.Striped64` | fields:2 |
| `java.util.logging.LogManager` | methods:1 |
| `java.util.logging.SimpleFormatter` | methods:1 |
| `javax.management.MBeanOperationInfo` | methods:1 |
| `javax.management.MBeanServerBuilder` | methods:1 |
| `javax.management.StandardEmitterMBean` | methods:3 |
| `javax.net.ssl.SSLParameters` | methods:1 |
| `javax.security.auth.Subject` | methods:1 |
| `jdk.management.jfr.FlightRecorderMXBeanImpl` | methods:4 |
| `sun.java2d.marlin.DMarlinRenderingEngine` | methods:1 |
| `sun.misc.Unsafe` | fields:1 |
| `sun.rmi.transport.Target` | fields:1 |
| `sun.security.provider.MD5` | methods:1 |
| `sun.security.provider.NativePRNG` | methods:2 |
| `sun.security.provider.SHA` | methods:1 |
| `sun.security.provider.SHA2$SHA224` | methods:1 |
| `sun.security.provider.SHA2$SHA256` | methods:1 |
| `sun.security.provider.SHA5$SHA384` | methods:1 |
| `sun.security.provider.SHA5$SHA512` | methods:1 |
| `sun.security.provider.SecureRandom` | methods:2 |
