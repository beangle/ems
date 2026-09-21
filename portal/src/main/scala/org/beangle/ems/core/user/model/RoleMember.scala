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

import org.beangle.data.model.LongId
import org.beangle.data.model.pojo.Updatable
import org.beangle.ems.core.config.model.HasEnvIds

import java.time.Instant
import scala.compiletime.uninitialized

/**
 * @author chaostone
 */
class RoleMember extends LongId, Updatable, HasEnvIds {
  var user: User = uninitialized
  var role: Role = uninitialized

  var member: Boolean = uninitialized
  var granter: Boolean = uninitialized
  var manager: Boolean = uninitialized

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
}
