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

import java.time.{Duration, Instant}
import java.util.UUID

import org.beangle.commons.collection.Properties
import org.beangle.data.dao.EntityDao
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.annotation.{param, response}
import org.beangle.ems.core.config.model.AccessToken
import org.beangle.ems.core.config.service.AppService
import org.beangle.ems.core.oauth.service.TokenRepository

class TokenWS(tokenRepository: TokenRepository, appService: AppService) extends ActionSupport {

  var entityDao: EntityDao = _

  @response
  def login(@param("app") name: String, @param("secret") secret: String): Properties = {
    val properties = new Properties
    appService.getApp(name, secret) match {
      case None => properties.put("error", "Incorrect app name or secret!")
      case Some(app) =>
        val token = new AccessToken
        token.id = generateAccessTokenId()
        token.appId = app.id
        token.principal = app.name
        token.expiredAt = this.generateExpiredAt()
        tokenRepository.put(token)
        properties.put("token", token.id)
        properties.put("appId", app.id)
        properties.put("expiredAt", token.expiredAt)
    }
    properties
  }

  @response
  def validate(@param("token") token: String): Any = {
    val properties = new Properties
    if (null == token) {
      properties.put("error", "token needed")
    } else {
      tokenRepository.get(token) match {
        case Some(app) => app
        case None => properties.put("error", "cannot find app"); properties
      }
    }
  }

  private def generateAccessTokenId(): String = {
    UUID.randomUUID().toString
  }

  private def generateExpiredAt(): Instant = {
    Instant.now.plus(Duration.ofDays(12 * 30))
  }
}
