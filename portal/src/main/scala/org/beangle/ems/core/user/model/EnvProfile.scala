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

import org.beangle.commons.collection.Collections
import org.beangle.commons.json.{Json, JsonObject}
import org.beangle.commons.lang.Strings
import org.beangle.data.model.LongId
import org.beangle.data.model.pojo.Named
import org.beangle.ems.core.config.model.{Domain, Env}

import scala.collection.mutable

object Property {
  val All = "*"
}

/**
 * 用户在某个App上的配置
 */
class EnvProfile extends LongId {
  var user: User = _
  var domain: Domain = _
  var env: Env = _
  var properties: JsonObject = Json.emptyObject

  def setProperty(field: Dimension, value: String): Unit = {
    if (Strings.isNotBlank(value)) properties.add(field.name, value) else properties.remove( field.name)
  }

  def getProperty(field: Dimension): Option[String] = {
    properties.get(field.name).map(_.toString)
  }

  def getProperty(name: String): Option[String] = {
    properties.get(name).map(_.toString)
  }

  def matches(other: EnvProfile): Boolean = {
    if (other.properties.isEmpty) return true
    other.properties exists {
      case (field, target) =>
        val source = getProperty(field).getOrElse("")
        (source != Property.All) && ((target == Property.All) || (Strings.split(target.toString, ",").toSet -- Strings.split(source, ",")).isEmpty)
    }
  }
}
