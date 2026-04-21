/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.ems.ws.config

import jakarta.servlet.http.HttpServletResponse
import org.beangle.commons.bean.Initializing
import org.beangle.commons.codec.digest.Digests
import org.beangle.commons.collection.Properties
import org.beangle.commons.lang.Strings
import org.beangle.commons.net.Networks
import org.beangle.ems.app.Ems
import org.beangle.ems.core.cache.Redis
import org.beangle.ems.core.config.service.AppService
import org.beangle.web.servlet.util.RequestUtils
import org.beangle.webmvc.annotation.{mapping, param, response}
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.support.{ActionSupport, ServletSupport}

/** 应用查询redis服务的配置信息
 */
class RedisWS extends ActionSupport, ServletSupport, Initializing {

  var appService: AppService = _

  private var host: String = "127.0.0.1"
  private var port: Int = 6379
  private var password: Option[String] = None

  private val ips: Set[String] = Networks.addresses(1)

  override def init(): Unit = {
    val conf = Redis.conf
    if conf.nonEmpty then
      this.host = conf("host")
      this.port = conf("port").toInt
      this.password = conf.get("password")
  }

  @mapping(value = "{app}")
  @response
  def index(@param("app") app: String): AnyRef = {
    val ip = RequestUtils.getIpAddr(request)
    val gateway = Strings.substringBeforeLast(ip, ".")
    val matched = ips exists { i => ip == i || Strings.substringBeforeLast(i, ".") == gateway }

    if (matched) {
      val apps = appService.getApp(app)
      if (apps.isEmpty) return reportError("error:error_app_name")
      val exist = apps.head
      val digest = Digests.md5Hex(Ems.key + exist.name)

      if (!get("digest").contains(digest)) return reportError("error:bad_digest")
      val properties = new Properties
      properties.put("host", host)
      properties.put("port", port)
      password foreach { p =>
        val decryptor = Ems.decryptor
        properties.put("password", decryptor.encrypt(p))
      }

      val ds = new Properties()
      ds.put("redis", properties)
      ds
    } else {
      new Properties()
    }
  }

  private def reportError(msg: String): String = {
    val res = ActionContext.current.response
    res.getWriter.print(msg)
    res.setStatus(HttpServletResponse.SC_BAD_REQUEST)
    null
  }
}
