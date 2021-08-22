import sbt.Keys._
import sbt._

object BuildSettings {
  val buildScalaVersion = "3.0.1"

  val commonSettings = Seq(
    organizationName := "The Beangle Software",
    licenses += ("GNU Lesser General Public License version 3", new URL("http://www.gnu.org/licenses/lgpl-3.0.txt")),
    startYear := Some(2005),
    scalaVersion := buildScalaVersion,
    scalacOptions := Seq("-Xtarget:11","-deprecation","-feature"),
    crossPaths := true,

    publishMavenStyle := true,
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    publishM2Configuration := publishM2Configuration.value.withOverwrite(true),
    publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true),

    versionScheme := Some("early-semver"),
    pomIncludeRepository := { _ => false }, // Remove all additional repository other than Maven Central from POM
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
    })
}

object Dependencies {
  val logbackVer = "1.2.4"
  val scalatestVer = "3.2.9"
  val scalaxmlVer = "2.0.1"
  val servletapiVer = "5.0.0"
  val commonsVer = "5.2.5"
  val dataVer = "5.3.24"
  val cdiVer = "0.3.1"
  val webVer = "0.0.1"
  val serializerVer= "0.0.20"
  val springVer = "5.3.6"
  val hibernateVer = "5.5.6.Final"
  val cacheVer= "0.0.23"
  val templateVer ="0.0.33"
  val webmvcVer="0.4.4"
  val securityVer="4.2.30"
  val idsVer="0.2.21"
  val gsonVer="2.8.6"
  val ehcacheVer = "3.9.5"

  val scalatest = "org.scalatest" %% "scalatest" % scalatestVer % "test"
  val scalaxml = "org.scala-lang.modules" %% "scala-xml" % scalaxmlVer
  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVer % "test"
  val logbackCore = "ch.qos.logback" % "logback-core" % logbackVer % "test"
  val servletapi = "jakarta.servlet" % "jakarta.servlet-api" % servletapiVer

  val commonsCore = "org.beangle.commons" %% "beangle-commons-core" % commonsVer
  val commonsFile = "org.beangle.commons" %% "beangle-commons-file" % commonsVer

  val dataJdbc = "org.beangle.data" %% "beangle-data-jdbc" % dataVer
  val dataOrm = "org.beangle.data" %% "beangle-data-orm" % dataVer
  val dataModel = "org.beangle.data" %% "beangle-data-model" % dataVer
  val dataHibernate = "org.beangle.data" %% "beangle-data-hibernate" % dataVer
  val dataTransfer = "org.beangle.data" %% "beangle-data-transfer" % dataVer
  val hibernateJCache = "org.hibernate" % "hibernate-jcache" % hibernateVer  exclude("org.hibernate","hibernate-core")
  val ehcache = "org.ehcache" % "ehcache" % ehcacheVer % "test"

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

  val gson = "com.google.code.gson" % "gson" % gsonVer

  var appDepends = Seq(commonsCore,commonsFile, logbackClassic, logbackCore, scalatest, webAction,cdiApi,gson) ++
                   Seq(dataModel,dataJdbc,cacheApi,cacheCaffeine,securitySession,securityCas,templateFreemarker)
}

