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
import org.beangle.commons.json.Json
import org.beangle.data.model.LongId
import org.beangle.data.model.pojo.Named
import org.beangle.ems.app.oa.Flows.Payload
import org.beangle.ems.core.user.model.User

import java.time.Instant
import scala.collection.mutable

/** 流程任务
 */
class FlowTask extends LongId, Named {
  /** 流程 */
  var process: FlowProcess = _
  /** 顺序号 */
  var idx: Int = _
  /** 受理人 */
  var assignee: Option[User] = None
  /** 开始时间 */
  var startAt: Instant = _
  /** 结束时间 */
  var endAt: Option[Instant] = None
  /** 审批意见 */
  var comments: mutable.Buffer[FlowComment] = Collections.newBuffer[FlowComment]
  /** 审批附件 */
  var attachments: mutable.Buffer[FlowAttachment] = Collections.newBuffer[FlowAttachment]
  /** 任务填写表单 */
  var dataJson: String = "{}"
  /** 当前环节 */
  var status: FlowStatus = FlowStatus.Initial

  def this(process: FlowProcess, at: FlowActiveTask) = {
    this()
    this.id = at.id
    this.process = process
    this.name = at.name
    this.idx = at.idx
    this.startAt = at.startAt
    this.status = FlowStatus.Initial
  }

  def complete(assignee: User, payload: Payload): Unit = {
    this.assignee = Some(assignee)
    if (payload.complete) {
      this.endAt = Some(Instant.now())
      this.status = FlowStatus.Completed
    }
    payload.comments foreach { c =>
      val comment = new FlowComment(this, assignee, c)
      this.comments += comment
    }
    payload.attachments foreach { a =>
      val attachment = new FlowAttachment(this, a)
      this.attachments += attachment
    }
    val j = Json.parseObject(this.dataJson)
    j.addAll(payload.data)
    this.dataJson = j.toJson
    this.process.updateEnv(payload.env)
  }
}
