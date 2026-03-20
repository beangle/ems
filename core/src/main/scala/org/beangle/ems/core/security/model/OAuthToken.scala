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

import org.beangle.data.model.LongId
import org.beangle.ems.core.config.model.ThirdPartyApp
import org.beangle.ems.core.user.model.User

import java.time.Instant

class OAuthToken extends LongId {

  /** oauth */
  var token: String = _

  /** 客户端ID (ThirdPartyApp) */
  var client: ThirdPartyApp = _

  /** 授权用户ID (User.code) */
  var user: User = _

  /** 授权范围，如 "read write profile"，多个用空格分隔 */
  var scope: String = _

  /** 颁发时间 */
  var issuedAt: Instant = _

  /** 过期时间 */
  var expiresAt: Instant = _
}
