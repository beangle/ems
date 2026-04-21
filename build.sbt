import EmsDepends.*
import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*

ThisBuild / organization := "org.beangle.ems"
ThisBuild / version := "4.18.15-SNAPSHOT"

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
  .aggregate(static, app, portal)

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

lazy val portal = (project in file("portal"))
  .enablePlugins(WarPlugin, TomcatPlugin, UndertowPlugin)
  .settings(
    name := "beangle-ems-portal",
    common,
    libraryDependencies ++= Seq(beangle_ids, beangle_notify, apache_commons_compress),
    libraryDependencies ++= Seq(beangle_transfer, beangle_bui_bootstrap),
    libraryDependencies ++= Seq(sshd_core, slf4j_jcl),
    libraryDependencies ++= webAppDepends
  ).dependsOn(app)

publish / skip := true
