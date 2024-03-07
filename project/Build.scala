import org.beangle.parent.Dependencies.*
import sbt.*

object EmsDepends {
  val commonsVer = "5.6.14"
  val dataVer = "5.8.7"
  val cdiVer = "0.6.4"
  val webVer = "0.4.10"
  val serializerVer = "0.1.8"
  val cacheVer = "0.1.7"
  val templateVer = "0.1.11"
  val webmvcVer = "0.9.23"
  val securityVer = "4.3.17"
  val idsVer = "0.3.15"
  val eventVer = "0.0.3"

  val commonsCore = "org.beangle.commons" %% "beangle-commons-core" % commonsVer
  val commonsFile = "org.beangle.commons" %% "beangle-commons-file" % commonsVer
  val commonsText = "org.beangle.commons" %% "beangle-commons-text" % commonsVer
  val dataJdbc = "org.beangle.data" %% "beangle-data-jdbc" % dataVer
  val dataOrm = "org.beangle.data" %% "beangle-data-orm" % dataVer
  val dataTransfer = "org.beangle.data" %% "beangle-data-transfer" % dataVer
  val cdiApi = "org.beangle.cdi" %% "beangle-cdi-api" % cdiVer
  val cdiSpring = "org.beangle.cdi" %% "beangle-cdi-spring" % cdiVer
  val cacheApi = "org.beangle.cache" %% "beangle-cache-api" % cacheVer
  val cacheCaffeine = "org.beangle.cache" %% "beangle-cache-caffeine" % cacheVer
  val cacheRedis = "org.beangle.cache" %% "beangle-cache-redis" % cacheVer
  val templateApi = "org.beangle.template" %% "beangle-template-api" % templateVer
  val templateFreemarker = "org.beangle.template" %% "beangle-template-freemarker" % templateVer
  val webAction = "org.beangle.web" %% "beangle-web-action" % webVer
  val webServlet = "org.beangle.web" %% "beangle-web-servlet" % webVer
  val webmvcCore = "org.beangle.webmvc" %% "beangle-webmvc-core" % webmvcVer
  val webmvcView = "org.beangle.webmvc" %% "beangle-webmvc-view" % webmvcVer
  val webmvcSupport = "org.beangle.webmvc" %% "beangle-webmvc-support" % webmvcVer
  val serializerText = "org.beangle.serializer" %% "beangle-serializer-text" % serializerVer
  val securityCore = "org.beangle.security" %% "beangle-security-core" % securityVer
  val securityWeb = "org.beangle.security" %% "beangle-security-web" % securityVer
  val securitySession = "org.beangle.security" %% "beangle-security-session" % securityVer
  val securitySso = "org.beangle.security" %% "beangle-security-sso" % securityVer
  val idsCas = "org.beangle.ids" %% "beangle-ids-cas" % idsVer
  val idsWeb = "org.beangle.ids" %% "beangle-ids-web" % idsVer
  val idsSms = "org.beangle.ids" %% "beangle-ids-sms" % idsVer
  val eventCore = "org.beangle.event" %% "beangle-event-core" % eventVer
  val eventBus = "org.beangle.event" %% "beangle-event-bus" % eventVer

  val appDepends = Seq(commonsCore, commonsFile, commonsText, logback_classic, logback_core, scalatest, webAction) ++
    Seq(cdiApi, cdiSpring, gson, HikariCP, webmvcSupport, cacheRedis) ++
    Seq(dataOrm, hibernate_core, dataJdbc, cacheApi, cacheCaffeine, securitySession, securitySso, templateApi) ++
    Seq(postgresql, caffeine_jcache, hibernate_jcache, eventBus)
}
