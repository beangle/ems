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

import org.beangle.commons.collection.Collections
import org.beangle.data.model.LongId
import org.beangle.data.model.pojo.Named
import org.beangle.ems.core.user.model.User

import java.time.Instant
import scala.collection.mutable

/** 流程任务
 */
class FlowActiveTask extends LongId, Named {
  /** 流程 */
  var process: FlowActiveProcess = _
  /** 顺序号 */
  var idx: Int = _
  /** 受理人 */
  var assignees: mutable.Set[User] = Collections.newSet[User]
  /** 开始时间 */
  var startAt: Instant = _
  /** 预计完成时间 */
  var dueTime: Option[Instant] = None

  def this(process: FlowActiveProcess, activity: FlowActivity) = {
    this()
    this.process = process
    this.idx = activity.idx
    this.startAt = Instant.now
    this.name = activity.name
  }

  def complete(assignee: User): Unit = {
    if (this.process.initiator.isEmpty) {
      if (!this.process.tasks.exists(_.idx < this.idx)) {
        this.process.initiator = Some(assignee)
      }
    }
  }
}
