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
package org.beangle.ems.admin

import org.beangle.cdi.bind.BindModule
import org.beangle.ems.admin.action.security._
import org.beangle.ems.admin.action.user._
import org.beangle.ems.admin.helper.UserDashboardHelper

class SecurityModule extends BindModule {

  protected override def binding(): Unit = {
    bind(classOf[AccountAction], classOf[DashboardAction], classOf[AvatarAction])
    bind(classOf[PasswordConfigAction])
    bind(classOf[DimensionAction], classOf[PermissionAction], classOf[UserAction],
      classOf[RoleAction], classOf[ProfileAction])

    bind(classOf[FuncResourceAction], classOf[MenuAction])
    bind(classOf[DataPermissionAction])
    bind(classOf[DataResourceAction])

    //bind(classOf[IndexAction])

    bind(classOf[action.session.IndexAction])
    bind(classOf[action.session.ConfigAction])
    bind(classOf[action.session.EventAction])

    bind("userDashboardHelper", classOf[UserDashboardHelper])
  }
}
