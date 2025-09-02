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
import org.beangle.ems.app.oa.Flows

/** 流程实例-附件
 */
class FlowAttachment extends LongId {
  var task: FlowTask = _
  var name: String = _
  var fileSize: Long = _
  var filePath: String = _

  def this(task: FlowTask, attachment: Flows.Attachment) = {
    this()
    this.task = task
    this.name = attachment.name
    this.fileSize = attachment.fileSize
    this.filePath = attachment.filePath
  }
}
