import EmsDepends.*
import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*

organization := "org.beangle.ems"
version := "4.20.10"

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
  .aggregate(app, portal)

lazy val app = (project in file("app"))
  .settings(
    name := "beangle-ems-app",
    common,
    libraryDependencies ++= appDepends,
    libraryDependencies ++= Seq(beangle_webmvc % "optional", beangle_bui_tag % "optional")
  )

lazy val portal = (project in file("portal"))
  .enablePlugins(WarPlugin, TomcatPlugin, NativeImagePlugin)
  .settings(
    name := "beangle-ems-portal",
    common,
    libraryDependencies ++= Seq(beangle_ids, beangle_notify, apache_commons_compress),
    libraryDependencies ++= Seq(beangle_transfer, beangle_bui_bootstrap),
    libraryDependencies ++= Seq(sshd_core, slf4j_jcl),
    snapshotRepoUrl := "https://sas.openurp.net/sas/repo/snapshot/upload/{fileName}",
    libraryDependencies ++= webAppDepends,
    // native-image 配置
    Compile / mainClass := Some("org.beangle.sas.engine.tomcat.Bootstrap"),
    nativeImageGraalHome := Def.uncached {
      file(sys.env.getOrElse("GRAALVM_HOME",
        sys.env.getOrElse("JAVA_HOME", "/home/chaostone/local/graalvm-jdk-21"))).toPath
    },
    nativeImageInstalled := true,
    nativeImageOutput := xsbti.VirtualFileRef.of((NativeImage / target).value.getAbsolutePath + "/ems-portal"),
    nativeImageOptions ++= Seq(
      "--no-fallback",
      "--enable-sbom=cyclonedx,export",
      "--enable-url-protocols=jar,resource,http,https",
      "-H:+AddAllCharsets",
      "-H:+BuildReport",
      "-H:IncludeResourceBundles=org.apache.xmlbeans.impl.regex.message",
      "-H:IncludeResources=.*\\.xsb",
      "-H:IncludeResources=.*functionMetadata.*\\.txt",
      "-H:+ReportExceptionStackTraces",
      "--report-unsupported-elements-at-runtime",
      "--initialize-at-build-time=ch.qos.logback,org.slf4j",
      "--initialize-at-run-time=java.net.http"
    ),
    libraryDependencies ++= Seq(
      "org.beangle.sas" % "beangle-sas-engine" % "0.13.12-SNAPSHOT",
      "org.apache.tomcat.embed" % "tomcat-embed-core" % "11.0.25" exclude("org.apache.tomcat", "tomcat-annotations-api"),
      "org.apache.tomcat.embed" % "tomcat-embed-websocket" % "11.0.25" exclude("org.apache.tomcat", "tomcat-annotations-api")
    )
  )
  .dependsOn(app)
