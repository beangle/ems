import org.beangle.parent.Dependencies.*
import sbt.*

object EmsDepends {
  val beangle_bui_tag = "org.beangle.bui" % "beangle-bui-tag" % "0.1.5"
  val beangle_bui_bootstrap = "org.beangle.bui" % "beangle-bui-bootstrap" % "0.1.5"
  val beangle_cdi = "org.beangle.cdi" % "beangle-cdi" % "0.10.0"
  val beangle_cache = "org.beangle.cache" % "beangle-cache" % "0.1.19"
  val beangle_config = "org.beangle.config" % "beangle-config" % "1.1.5"
  val beangle_event = "org.beangle.event" % "beangle-event" % "0.1.4"
  val beangle_ids = "org.beangle.ids" % "beangle-ids" % "0.4.8"
  val beangle_data_hibernate = "org.beangle.data" % "beangle-data-hibernate" % "5.12.0"
  val beangle_notify = "org.beangle.notify" % "beangle-notify" % "0.1.20"
  val beangle_jdbc = "org.beangle.jdbc" % "beangle-jdbc" % "1.1.8"
  val beangle_she = "org.beangle.she" % "beangle-she" % "0.0.1"
  val beangle_security = "org.beangle.security" % "beangle-security" % "4.4.7"
  val beangle_serializer = "org.beangle.serializer" % "beangle-serializer" % "0.1.24"
  val beangle_template = "org.beangle.template" % "beangle-template" % "0.2.5"
  val beangle_transfer = "org.beangle.transfer" % "beangle-transfer" % "0.0.5"
  val beangle_webmvc = "org.beangle.webmvc" % "beangle-webmvc" % "0.14.0"

  val appDepends = Seq(beangle_config, typesafe_config, slf4j, logback_classic) ++
    Seq(beangle_cdi, spring_beans) ++
    Seq(beangle_jdbc, HikariCP, postgresql) ++
    Seq(beangle_cache, caffeine_jcache, jedis) ++
    Seq(beangle_data_hibernate, spring_tx, spring_aop) ++
    Seq(beangle_security, beangle_template, beangle_serializer, beangle_event) ++
    Seq(scalatest)

  val webAppDepends = appDepends ++ Seq(beangle_she)
}
