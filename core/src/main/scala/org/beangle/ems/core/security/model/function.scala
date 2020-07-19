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
package org.beangle.ems.core.security.model

import java.security.Principal

import org.beangle.security.authz.{ Permission, Resource, Scopes }
import org.beangle.ems.core.config.model.App
import org.beangle.ems.core.user.model.Role
import org.beangle.data.model.LongId
import org.beangle.data.model.pojo.Named
import org.beangle.data.model.IntId
import org.beangle.data.model.pojo.Remark
import org.beangle.data.model.pojo.TemporalAt
import org.beangle.data.model.pojo.Enabled
import java.time.ZonedDateTime
import java.time.Instant

class FuncResource extends IntId with Named with Enabled with Resource with Remark {
  var app: App = _
  var scope = Scopes.Public
  var title: String = _
  var actions: Option[String] = None

  def description: String = {
    name + " " + title
  }
}

class FuncPermission extends LongId with Permission with TemporalAt with Remark {
  var role: Role = _
  var resource: FuncResource = _
  var actions: Option[String] = None
  var restrictions: Option[String] = None

  def this(role: Role, resource: FuncResource) {
    this();
    this.role = role
    this.resource = resource
    this.beginAt = Instant.now
  }

  def principal: Principal = role
}

class AppPermission extends IntId with Permission with TemporalAt {
  var app: App = _
  var resource: FuncResource = _
  var actions: Option[String] = None
  var restrictions: Option[String] = None
  def principal = app
}
