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
import org.beangle.commons.collection.{Collections, Properties}
import org.beangle.commons.lang.Strings
import org.beangle.ems.app.cache.Redis
import org.beangle.ems.core.config.service.AppService
import org.beangle.web.servlet.util.RequestUtils
import org.beangle.webmvc.annotation.{mapping, param, response}
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.support.{ActionSupport, ServletSupport}

import java.net.{Inet4Address, NetworkInterface}

/** 应用查询redis服务的配置信息
 */
class RedisWS extends ActionSupport, ServletSupport, Initializing {

  var appService: AppService = _

  private var host: String = "127.0.0.1"
  private var port: Int = 6379

  private val ips: Set[String] = getLocalIPs()

  override def init(): Unit = {
    val conf = Redis.conf
    if conf.nonEmpty then
      this.host = conf("host")
      this.port = conf("port").toInt
  }

  @mapping(value = "{app}")
  @response
  def index(@param("app") app: String): AnyRef = {
    val secret = get("secret", "")

    val ip = RequestUtils.getIpAddr(request)
    val gateway = Strings.substringBeforeLast(ip, ".")
    val matched = ips exists { i => ip == i || Strings.substringBeforeLast(i, ".") == gateway }

    if (matched) {
      val apps = appService.getApp(app)
      if (apps.isEmpty) return reportError("error:error_app_name")
      val exist = apps.head
      if (exist.secret != secret) return reportError("error:error_secret")
      val properties = new Properties
      properties.put("host", host)
      properties.put("port", port)

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

  private def getLocalIPs(): Set[String] = {
    val niEnum = NetworkInterface.getNetworkInterfaces
    val ips = Collections.newBuffer[String]("127.0.0.1")
    while (niEnum.hasMoreElements) {
      val ni = niEnum.nextElement()
      if (ni.isUp && !ni.isLoopback) {
        val ipEnum = ni.getInetAddresses
        while (ipEnum.hasMoreElements) {
          ipEnum.nextElement() match {
            case ip: Inet4Address => ips += ip.getHostAddress
            case _ =>
          }
        }
      }
    }
    ips.toSet
  }
}
