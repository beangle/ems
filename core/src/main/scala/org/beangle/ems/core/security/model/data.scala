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

import java.security.Principal
import java.time.Instant

import org.beangle.data.model.{ IntId, LongId }
import org.beangle.data.model.pojo.{ Named, Remark }
import org.beangle.security.authz.{ Permission, Resource, Scope }
import org.beangle.ems.core.config.model.{ App, Domain }
import org.beangle.ems.core.user.model.Role

class DataPermission extends LongId  with Permission with Remark {
  var domain: Domain = _
  var app: Option[App] = None
  var resource: DataResource = _
  var description: String = _
  var filters: String = _
  var funcResource: Option[FuncResource] = None
  var attrs: Option[String] = None
  var actions: Option[String] = None
  var restrictions: Option[String] = None
  var role: Option[Role] = None

  var beginAt:Instant=_
  var endAt:Option[Instant]=None
  def principal: Principal = role.orNull
}

class DataResource extends IntId with Named with Resource with Remark {
  var domain: Domain = _
  var scope = Scope.Public
  var typeName: String = _
  var title: String = _
  var actions: Option[String] = None
  def enabled = true
}
