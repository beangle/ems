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

import org.beangle.ems.core.user.model.{MemberShip, RoleMember, User, UserCategory}

trait UserService {

  def get(code: String): Option[User]

  def get(id: Long): User

  def getUsers(id: Long*): collection.Seq[User]

  def getRoles(user: User, ship: MemberShip.Ship): collection.Seq[RoleMember]

  def isManagedBy(manager: User, user: User): Boolean

  def create(creator: User, user: User): Unit

  def remove(creator: User, user: User): Unit

  def isRoot(user: User, appName: String): Boolean

  def getCategories():Seq[UserCategory]
}
