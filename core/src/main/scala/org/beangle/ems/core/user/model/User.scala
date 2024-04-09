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

package org.beangle.ems.core.user.model

import org.beangle.commons.collection.Collections
import org.beangle.data.model.LongId
import org.beangle.data.model.pojo.*
import org.beangle.ems.core.config.model.Org

import java.security.Principal
import java.time.LocalDate

/**
 * @author chaostone
 */

class User extends LongId, Coded, Named, Updated, TemporalOn, Principal, Remark, Enabled {
  /** 组织 */
  var org: Org = _
  /** 角色 */
  var roles = Collections.newBuffer[RoleMember]
  /** 用户组 */
  var groups = Collections.newBuffer[GroupMember]
  /** 身份 */
  var category: Category = _
  /** 照片ID */
  var avatarId: Option[String] = None
  /** 是否锁定 */
  var locked: Boolean = _
  /** 密码 */
  var password: String = _
  /** 密码过期日期 */
  var passwdExpiredOn: LocalDate = _
  /** 移动电话 */
  var mobile: Option[String] = None
  /** 电子邮件 */
  var email: Option[String] = None

  def accountExpired: Boolean = {
    endOn match {
      case Some(e) => LocalDate.now.isAfter(e)
      case None => false
    }
  }

  def passwdExpired: Boolean = {
    LocalDate.now.isAfter(passwdExpiredOn)
  }

  def passwdInactive(idleDays: Int): Boolean = {
    LocalDate.now.isAfter(passwdExpiredOn.plusDays(idleDays))
  }

  override def getName: String = {
    name
  }
}
