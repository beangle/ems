import org.beangle.parent.Dependencies.*
import sbt.*

object EmsDepends {
  val commonsVer = "5.6.15"
  val jdbcVer = "1.0.0"
  val dataVer = "5.8.9"
  val cdiVer = "0.6.5"
  val webVer = "0.4.11"
  val serializerVer = "0.1.9"
  val cacheVer = "0.1.8"
  val templateVer = "0.1.13"
  val webmvcVer = "0.9.25"
  val securityVer = "4.3.19"
  val idsVer = "0.3.16"
  val eventVer = "0.0.4"
  val docVer = "0.3.3"

  val b_commons = "org.beangle.commons" % "beangle-commons" % commonsVer
  val b_jdbc = "org.beangle.jdbc" % "beangle-jdbc" % jdbcVer
  val b_model = "org.beangle.data" % "beangle-model" % dataVer
  val b_cdi = "org.beangle.cdi" % "beangle-cdi" % cdiVer
  val b_cache = "org.beangle.cache" % "beangle-cache" % cacheVer
  val b_template = "org.beangle.template" % "beangle-template" % templateVer
  val b_web = "org.beangle.web" % "beangle-web" % webVer
  val b_webmvc = "org.beangle.webmvc" % "beangle-webmvc" % webmvcVer
  val b_serializer = "org.beangle.serializer" % "beangle-serializer" % serializerVer
  val b_security = "org.beangle.security" % "beangle-security" % securityVer
  val b_ids = "org.beangle.ids" % "beangle-ids" % idsVer
  val b_event = "org.beangle.event" % "beangle-event" % eventVer
  val b_doc_transfer = "org.beangle.doc" % "beangle-doc-transfer" % docVer

  val appDepends = Seq(b_commons, logback_classic, scalatest, b_web) ++
    Seq(b_cdi, spring_beans, spring_context, spring_tx, spring_jdbc, gson, HikariCP, b_webmvc, jedis) ++
    Seq(b_model, hibernate_core, b_jdbc, b_cache, b_security, b_template) ++
    Seq(postgresql, caffeine_jcache, hibernate_jcache, b_event)
}
