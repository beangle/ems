import org.beangle.parent.Settings._
import org.beangle.parent.Dependencies._
import EmsDepends._

ThisBuild / organization := "org.beangle.ems"
ThisBuild / version := "4.1.23"

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/beangle/ems"),
    "scm:git@github.com:beangle/ems.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id    = "chaostone",
    name  = "Tihua Duan",
    email = "duantihua@gmail.com",
    url   = url("http://github.com/duantihua")
  )
)

ThisBuild / description := "The Beangle EMS Library"
ThisBuild / homepage := Some(url("http://beangle.github.io/ems/index.html"))
ThisBuild / resolvers += Resolver.mavenLocal

lazy val root = (project in file("."))
  .settings()
  .aggregate(static,app,core,web,cas,ws,portal,index)

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
    libraryDependencies ++= Seq(idsCas,dataHibernate)
  ).dependsOn(app)

lazy val web = (project in file("web"))
  .settings(
    name := "beangle-ems-web",
    common,
    libraryDependencies ++= Seq(webmvcSupport,hibernate_jcache,ehcache)
  ).dependsOn(core)

lazy val cas = (project in file("cas"))
  .enablePlugins(WarPlugin)
  .settings(
    name := "beangle-ems-cas",
    common,
    libraryDependencies ++= Seq(webmvcBootstrap),
    libraryDependencies ++= appDepends
  ).dependsOn(web,app,core)

lazy val ws = (project in file("ws"))
  .enablePlugins(WarPlugin)
  .settings(
    name := "beangle-ems-ws",
    common,
    libraryDependencies ++= Seq(serializerText,cacheCaffeine),
    libraryDependencies ++= appDepends
  ).dependsOn(web,core,app)

lazy val portal = (project in file("portal"))
  .enablePlugins(WarPlugin)
  .settings(
    name := "beangle-ems-portal",
    common,
    libraryDependencies ++= Seq(webmvcBootstrap,idsCas,dataHibernate),
    libraryDependencies ++= appDepends
  ).dependsOn(web,app,core)

lazy val index = (project in file("index"))
  .enablePlugins(WarPlugin)
  .settings(
    name := "beangle-ems-index",
    common,
    libraryDependencies ++= Seq(webmvcSupport)
  ).dependsOn(core,app)

publish / skip := true
