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

package org.beangle.ems.core.security.service

import org.beangle.ems.core.config.model.App
import org.beangle.ems.core.security.model.{FuncPermission, FuncResource}
import org.beangle.ems.core.user.model.{Role, User}

trait FuncPermissionService {
  def getResource(app: App, name: String): Option[FuncResource]

  def getResourceIdsByRole(roleId: Int): Set[Int]

  def getResources(user: User): Seq[FuncResource]

  def getResources(app: App): Seq[FuncResource]

  def getPermissions(app: App, role: Role): Seq[FuncPermission]

  def activate(resourceId: Iterable[Int], active: Boolean): Unit

  def authorize(app: App, role: Role, resources: Set[FuncResource]): Unit

  def removeResources(entities: Iterable[FuncResource]): Unit

}
