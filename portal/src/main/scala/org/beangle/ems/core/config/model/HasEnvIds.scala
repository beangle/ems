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

package org.beangle.ems.core.config.model

import org.beangle.commons.json.{Json, JsonArray}

/** 以 JsonArray 存储适用场景 ID；空表示不限制。 */
trait HasEnvIds {
  var envIds: JsonArray = Json.emptyArray

  /** 空表示不限制；有值时仅所列场景适用 */
  def suitable(env: Env): Boolean = {
    envIds.isEmpty || envIds.exists {
      case n: Number => n.longValue == env.id
      case x => x.toString.toLongOption.contains(env.id)
    }
  }

  @transient
  def envIdSet: Set[Long] = {
    envIds.flatMap {
      case n: Number => Some(n.longValue)
      case x => x.toString.toLongOption
    }.toSet
  }

  def setEnvIdSet(ids: Set[Long]): Unit = {
    val distinct = ids.map(_.toString).toList
    envIds = if (distinct.isEmpty) Json.emptyArray else JsonArray(distinct *)
  }
}
