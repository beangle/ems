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

package org.beangle.ems.ws.user

import org.beangle.commons.collection.Properties
import org.beangle.ems.core.user.service.UserService
import org.beangle.webmvc.annotation.{mapping, param, response}
import org.beangle.webmvc.support.ActionSupport

/**
 * @author chaostone
 */
class AccountWS(userService: UserService) extends ActionSupport {

  @response
  @mapping("{userCode}")
  def index(@param("userCode") userCode: String): Properties = {
    userService.getAccount(userCode) match {
      case Some(acc) =>
        val properties = new Properties()
        properties += ("name" -> acc.name)
        properties += ("description" -> acc.description)
        properties += ("accountExpired" -> acc.accountExpired)
        properties += ("accountLocked" -> acc.accountLocked)
        properties += ("credentialExpired" -> acc.credentialExpired)
        properties += ("enabled" -> !acc.disabled)

        properties += ("authorities" -> acc.authorities)
        properties += ("details" -> acc.details)
        properties
      case None => new Properties()
    }
  }
}
