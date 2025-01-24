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
import org.beangle.commons.json.{Json, JsonObject, JsonParser}
import org.beangle.data.model.LongId

import java.time.Instant
import scala.collection.mutable

/** 业务流程历史实例
 */
class FlowProcess extends LongId {
  /** 流程定于 */
  var flow: Flow = _
  /** 业务主键 */
  var businessKey: String = _
  /** 所有执行环节 */
  var tasks: mutable.Buffer[FlowTask] = Collections.newBuffer[FlowTask]
  /** 开始时间 */
  var startAt: Instant = _
  /** 结束时间 */
  var endAt: Option[Instant] = None
  /** 全局环境变量 */
  var envJson: String = "{}"
  /** 当前环节 */
  var status: FlowStatus = FlowStatus.Initial

  def this(activeProcess: FlowActiveProcess, env: JsonObject) = {
    this()
    this.id = activeProcess.id
    this.flow = activeProcess.flow
    this.businessKey = activeProcess.businessKey
    this.startAt = activeProcess.startAt
    this.envJson = env.toJson
  }

  def updateEnv(data: JsonObject): Unit = {
    val j = Json.parseObject(this.envJson)
    j.addAll(data)
    envJson = j.toJson
  }
}

enum FlowStatus(id: Int, name: String) {
  case Initial extends FlowStatus(1, "起始")
  case Running extends FlowStatus(2, "进行中")
  case Pending extends FlowStatus(3, "待处理")
  case Completed extends FlowStatus(10, "已完结")
  case Canceled extends FlowStatus(11, "已取消")
}
