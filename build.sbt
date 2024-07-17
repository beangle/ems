import EmsDepends.*
import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*

ThisBuild / organization := "org.beangle.ems"
ThisBuild / version := "4.10.1"

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
  .settings()
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
    libraryDependencies ++= appDepends
  )

lazy val core = (project in file("core"))
  .settings(
    name := "beangle-ems-core",
    common,
    libraryDependencies ++= Seq(b_commons, b_ids, b_model, apache_commons_compress)
  ).dependsOn(app)

lazy val cas = (project in file("cas"))
  .enablePlugins(WarPlugin, TomcatPlugin, UndertowPlugin)
  .settings(
    name := "beangle-ems-cas",
    common,
    libraryDependencies ++= appDepends,
    libraryDependencies ++= Seq(b_webmvc, freemarker)
  ).dependsOn(core)

lazy val ws = (project in file("ws"))
  .enablePlugins(WarPlugin, TomcatPlugin, UndertowPlugin)
  .settings(
    name := "beangle-ems-ws",
    common,
    libraryDependencies ++= Seq(b_serializer, b_event),
    libraryDependencies ++= appDepends
  ).dependsOn(core)

lazy val portal = (project in file("portal"))
  .enablePlugins(WarPlugin, TomcatPlugin)
  .settings(
    name := "beangle-ems-portal",
    common,
    libraryDependencies ++= Seq(b_webmvc, freemarker),
    libraryDependencies ++= Seq(b_doc_transfer, b_event),
    libraryDependencies ++= appDepends
  ).dependsOn(core)

lazy val index = (project in file("index"))
  .enablePlugins(WarPlugin)
  .settings(
    name := "beangle-ems-index",
    common,
    libraryDependencies ++= Seq(b_webmvc)
  ).dependsOn(core)

publish / skip := true
