import org.beangle.parent.Dependencies.*
import sbt.*

object EmsDepends {
  val beangle_bui_tag = "org.beangle.bui" % "beangle-bui-tag" % "0.1.3-SNAPSHOT"
  val beangle_bui_bootstrap = "org.beangle.bui" % "beangle-bui-bootstrap" % "0.1.3-SNAPSHOT"
  val beangle_cdi = "org.beangle.cdi" % "beangle-cdi" % "0.10.0-SNAPSHOT"
  val beangle_cache = "org.beangle.cache" % "beangle-cache" % "0.1.19-SNAPSHOT"
  val beangle_commons = "org.beangle.commons" % "beangle-commons" % "6.0.0-SNAPSHOT"
  val beangle_config = "org.beangle.config" % "beangle-config" % "1.1.0-SNAPSHOT"
  val beangle_event = "org.beangle.event" % "beangle-event" % "0.1.4-SNAPSHOT"
  val beangle_ids = "org.beangle.ids" % "beangle-ids" % "0.4.6-SNAPSHOT"
  val beangle_model = "org.beangle.data" % "beangle-model" % "5.11.7-SNAPSHOT"
  val beangle_jdbc = "org.beangle.jdbc" % "beangle-jdbc" % "1.1.7-SNAPSHOT"
  val beangle_security = "org.beangle.security" % "beangle-security" % "4.4.6-SNAPSHOT"
  val beangle_serializer = "org.beangle.serializer" % "beangle-serializer" % "0.1.24-SNAPSHOT"
  val beangle_template = "org.beangle.template" % "beangle-template" % "0.2.4-SNAPSHOT"
  val beangle_transfer = "org.beangle.transfer" % "beangle-transfer" % "0.0.4-SNAPSHOT"
  val beangle_web = "org.beangle.web" % "beangle-web" % "0.7.3-SNAPSHOT"
  val beangle_webmvc = "org.beangle.webmvc" % "beangle-webmvc" % "0.13.2-SNAPSHOT"
  val beangle_notify = "org.beangle.notify" % "beangle-notify" % "0.1.19-SNAPSHOT"

  val appDepends = Seq(beangle_commons, logback_classic, scalatest, beangle_web, beangle_config) ++
    Seq(beangle_cdi, beangle_jdbc, spring_beans, spring_context, spring_tx, spring_jdbc, HikariCP) ++
    Seq(beangle_model, hibernate_core, postgresql, beangle_serializer, caffeine_jcache, hibernate_jcache, beangle_event, beangle_cache) ++
    Seq(beangle_template, jedis, beangle_security)

  val webAppDepends = appDepends ++ Seq(beangle_webmvc, freemarker)
}
