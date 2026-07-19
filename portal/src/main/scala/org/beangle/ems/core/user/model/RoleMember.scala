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

package org.beangle.ems.core.user.model

import org.beangle.commons.json.{Json, JsonArray}
import org.beangle.data.model.LongId
import org.beangle.data.model.pojo.Updatable
import org.beangle.ems.core.config.model.Env

import java.time.Instant

/**
 * @author chaostone
 */
class RoleMember extends LongId, Updatable {
  var user: User = _
  var role: Role = _
  /** 适用场景 ID 列表；空表示不限制 */
  var envIds: JsonArray = Json.emptyArray

  var member: Boolean = _
  var granter: Boolean = _
  var manager: Boolean = _

  def this(user: User, role: Role) = {
    this()
    this.user = user
    this.role = role
    this.updatedAt = Instant.now
  }

  def this(user: User, role: Role, ships: MemberShip*) = {
    this(user, role)
    ships foreach {
      case MemberShip.Member => member = true
      case MemberShip.Manager => manager = true
      case MemberShip.Granter => granter = true
    }
  }

  def is(ship: MemberShip): Boolean = {
    ship match {
      case MemberShip.Member => member
      case MemberShip.Manager => manager
      case MemberShip.Granter => granter
    }
  }

  /** 空表示不限制；有值时仅所列场景适用 */
  def suitable(env: Env): Boolean = {
    envIds.isEmpty || envIds.exists {
      case n: Number => n.longValue == env.id
      case x => x.toString.toLongOption.contains(env.id)
    }
  }

  def envIdSet: Set[Long] = {
    envIds.flatMap {
      case n: Number => Some(n.longValue)
      case x => x.toString.toLongOption
    }.toSet
  }

  def setEnvIds(ids: Iterable[Long]): Unit = {
    val distinct = ids.toSeq.distinct.sorted
    envIds = if (distinct.isEmpty) Json.emptyArray else JsonArray(distinct *)
  }
}
