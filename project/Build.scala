import org.beangle.parent.Dependencies.*
import sbt.*

object EmsDepends {
  val commonsVer = "5.6.0"
  val dataVer = "5.7.2"
  val cdiVer = "0.5.5"
  val webVer = "0.4.6"
  val serializerVer = "0.1.5"
  val cacheVer = "0.1.5"
  val templateVer = "0.1.7"
  val webmvcVer = "0.9.9"
  val securityVer = "4.3.11"
  val idsVer = "0.3.12"

  val commonsCore = "org.beangle.commons" %% "beangle-commons-core" % commonsVer
  val commonsFile = "org.beangle.commons" %% "beangle-commons-file" % commonsVer
  val dataJdbc = "org.beangle.data" %% "beangle-data-jdbc" % dataVer
  val dataOrm = "org.beangle.data" %% "beangle-data-orm" % dataVer
  val dataTransfer = "org.beangle.data" %% "beangle-data-transfer" % dataVer
  val cdiApi = "org.beangle.cdi" %% "beangle-cdi-api" % cdiVer
  val cdiSpring = "org.beangle.cdi" %% "beangle-cdi-spring" % cdiVer
  val cacheApi = "org.beangle.cache" %% "beangle-cache-api" % cacheVer
  val cacheCaffeine = "org.beangle.cache" %% "beangle-cache-caffeine" % cacheVer
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

//  val hibernate_core = "org.beangle.hibernate" % "beangle-hibernate-core" % "6.2.7.Final"
//  val hibernate_jcache = "org.hibernate.orm" % "hibernate-jcache" % "6.2.9.Final" exclude("org.hibernate.orm", "hibernate-core")

  val appDepends = Seq(commonsCore, commonsFile, logback_classic, logback_core, scalatest, webAction, cdiApi, cdiSpring, gson, HikariCP) ++
    Seq(dataOrm, hibernate_core, dataJdbc, cacheApi, cacheCaffeine, securitySession, securitySso, templateApi) ++
    Seq(postgresql, caffeine_jcache, hibernate_jcache)
}
