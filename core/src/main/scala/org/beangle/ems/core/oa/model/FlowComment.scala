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

package org.beangle.ems.core.oa.model

import org.beangle.data.model.LongId
import org.beangle.data.model.pojo.Updated
import org.beangle.ems.core.user.model.User

import java.time.Instant

/** 流程审批意见
 */
class FlowComment extends LongId, Updated {
  var task: FlowTask = _
  var user: User = _
  var messages: String = _

  def this(task: FlowTask, user: User, messages: String) = {
    this()
    this.task = task
    this.user = user
    this.messages = messages
    this.updatedAt = Instant.now
  }
}
