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

package org.beangle.ems.app.cache

import org.beangle.cache.caffeine.CaffeineCacheManager
import org.beangle.cache.redis.JedisPoolFactory
import org.beangle.cdi.bind.BindModule
import org.beangle.commons.logging.Logging
import org.beangle.commons.net.http.HttpUtils
import org.beangle.ems.app.{EmsApi, EmsApp}

import java.io.FileInputStream
import scala.xml.Node

class DefaultModule extends BindModule, Logging {

  protected override def binding(): Unit = {
    bind("cache.Caffeine", classOf[CaffeineCacheManager]).constructor(true)

    var elem: Node = null
    EmsApp.getAppFile foreach { file =>
      val is = new FileInputStream(file)
      (scala.xml.XML.load(is) \\ "redis") foreach { e => elem = e }
    }
    if (null == elem) {
      val res = HttpUtils.getText(EmsApi.getRedisUrl)
      if (res.isOk) {
        (scala.xml.XML.loadString(res.getText) \\ "redis") foreach { e => elem = e }
      }
    }
    if (null != elem) {
      val host = (elem \ "host").text.trim
      val port = (elem \ "port").text.trim
      bind("jedis.Factory", classOf[JedisPoolFactory]).constructor(Map("host" -> host, "port" -> port))
    }
  }
}
