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

import org.beangle.commons.lang.Strings

object Property {
  val All = "*"
}

/**
 * @author chaostone
 */
trait Profile {

  def name: String

  def properties: collection.mutable.Map[Dimension, String]

  def setProperty(field: Dimension, value: String): Unit = {
    if (Strings.isNotBlank(value))
      properties.put(field, value)
    else properties -= field
  }

  def getProperty(field: Dimension): Option[String] = {
    properties.get(field)
  }

  def getProperty(name: String): Option[String] = {
    properties.keys.find(k => k.name == name) match {
      case Some(p) => properties.get(p)
      case None => None
    }
  }

  def matches(other: Profile): Boolean = {
    if (other.properties.isEmpty) return true
    other.properties exists {
      case (field, target) =>
        val source = getProperty(field).getOrElse("")
        (source != Property.All) && ((target == Property.All) || (Strings.split(target, ",").toSet -- Strings.split(source, ",")).isEmpty)
    }
  }
}
