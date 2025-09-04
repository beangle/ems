import EmsDepends.*
import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*

ThisBuild / organization := "org.beangle.ems"
ThisBuild / version := "4.12.0"

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

ThisBuild / description := "The Beangle EMS Application"
ThisBuild / homepage := Some(url("http://beangle.github.io/ems/index.html"))
ThisBuild / resolvers += Resolver.mavenLocal

lazy val root = (project in file("."))
  .settings(
    common
  )
  .aggregate(static, app, core, cas, portal, index, ws)

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
    libraryDependencies ++= appDepends,
    libraryDependencies ++= Seq(beangle_webmvc % "optional", beangle_bui_tag % "optional")
  )

lazy val core = (project in file("core"))
  .settings(
    name := "beangle-ems-core",
    common,
    libraryDependencies ++= Seq(beangle_ids, apache_commons_compress, jexl3)
  ).dependsOn(app)

lazy val cas = (project in file("cas"))
  .enablePlugins(WarPlugin, TomcatPlugin, UndertowPlugin)
  .settings(
    name := "beangle-ems-cas",
    common,
    libraryDependencies ++= webAppDepends,
    libraryDependencies ++= Seq(beangle_bui_bootstrap)
  ).dependsOn(core)

lazy val ws = (project in file("ws"))
  .enablePlugins(WarPlugin, TomcatPlugin, UndertowPlugin)
  .settings(
    name := "beangle-ems-ws",
    common,
    libraryDependencies ++= webAppDepends,
  ).dependsOn(core)

lazy val portal = (project in file("portal"))
  .enablePlugins(WarPlugin, TomcatPlugin)
  .settings(
    name := "beangle-ems-portal",
    common,
    libraryDependencies ++= Seq(beangle_doc_transfer, beangle_bui_bootstrap),
    libraryDependencies ++= webAppDepends
  ).dependsOn(core)

lazy val index = (project in file("index"))
  .enablePlugins(WarPlugin)
  .settings(
    name := "beangle-ems-index",
    common,
    libraryDependencies ++= webAppDepends
  ).dependsOn(core)

publish / skip := true
