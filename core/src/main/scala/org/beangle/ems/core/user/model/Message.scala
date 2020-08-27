/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2020, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.ems.core.user.model

import org.beangle.data.model.LongId
import java.time.Instant

object Message {
  val Newly = 1
  val Readed = 2
  val InTrash = 3
}

class Message extends LongId {
  /** 标题 */
  var title: String = _

  /** 内容 */
  var contents: String = _

  /** 发送人 */
  var sender: User = _

  /**接受人*/
  var recipient: User = _

  /** 消息状态 */
  var status: Int = _

  /** 发送日期 */
  var sentAt: Instant = _

}
