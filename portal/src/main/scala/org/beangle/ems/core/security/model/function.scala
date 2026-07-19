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

package org.beangle.ems.core.security.model

import org.beangle.data.model.pojo.{Enabled, Named, Remark, TemporalAt}
import org.beangle.data.model.{IntId, LongId}
import org.beangle.ems.core.config.model.{App, Env}
import org.beangle.ems.core.user.model.Role
import org.beangle.security.authz.{Permission, Resource, Scope}

import java.security.Principal
import java.time.Instant

class FuncResource extends IntId, Named, Enabled, Resource, Remark {
  var app: App = _
  var scope = Scope.Public
  var title: String = _
  var actions: Option[String] = None

  def description: String = {
    name + " " + title
  }
}

/** 角色在某应用上的功能资源授权（与场景无关） */
class FuncPermission extends LongId, Permission, TemporalAt, Remark {
  var role: Role = _
  var resource: FuncResource = _
  var actions: Option[String] = None
  var restrictions: Option[String] = None

  def this(role: Role, resource: FuncResource) = {
    this();
    this.role = role
    this.resource = resource
    this.beginAt = Instant.now
  }

  def principal: Principal = role
}

/**
 * 角色在某应用上限定的业务场景（一行一个 env）。
 * 无记录表示不限制（全部场景）；有多条则表示仅这些场景生效。
 */
class RoleAppEnv extends LongId {
  var role: Role = _
  var app: App = _
  var env: Env = _

  def this(role: Role, app: App, env: Env) = {
    this()
    this.role = role
    this.app = app
    this.env = env
  }
}
