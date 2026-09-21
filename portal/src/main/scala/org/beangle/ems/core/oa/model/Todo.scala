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
import org.beangle.data.model.pojo.Updatable
import org.beangle.ems.core.config.model.{Business, Domain}
import org.beangle.ems.core.user.model.User

import java.time.Instant
import scala.compiletime.uninitialized

/** 代办 */
class Todo extends LongId, Updatable {
  /** 用户 */
  var user: User = uninitialized
  /** 领域 */
  var domain: Domain = uninitialized
  /** 标题 */
  var title: String = uninitialized
  /** 内容 */
  var contents: String = uninitialized
  /** 业务主键 */
  var businessKey: String = uninitialized
  /** 业务类型 */
  var business: Business = uninitialized
  /** 是否完成 */
  var done: Boolean = uninitialized
  /** 处理代办的地址 */
  var url: String = uninitialized
  /** 发送到外部系统 0 表示不需要，1表示需要 2表示已经发送到外部系统 */
  var smsStatus: Int = uninitialized

  def this(user: User, title: String, contents: String, business: Business, businessKey: String) = {
    this()
    this.domain = business.domain
    this.business = business
    this.businessKey = businessKey
    this.title = title
    this.contents = contents
    this.user = user
    this.done = false
    this.updatedAt = Instant.now
  }
}
