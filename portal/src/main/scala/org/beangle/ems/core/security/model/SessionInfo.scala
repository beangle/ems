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

package org.beangle.ems.core.security.model

import org.beangle.ems.core.config.model.Domain
import org.beangle.ems.core.user.model.Category

import java.time.Instant
import scala.compiletime.uninitialized

/** 会话信息
  * 先不要持久化，该模型处于历史原因使用jdbc进行存储
  */
class SessionInfo extends Serializable {
  var domain: Domain = uninitialized
  var id: String = uninitialized
  var principal: String = uninitialized
  var description: Option[String] = uninitialized
  var category: Category = uninitialized
  var ip: Option[String] = uninitialized
  var agent: Option[String] = uninitialized
  var os: Option[String] = uninitialized
  var loginAt: Instant = uninitialized
  var lastAccessAt: Instant = uninitialized

}
