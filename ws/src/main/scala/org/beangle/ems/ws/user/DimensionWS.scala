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

package org.beangle.ems.ws.user

import org.beangle.commons.collection.Properties
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.annotation.{mapping, param, response}
import org.beangle.ems.core.user.service.DimensionService

/**
 * @author chaostone
 */
class DimensionWS extends ActionSupport {

  var dimensionService: DimensionService = _

  @response
  @mapping("{name}")
  def index(@param("name") name: String): Properties = {
    val dimensions = dimensionService.get(name)
    if (dimensions.isEmpty) {
      new Properties()
    } else {
      val dimension = dimensions.head
      new Properties(dimension, "id", "name", "title", "source", "multiple", "required", "typeName", "keyName", "properties")
    }
  }
}
