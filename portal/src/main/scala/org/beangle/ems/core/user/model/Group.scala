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
import org.beangle.commons.lang.{Numbers, Strings}
import org.beangle.data.model.IntId
import org.beangle.data.model.pojo.*
import org.beangle.ems.core.config.model.Org

import scala.collection.mutable

/**
 * @author chaostone
 */
class Group extends IntId, Named, Coded, Updated, Enabled, Hierarchical[Group], Remark {
  var org: Org = _
  var manager: Option[User] = None
  var roles: mutable.Set[Role] = Collections.newSet[Role]

  def index: Int = {
    if (Strings.isEmpty(indexno)) return 1;
    val lastPart = Strings.substringAfterLast(indexno, ".")
    if (lastPart.isEmpty) Numbers.toInt(indexno) else Numbers.toInt(lastPart)
  }

  def this(id: Int, name: String) = {
    this()
    this.id = id
    this.name = name
  }

  def isParentOf(p: Group): Boolean = {
    val pts = Collections.newSet[Group]
    var pt: Group = p
    while (null != pt && !pts.contains(pt) && !pts.contains(this)) {
      pts.add(pt)
      pt = pt.parent.orNull
    }
    pts.contains(this)
  }
}
