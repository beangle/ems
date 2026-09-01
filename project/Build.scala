import org.beangle.parent.Dependencies.*
import sbt.*

object EmsDepends {
  val beangle_commons = "org.beangle.commons" % "beangle-commons" % "6.3.1"
  val beangle_cdi = "org.beangle.cdi" % "beangle-cdi" % "0.10.7-SNAPSHOT"
  val beangle_bui_tag = "org.beangle.bui" % "beangle-bui-tag" % "0.9.2-SNAPSHOT"
  val beangle_bui_bootstrap = "org.beangle.bui" % "beangle-bui-bootstrap" % "0.9.2-SNAPSHOT"
  val beangle_cache = "org.beangle.cache" % "beangle-cache" % "0.1.21-SNAPSHOT"
  val beangle_config = "org.beangle.config" % "beangle-config" % "1.1.10"
  val beangle_event = "org.beangle.event" % "beangle-event" % "0.1.10-SNAPSHOT"
  val beangle_ids = "org.beangle.ids" % "beangle-ids" % "0.4.21-SNAPSHOT"
  val beangle_data_hibernate = "org.beangle.data" % "beangle-data-hibernate" % "5.12.8-SNAPSHOT"
  val beangle_notify = "org.beangle.notify" % "beangle-notify" % "0.1.27-SNAPSHOT"
  val beangle_jdbc = "org.beangle.jdbc" % "beangle-jdbc" % "1.1.13"
  val beangle_she = "org.beangle.she" % "beangle-she" % "0.0.18-SNAPSHOT"
  val beangle_security = "org.beangle.security" % "beangle-security" % "4.5.1-SNAPSHOT"
  val beangle_serializer = "org.beangle.serializer" % "beangle-serializer" % "0.1.28-SNAPSHOT"
  val beangle_template = "org.beangle.template" % "beangle-template" % "0.2.10-SNAPSHOT"
  val beangle_transfer = "org.beangle.transfer" % "beangle-transfer" % "0.0.8-SNAPSHOT"
  val beangle_webmvc = "org.beangle.webmvc" % "beangle-webmvc" % "0.15.2-SNAPSHOT"
  val beangle_cron = "org.beangle.cron" % "beangle-cron" % "0.0.7-SNAPSHOT"
  val sshd_core = "org.apache.sshd" % "sshd-core" % "2.18.0"

  val slf4j_jcl = "org.slf4j" % "jcl-over-slf4j" % "2.0.18"
  val appDepends = Seq(beangle_commons, beangle_config, typesafe_config, slf4j, logback_classic) ++
    Seq(beangle_cdi, spring_beans) ++
    Seq(beangle_jdbc, HikariCP, postgresql) ++
    Seq(beangle_cache, caffeine_jcache, jedis) ++
    Seq(beangle_data_hibernate, spring_tx, spring_aop) ++
    Seq(beangle_security, beangle_template, beangle_serializer, beangle_event) ++
    Seq(beangle_cron, jexl3) ++ //定时器和表达式引擎
    Seq(scalatest)

  val webAppDepends = appDepends ++ Seq(beangle_she,beangle_webmvc)
}
