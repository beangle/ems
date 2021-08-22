import Dependencies._
import BuildSettings._
import sbt.url

ThisBuild / organization := "org.beangle.ems"
ThisBuild / version := "4.1.21-SNAPSHOT"

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
    commonSettings,
    crossPaths := false
  )

lazy val app = (project in file("app"))
  .settings(
    name := "beangle-ems-app",
    commonSettings,
    libraryDependencies ++= appDepends
  )

lazy val core = (project in file("core"))
  .settings(
    name := "beangle-ems-core",
    commonSettings,
    libraryDependencies ++= Seq(idsCas,dataHibernate)
  ).dependsOn(app)

lazy val web = (project in file("web"))
  .settings(
    name := "beangle-ems-web",
    commonSettings,
    libraryDependencies ++= Seq(webmvcSupport,hibernateJCache,ehcache)
  ).dependsOn(core)

lazy val cas = (project in file("cas"))
  .settings(
    name := "beangle-ems-cas",
    commonSettings,
    libraryDependencies ++= Seq(webmvcBootstrap)
  ).dependsOn(web)

lazy val ws = (project in file("ws"))
  .settings(
    name := "beangle-ems-ws",
    commonSettings,
    libraryDependencies ++= Seq(serializerText,cacheCaffeine)
  ).dependsOn(web)

lazy val portal = (project in file("portal"))
  .settings(
    name := "beangle-ems-portal",
    commonSettings,
    libraryDependencies ++= Seq(webmvcBootstrap)
  ).dependsOn(web)

lazy val index = (project in file("index"))
  .settings(
    name := "beangle-ems-index",
    commonSettings,
    libraryDependencies ++= Seq(webmvcSupport)
  ).dependsOn(core)

publish / skip := true
