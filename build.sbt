import EmsDepends.*
import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*

organization := "org.beangle.ems"
version := "4.20.10-SNAPSHOT"

scmInfo := Some(
  ScmInfo(uri("https://github.com/beangle/ems"), "scm:git@github.com:beangle/ems.git")
)

developers := List(
  Developer(
    id = "chaostone",
    name = "Tihua Duan",
    email = "duantihua@gmail.com",
    url = uri("http://github.com/duantihua")
  )
)

description := "The Beangle EMS Application"
homepage := Some(uri("http://beangle.github.io/ems/index.html"))
resolvers += Resolver.mavenLocal

lazy val root = (project in file("."))
  .settings(
    name := "beangle-ems",
    common,
    publish / skip := true
  )
  .aggregate(app, portal, native)

lazy val app = (project in file("app"))
  .settings(
    name := "beangle-ems-app",
    common,
    libraryDependencies ++= appDepends,
    libraryDependencies ++= Seq(beangle_webmvc % "optional", beangle_bui_tag % "optional")
  )

lazy val portal = (project in file("portal"))
  .enablePlugins(WarPlugin, TomcatPlugin)
  .settings(
    name := "beangle-ems-portal",
    common,
    libraryDependencies ++= Seq(beangle_ids, beangle_notify, apache_commons_compress),
    libraryDependencies ++= Seq(beangle_transfer, beangle_bui_bootstrap),
    libraryDependencies ++= Seq(sshd_core, slf4j_jcl),
    snapshotRepoUrl := "https://sas.openurp.net/sas/repo/snapshot/upload/{fileName}",
    libraryDependencies ++= webAppDepends
  )
  .dependsOn(app)

lazy val native = (project in file("native"))
  .enablePlugins(NativeImagePlugin, TomcatPlugin)
  .settings(
    name := "beangle-ems-native",
    common,
    Compile / mainClass := Some("org.beangle.sas.engine.tomcat.Bootstrap"),
    nativeImageGraalHome := Def.uncached {
      file(sys.env.getOrElse("GRAALVM_HOME",
        sys.env.getOrElse("JAVA_HOME", "/home/chaostone/local/graalvm-jdk-21"))).toPath
    },
    nativeImageInstalled := true,
    nativeImageOptions ++= Seq(
      "--no-fallback",
      "--enable-url-protocols=jar,resource,http,https",
      "-H:+AddAllCharsets",
      "-H:+ReportExceptionStackTraces",
      "--report-unsupported-elements-at-runtime",
      "--initialize-at-build-time=ch.qos.logback,org.slf4j",
      "--initialize-at-run-time=java.net.http"
    ),
    // TomcatPlugin 依赖是 test scope，native-image 需要 compile scope
    libraryDependencies ++= Seq(
      "org.beangle.sas" % "beangle-sas-engine" % "0.13.12-SNAPSHOT",
      "org.apache.tomcat.embed" % "tomcat-embed-core" % "11.0.24" exclude("org.apache.tomcat", "tomcat-annotations-api"),
      "org.apache.tomcat.embed" % "tomcat-embed-websocket" % "11.0.24" exclude("org.apache.tomcat", "tomcat-annotations-api")
    ),
    libraryDependencies ++= Seq(beangle_data_hibernate, beangle_she, h2, logback_classic, logback_core),
    libraryDependencies ++= Seq(
      "com.github.ben-manes.caffeine" % "caffeine" % "3.2.0",
      "com.github.ben-manes.caffeine" % "jcache" % "3.2.0",
      "javax.cache" % "cache-api" % "1.1.1"
    )
  )
  .dependsOn(app, portal)
