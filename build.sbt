import EmsDepends._
import org.beangle.parent.Dependencies._
import org.beangle.parent.Settings._

ThisBuild / organization := "org.beangle.ems"
ThisBuild / version := "4.4.0-SNAPSHOT"

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/beangle/ems"),
    "scm:git@github.com:beangle/ems.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id = "chaostone",
    name = "Tihua Duan",
    email = "duantihua@gmail.com",
    url = url("http://github.com/duantihua")
  )
)

ThisBuild / description := "The Beangle EMS Library"
ThisBuild / homepage := Some(url("http://beangle.github.io/ems/index.html"))
ThisBuild / resolvers += Resolver.mavenLocal

lazy val root = (project in file("."))
  .settings()
  .aggregate(static, app, core, web, cas, service, portal, index, ws)

lazy val static = (project in file("static"))
  .settings(
    name := "beangle-ems-static",
    common,
    crossPaths := false,
  )

lazy val app = (project in file("app"))
  .settings(
    name := "beangle-ems-app",
    common,
    libraryDependencies ++= appDepends
  )

lazy val core = (project in file("core"))
  .settings(
    name := "beangle-ems-core",
    common,
    libraryDependencies ++= Seq(commonsCore, idsCas, dataOrm)
  ).dependsOn(app)

lazy val web = (project in file("web"))
  .settings(
    name := "beangle-ems-web",
    common,
    libraryDependencies ++= Seq(commonsCore, idsWeb, webmvcSupport, hibernate_jcache, ehcache, webmvcBootstrap, dataOrm, webmvcFreemarker)
  ).dependsOn(core, app)

lazy val cas = (project in file("cas"))
  .enablePlugins(WarPlugin, TomcatPlugin)
  .settings(
    name := "beangle-ems-cas",
    common,
    libraryDependencies ++= appDepends
  ).dependsOn(web, app, core)

lazy val service = (project in file("service"))
  .settings(
    name := "beangle-ems-service",
    common,
    libraryDependencies ++= Seq(webmvcSupport, serializerText, cacheCaffeine, hibernate_jcache, ehcache),
    libraryDependencies ++= appDepends
  ).dependsOn(core, app)

lazy val ws = (project in file("ws"))
  .enablePlugins(WarPlugin, TomcatPlugin)
  .settings(
    name := "beangle-ems-ws",
    common,
  ).dependsOn(service)

lazy val portal = (project in file("portal"))
  .enablePlugins(WarPlugin, TomcatPlugin)
  .settings(
    name := "beangle-ems-portal",
    common,
    libraryDependencies ++= appDepends
  ).dependsOn(web, app, core)

lazy val index = (project in file("index"))
  .enablePlugins(WarPlugin)
  .settings(
    name := "beangle-ems-index",
    common,
    libraryDependencies ++= Seq(webmvcSupport)
  ).dependsOn(core, app)

publish / skip := true
