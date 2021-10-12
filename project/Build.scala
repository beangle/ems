import sbt.Keys._
import sbt._
import org.beangle.parent.Dependencies._

object EmsDepends {
  val commonsVer = "5.2.6"
  val dataVer = "5.3.25"
  val cdiVer = "0.3.3"
  val webVer = "0.0.2"
  val serializerVer= "0.0.21"
  val cacheVer= "0.0.24"
  val templateVer ="0.0.34"
  val webmvcVer="0.4.5"
  val securityVer="4.2.31"
  val idsVer="0.2.22"

  val commonsCore = "org.beangle.commons" %% "beangle-commons-core" % commonsVer
  val commonsFile = "org.beangle.commons" %% "beangle-commons-file" % commonsVer
  val dataJdbc = "org.beangle.data" %% "beangle-data-jdbc" % dataVer
  val dataOrm = "org.beangle.data" %% "beangle-data-orm" % dataVer
  val dataModel = "org.beangle.data" %% "beangle-data-model" % dataVer
  val dataHibernate = "org.beangle.data" %% "beangle-data-hibernate" % dataVer
  val dataTransfer = "org.beangle.data" %% "beangle-data-transfer" % dataVer
  val cdiApi = "org.beangle.cdi" %% "beangle-cdi-api" % cdiVer
  val cdiSpring = "org.beangle.cdi" %% "beangle-cdi-spring" % cdiVer
  val cacheApi = "org.beangle.cache" %% "beangle-cache-api" % cacheVer
  val cacheCaffeine = "org.beangle.cache" %% "beangle-cache-caffeine" % cacheVer
  val templateApi = "org.beangle.template" %% "beangle-template-api" % templateVer
  val templateFreemarker = "org.beangle.template" %% "beangle-template-freemarker" % templateVer
  val webAction = "org.beangle.web" %% "beangle-web-action" % webVer
  val webmvcCore= "org.beangle.webmvc" %% "beangle-webmvc-core" % webmvcVer
  val webmvcFreemarker= "org.beangle.webmvc" %% "beangle-webmvc-freemarker" % webmvcVer
  val webmvcSupport= "org.beangle.webmvc" %% "beangle-webmvc-support" % webmvcVer
  val webmvcBootstrap= "org.beangle.webmvc" %% "beangle-webmvc-bootstrap" % webmvcVer
  val serializerText = "org.beangle.serializer" %% "beangle-serializer-text" % serializerVer
  val securityCore= "org.beangle.security" %% "beangle-security-core" % securityVer
  val securityWeb= "org.beangle.security" %% "beangle-security-web" % securityVer
  val securitySession= "org.beangle.security" %% "beangle-security-session" % securityVer
  val securityCas= "org.beangle.security" %% "beangle-security-cas" % securityVer
  val idsCas = "org.beangle.ids" %% "beangle-ids-cas" % idsVer

  val appDepends = Seq(commonsCore,commonsFile, logback_classic, logback_core, scalatest, webAction,cdiApi,gson,HikariCP) ++
                   Seq(dataModel,dataJdbc,cacheApi,cacheCaffeine,securitySession,securityCas,templateFreemarker,ehcache,jaxb,jaxb_impl)
}
