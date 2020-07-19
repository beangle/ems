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
package org.beangle.ems.core.config.model

import org.beangle.commons.collection.Collections
import org.beangle.data.model.IntId
import org.beangle.data.model.pojo.{Named, Remark}

import scala.collection.mutable

class Db extends IntId with Named with Remark {
  var domain: Domain = _
  var url: Option[String] = None
  var driver: String = _
  var serverName: String = _
  var databaseName: String = _
  var portNumber: Int = _
  var properties: mutable.Map[String, String] = Collections.newMap[String, String]

  def propertiesString: String = {
    val sb = new mutable.StringBuilder
    properties.foreach { case (k, v) =>
      sb.append(k).append("=").append(v).append('\n')
    }
    sb.toString
  }
}
