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
import org.beangle.data.model.pojo.{Named, Updated}
import org.beangle.ems.core.config.model.Business

/** 消息模板
 */
class MessageTemplate extends LongId, Updated, Named {
  /** 业务类型 */
  var business: Business = _
  /** 标题模板 */
  var title: String = _
  /** 内容模板 */
  var contents: String = _
  /** 变量 */
  var variables: Option[String] = None
  /** 是否是代办消息 */
  var todo: Boolean = _
  /** 延迟发送时间 */
  var delayMinutes: Int = _
}
