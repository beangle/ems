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

import org.beangle.data.model.IntId
import org.beangle.security.authc.PasswordPolicy
import org.beangle.ems.core.config.model.Domain

object PasswordConfig {
  def apply(policy: PasswordPolicy): PasswordConfig = {
    val config = new PasswordConfig
    val policy = PasswordPolicy.Medium
    config.minlen = policy.minlen
    config.maxlen = 16
    config.dcredit = policy.dcredit
    config.lcredit = policy.lcredit
    config.ucredit = policy.ucredit
    config.ocredit = policy.ocredit
    config.minclass = policy.minclass
    config
  }
}

/** 密码配置
 *
 */
class PasswordConfig extends IntId with PasswordPolicy {

  var domain: Domain = _
  /** 密码的最小长度 */
  var minlen: Int = 6
  /** 密码的最大长度 */
  var maxlen: Int = 64

  /** 密码可更改的最小天数 */
  var mindays: Int = 0
  /** 密码保持有效的最大天数 > minDays */
  var maxdays: Int = 999
  /** 用户密码到期前，提前收到警告信息的天数 */
  var warnage: Int = 7
  /** 密码停滞的天数 */
  var idledays: Int = 999

  /** 密码中最少含有多少个数字 */
  var dcredit: Int = 0
  /** 密码中最少含有多少个小写字母 */
  var lcredit: Int = 0
  /** 密码中最少含有多少个大写字母 */
  var ucredit: Int = 0
  /** 密码中最少含有多少个其他字母 */
  var ocredit: Int = 0
  /** 密码中最少含有几类字符 */
  var minclass: Int = 2

  /** 是否检查密码中含有用户名 */
  var usercheck: Boolean = _

}
