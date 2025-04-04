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
import org.beangle.ems.core.config.model.{Business, Domain}
import org.beangle.ems.core.user.model.User

import java.time.Instant

/** 代办 */
class Todo extends LongId, Updated {
  /** 用户 */
  var user: User = _
  /** 领域 */
  var domain: Domain = _
  /** 内容 */
  var contents: String = _
  /** 业务主键 */
  var businessKey: String = _
  /** 业务类型 */
  var business: Business = _
  /** 是否完成 */
  var done: Boolean = _
  /** 处理代办的地址 */
  var url: String = _

  def this(flow: Flow, user: User, contents: String, businessKey: String) = {
    this()
    this.business = flow.business
    this.businessKey = businessKey
    this.contents = contents
    this.user = user
    this.domain = flow.domain
    this.done = false
    this.updatedAt = Instant.now
  }
}
