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
import org.beangle.data.model.LongId
import org.beangle.data.model.pojo.*
import org.beangle.ems.core.config.model.Org

import java.security.Principal

/**
 * @author chaostone
 */

class User extends LongId with Coded with Named with Updated with TemporalOn with Principal with Remark {
  var org: Org = _
  var roles = Collections.newBuffer[RoleMember]
  var groups = Collections.newBuffer[GroupMember]
  var acounts = Collections.newBuffer[Account]
  var category: Category = _
  var avatarId: Option[String] = None
  var mobile: Option[String] = None
  var email: Option[String] = None

  override def getName: String = {
    name
  }
}
