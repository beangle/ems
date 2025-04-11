import org.beangle.parent.Dependencies.*
import sbt.*

object EmsDepends {
  val commonsVer = "5.6.27"
  val jdbcVer = "1.0.10"
  val dataVer = "5.8.22"
  val cdiVer = "0.7.2"
  val webVer = "0.6.3"
  val serializerVer = "0.1.16"
  val cacheVer = "0.1.13"
  val templateVer = "0.1.25-SNAPSHOT"
  val webmvcVer = "0.10.4"
  val buiVer = "0.0.3"
  val securityVer = "4.3.27"
  val idsVer = "0.3.23"
  val eventVer = "0.0.12"
  val docVer = "0.4.9"

  val beangle_commons = "org.beangle.commons" % "beangle-commons" % commonsVer
  val beangle_jdbc = "org.beangle.jdbc" % "beangle-jdbc" % jdbcVer
  val beangle_model = "org.beangle.data" % "beangle-model" % dataVer
  val beangle_cdi = "org.beangle.cdi" % "beangle-cdi" % cdiVer
  val beangle_cache = "org.beangle.cache" % "beangle-cache" % cacheVer
  val beangle_template = "org.beangle.template" % "beangle-template" % templateVer
  val beangle_web = "org.beangle.web" % "beangle-web" % webVer
  val beangle_webmvc = "org.beangle.webmvc" % "beangle-webmvc" % webmvcVer
  val beangle_bui_tag = "org.beangle.bui" % "beangle-bui-tag" % buiVer
  val beangle_bui_bootstrap = "org.beangle.bui" % "beangle-bui-bootstrap" % buiVer
  val beangle_serializer = "org.beangle.serializer" % "beangle-serializer" % serializerVer
  val beangle_security = "org.beangle.security" % "beangle-security" % securityVer
  val beangle_ids = "org.beangle.ids" % "beangle-ids" % idsVer
  val beangle_event = "org.beangle.event" % "beangle-event" % eventVer
  val beangle_doc_transfer = "org.beangle.doc" % "beangle-doc-transfer" % docVer

  val appDepends = Seq(beangle_commons, logback_classic, scalatest, beangle_web) ++
    Seq(beangle_cdi, beangle_jdbc, spring_beans, spring_context, spring_tx, spring_jdbc, HikariCP) ++
    Seq(beangle_model, hibernate_core, postgresql, beangle_serializer, caffeine_jcache, hibernate_jcache, beangle_event, beangle_cache) ++
    Seq(beangle_template, jedis, beangle_security)

  val webAppDepends = appDepends ++ Seq(beangle_webmvc, freemarker)
}
