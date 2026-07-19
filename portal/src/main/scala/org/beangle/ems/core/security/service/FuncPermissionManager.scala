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
import org.beangle.ems.core.security.model.{FuncPermission, FuncResource, RoleAppEnv}
import org.beangle.ems.core.user.model.{Role, User}

trait FuncPermissionService {
  def getResource(app: App, name: String): Option[FuncResource]

  def getResourceIdsByRole(roleId: Int): Set[Int]

  def getResources(user: User): Seq[FuncResource]

  def getResources(app: App): Seq[FuncResource]

  def getPermissions(app: App, role: Role): Seq[FuncPermission]

  /** 角色在应用上限定的场景；空表示不限制（全部场景） */
  def getRoleAppEnvs(app: App, role: Role): Seq[RoleAppEnv]

  def activate(resourceId: Iterable[Int], active: Boolean): Unit

  /**
   * 授权功能资源，并设置场景限定。
   * envIds 为空：不存储 RoleAppEnv（表示全部场景）；非空：每个 env 存一条。
   */
  def authorize(app: App, role: Role, resources: Set[FuncResource], envIds: Iterable[Long] = Nil): Unit

  def removeResources(entities: Iterable[FuncResource]): Unit

}
