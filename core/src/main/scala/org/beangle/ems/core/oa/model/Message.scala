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
import org.beangle.ems.core.user.model.User

import java.time.Instant

object Message {
  val Newly = 1
  val Readed = 2
  val InTrash = 3
}

/** 消息
 * */
class Message extends LongId {
  /** 标题 */
  var title: String = _
  /** 内容 */
  var contents: String = _
  /** 发送用户 */
  var sender: Option[User] = None
  /** 发送人 */
  var sendFrom: String = _
  /** 接受人 */
  var recipient: User = _
  /** 消息状态 */
  var status: Int = _
  /** 发送时间 */
  var sentAt: Instant = _

  def this(recipient: User, title: String, contents: String) = {
    this()
    this.recipient = recipient
    this.title = title
    this.contents = contents
    this.sentAt = Instant.now
    this.status = Message.Newly
  }

  def updateSender(sender: User): Unit = {
    this.sendFrom = sender.name
    this.sender = Some(sender)
  }
}
