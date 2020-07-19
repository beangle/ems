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

import java.time.LocalDate

import org.beangle.data.model.LongId
import org.beangle.data.model.pojo.{Enabled, TemporalOn, Updated}
import org.beangle.ems.core.config.model.Domain

class Account extends LongId with Updated with Enabled with TemporalOn {

  var domain: Domain = _

  var user: User = _

  var locked: Boolean = _

  var password: String = _

  var passwdExpiredOn: LocalDate = _

  var passwdInactiveOn: LocalDate = _

  def accountExpired: Boolean = {
    endOn match {
      case Some(e) => LocalDate.now.isAfter(e)
      case None => false
    }
  }

  def passwdExpired: Boolean = {
    LocalDate.now.isAfter(passwdExpiredOn)
  }

  def passwdInactive: Boolean = {
    LocalDate.now.isAfter(passwdInactiveOn)
  }
}
