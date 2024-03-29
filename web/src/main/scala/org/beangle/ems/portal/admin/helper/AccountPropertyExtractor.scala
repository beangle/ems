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

package org.beangle.ems.portal.admin.helper

import org.beangle.commons.bean.DefaultPropertyExtractor
import org.beangle.ems.core.user.model.Account

class AccountPropertyExtractor extends DefaultPropertyExtractor {
  override def get(target: Object, property: String): Any = {
    if (property == "roleNames") {
      val account = target.asInstanceOf[Account]
      account.user.roles.filter(x => x.role.domain == account.domain && x.member).map(_.role.name).mkString(",")
    } else {
      super.get(target, property)
    }
  }

}
