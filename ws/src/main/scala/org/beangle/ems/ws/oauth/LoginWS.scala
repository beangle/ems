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

package org.beangle.ems.ws.oauth

import org.beangle.commons.bean.Initializing
import org.beangle.commons.collection.Collections
import org.beangle.commons.json.JsonObject
import org.beangle.data.dao.EntityDao
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.config.model.ThirdPartyApp
import org.beangle.security.realm.jwt.{JwtDigest, Jwts}
import org.beangle.webmvc.annotation.{action, body, mapping, response}
import org.beangle.webmvc.support.ActionSupport

@action("/oauth/login")
class LoginWS extends ActionSupport, Initializing {

  var entityDao: EntityDao = _

  private var digest: JwtDigest = _

  override def init(): Unit = {
    val s = EmsApp.properties.getOrElse("openapi.secret", "org.beangle.ems:ems-ws.openapi.secret").toString
    this.digest = Jwts.digest(s)
  }

  @mapping("")
  def index(@body body: JsonObject): JsonObject = {
    if (body.contains("username") && body.contains("password")) {
      val username = body.getString("username")
      val password = body.getString("password")
      val apps = entityDao.findBy(classOf[ThirdPartyApp], "code" -> username, "secret" -> password)
      if (apps.size == 1) {
        val app = apps.head
        val rs = new JsonObject()
        rs.add("code", "200")
        rs.add("msg", "Login success")
        val data = new JsonObject()
        data.add("userId", app.id.toString)
        data.add("userCode", app.code)
        data.add("userName", app.name)
        data.add("token", generateToken(app))
        rs.add("data", data)
        rs
      } else {
        val rs = new JsonObject()
        rs.add("code", "500")
        rs.add("msg", s"Cannot find app with ${username} and the password")
        rs
      }
    } else {
      val rs = new JsonObject()
      rs.add("code", "500")
      rs.add("msg", "Missing json in body which contains username and password")
      rs
    }
  }

  @response
  @mapping("verify/{token}")
  def verify(token: String): Boolean = {
    try {
      val jo = Jwts.getClaims(token)
      val apps = entityDao.findBy(classOf[ThirdPartyApp], "code" -> jo.getString("userCode"))
      if (apps.size != 1) {
        false
      } else {
        digest.validateToken(token)
      }
    } catch {
      case e: Exception => false
    }
  }

  private def generateToken(app: ThirdPartyApp): String = {
    val claims = Collections.newMap[String, String]
    claims.put("userId", app.id.toString)
    claims.put("userCode", app.code)
    claims.put("userName", app.name)
    digest.generateToken(claims)
  }
}
