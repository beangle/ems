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

package org.beangle.ems.ws.oa

import org.beangle.commons.json.Json
import org.beangle.data.dao.EntityDao
import org.beangle.ems.core.config.service.AppService
import org.beangle.notify.sms.{Receiver, SmsCodeService}
import org.beangle.webmvc.annotation.{action, mapping, param}
import org.beangle.webmvc.support.{ActionSupport, ServletSupport}
import org.beangle.webmvc.view.{Status, View}

@action("sms")
class SmsWS(entityDao: EntityDao) extends ActionSupport, ServletSupport {
  var appService: AppService = _
  var smsCodeService: Option[SmsCodeService] = None

  @mapping("send/{mobile}")
  def send(@param("mobile") mobile: String): View = {
    val appName = get("appName", "missing")
    val appSecret = get("secret", "none")
    val name = get("name", "")
    val template = get("template", "")
    if (appName.isEmpty || appSecret.isEmpty || name.isEmpty) {
      Status.BadRequest
    } else {
      appService.getApp(appName, appSecret) match {
        case None => Status.Forbidden
        case Some(app) =>
          smsCodeService match {
            case None => raw(Json.toJson(Map("code" -> 500, "msg" -> "Platform 未配置SMS发生接口")))
            case Some(s) =>
              val rs = s.send(Receiver(mobile, name), template)
              val json = Json.toJson(Map("code" -> (if rs._1 then 200 else 500), "msg" -> rs._2))
              raw(json)
          }
      }
    }
  }

  @mapping("verify/{mobile}/{code}")
  def verify(@param("mobile") mobile: String, @param("code") code: String): View = {
    smsCodeService match {
      case None => raw("false")
      case Some(s) => if s.verify(mobile, code) then raw("true") else raw("false")
    }
  }
}
