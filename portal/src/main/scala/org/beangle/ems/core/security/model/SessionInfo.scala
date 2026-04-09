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

/** 会话信息
  * 先不要持久化，该模型处于历史原因使用jdbc进行存储
  */
class SessionInfo extends Serializable {
  var domain: Domain = _
  var id: String = _
  var principal: String = _
  var description: Option[String] = _
  var category: Category = _
  var ip: Option[String] = _
  var agent: Option[String] = _
  var os: Option[String] = _
  var loginAt: Instant = _
  var lastAccessAt: Instant = _

}
