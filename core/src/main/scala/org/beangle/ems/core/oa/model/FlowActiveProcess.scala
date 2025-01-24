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

import java.time.Instant
import scala.collection.mutable

/** 业务流程实例
 */
class FlowActiveProcess extends LongId {
  /** 流程定于 */
  var flow: Flow = _
  /** 业务主键 */
  var businessKey: String = _
  /** 所有执行环节 */
  var tasks: mutable.Buffer[FlowActiveTask] = Collections.newBuffer[FlowActiveTask]
  /** 开始时间 */
  var startAt: Instant = _

  def this(flow: Flow, businessKey: String) = {
    this()
    this.flow = flow
    this.businessKey = businessKey
    this.startAt = Instant.now()
  }
}
