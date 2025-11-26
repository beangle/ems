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

package org.beangle.ems.ws.log

import org.beangle.commons.bean.{Disposable, Initializing}
import org.beangle.commons.codec.digest.Digests
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.Charsets
import org.beangle.data.dao.EntityDao
import org.beangle.ems.app.log.Proto
import org.beangle.ems.core.config.service.AppService
import org.beangle.webmvc.annotation.mapping
import org.beangle.webmvc.support.{ActionSupport, ServletSupport}
import org.beangle.webmvc.view.{Status, View}
import org.hibernate.SessionFactory

/** 应用系统调用，存储日志的服务
 */
class PushWS extends ActionSupport, ServletSupport, Initializing, Disposable {

  var appService: AppService = _

  var logDao: EntityDao = _

  var sf: SessionFactory = _

  var buffer: PersistBuffer = _

  @mapping("")
  def index(): View = {
    val bytes = IOs.readBytes(request.getInputStream)
    var appName: String = null
    val event = get("type", "business") match {
      case "business" =>
        val e = Proto.BusinessLogEvent.parseFrom(bytes)
        appName = e.getAppName
        e
      case "error" =>
        val e = Proto.ErrorLogEvent.parseFrom(bytes)
        appName = e.getAppName
        e
      case _ => null
    }
    if (null == appName) {
      error("Missing type parameter or missing appName in protobuf datas.")
    } else {
      appService.getApp(appName) match {
        case Some(app) =>
          if (validate(bytes, app.secret)) {
            buffer.push(event)
            Status.Ok
          } else {
            error("Invalidate data stream")
          }
        case None => error(s"Cannot find app $appName")
      }
    }
  }

  private def error(message: String): View = {
    response.getWriter.write(message)
    Status.BadRequest
  }

  private def validate(bytes: Array[Byte], secret: String): Boolean = {
    get("digest", "--") == Digests.md5Hex(Array.concat(secret.getBytes(Charsets.UTF_8), bytes))
  }

  override def init(): Unit = {
    buffer = new PersistBuffer(logDao, sf, 1024, appService)
  }

  override def destroy(): Unit = {
    buffer.destroy()
  }
}
