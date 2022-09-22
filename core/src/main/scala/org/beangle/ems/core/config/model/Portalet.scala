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

import org.beangle.commons.collection.Collections
import org.beangle.data.model.IntId
import org.beangle.data.model.pojo.{Enabled, Named}
import org.beangle.ems.core.user.model.UserCategory

import scala.collection.mutable

/**
 * 小部件
 */
class Portalet extends IntId with Named with Enabled {

  var idx: Int = _

  var title: String = _

  var url: String = _

  var usingIframe: Boolean = _

  var rowIndex: Int = _

  var colspan: Int = _

  var categories: mutable.Set[UserCategory] = Collections.newSet[UserCategory]

}
