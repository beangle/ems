/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2020, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.ems.core.user.service

import org.beangle.security.authc.{CredentialAge, DefaultAccount}
import org.beangle.ems.core.user.model.{Account, User}

trait AccountService {

  def get(code: String): Option[Account]

  def getAuthAccount(code: String): Option[DefaultAccount]

  def enable(manager: User, accountIds: Iterable[Long], enabled: Boolean): Int

  def getActivePassword(code: String): Option[String]

  def getPasswordAge(code: String): Option[CredentialAge]

  def updatePassword(code: String, rawPassword: String): Unit

  def createAccount(user: User, account: Account): Unit
}
