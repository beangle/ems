import org.beangle.parent.Dependencies.*
import sbt.*

object EmsDepends {
  val beangle_commons = "org.beangle.commons" % "beangle-commons" % "5.6.29"
  val beangle_jdbc = "org.beangle.jdbc" % "beangle-jdbc" % "1.0.11"
  val beangle_model = "org.beangle.data" % "beangle-model" % "5.8.24"
  val beangle_cdi = "org.beangle.cdi" % "beangle-cdi" % "0.7.3"
  val beangle_web = "org.beangle.web" % "beangle-web" % "0.6.4"
  val beangle_cache = "org.beangle.cache" % "beangle-cache" % "0.1.14"
  val beangle_template = "org.beangle.template" % "beangle-template" % "0.1.26"
  val beangle_webmvc = "org.beangle.webmvc" % "beangle-webmvc" % "0.10.6"
  val beangle_bui_tag = "org.beangle.bui" % "beangle-bui-tag" % "0.0.6"
  val beangle_bui_bootstrap = "org.beangle.bui" % "beangle-bui-bootstrap" % "0.0.6"
  val beangle_serializer = "org.beangle.serializer" % "beangle-serializer" % "0.1.18"
  val beangle_security = "org.beangle.security" % "beangle-security" % "4.3.29"
  val beangle_ids = "org.beangle.ids" % "beangle-ids" % "0.3.24"
  val beangle_event = "org.beangle.event" % "beangle-event" % "0.1.0"
  val beangle_doc_transfer = "org.beangle.doc" % "beangle-doc-transfer" % "0.4.13"

  val appDepends = Seq(beangle_commons, logback_classic, scalatest, beangle_web) ++
    Seq(beangle_cdi, beangle_jdbc, spring_beans, spring_context, spring_tx, spring_jdbc, HikariCP) ++
    Seq(beangle_model, hibernate_core, postgresql, beangle_serializer, caffeine_jcache, hibernate_jcache, beangle_event, beangle_cache) ++
    Seq(beangle_template, jedis, beangle_security)

  val webAppDepends = appDepends ++ Seq(beangle_webmvc, freemarker)
}
