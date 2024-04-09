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

package org.beangle.ems.core.user.service.impl

import org.beangle.data.dao.EntityDao
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.user.service.UserService
import org.beangle.security.authc.{CredentialAge, DBCredentialStore, Principals}

class DefaultCredentialStore extends DBCredentialStore {

  var entityDao: EntityDao = _
  var domainService: DomainService = _
  var userService: UserService = _

  override def getPassword(principal: Any): Option[String] = {
    userService.getActivePassword(Principals.getName(principal))
  }

  override def updatePassword(principal: Any, rawPassword: String): Unit = {
    userService.updatePassword(Principals.getName(principal), rawPassword)
  }

  override def getAge(principal: Any): Option[CredentialAge] = {
    userService.getPasswordAge(Principals.getName(principal))
  }
}
