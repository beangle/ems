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

import org.beangle.commons.lang.{Numbers, Strings}
import org.beangle.data.model.IntId
import org.beangle.data.model.pojo.*
import org.beangle.ems.core.config.model.Domain

import java.security.Principal

/**
 * @author chaostone
 */

class Role extends IntId, Named, Updated, Enabled, Hierarchical[Role], IProfile, Principal, Remark {
  var properties: collection.mutable.Map[Dimension, String] = new collection.mutable.HashMap[Dimension, String]
  var creator: User = _
  var members: collection.mutable.Seq[RoleMember] = new collection.mutable.ListBuffer[RoleMember]
  var domain: Domain = _

  override def getName: String = {
    name
  }

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
}
