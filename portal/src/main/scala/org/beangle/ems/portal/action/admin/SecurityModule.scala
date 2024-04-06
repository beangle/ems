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

package org.beangle.ems.portal.action.admin

import org.beangle.cdi.bind.BindModule
import org.beangle.ems.portal.helper.UserDashboardHelper

class SecurityModule extends BindModule {

  protected override def binding(): Unit = {
    bind(classOf[user.AccountAction], classOf[user.AvatarAction])
    bind(classOf[user.PasswordConfigAction], classOf[user.UserAction])
    bind(classOf[user.DimensionAction], classOf[user.RoleAction], classOf[user.ProfileAction])

    bind(classOf[security.FuncResourceAction], classOf[security.MenuAction])
    bind(classOf[security.DataPermissionAction], classOf[security.DashboardAction])
    bind(classOf[security.DataResourceAction], classOf[security.PermissionAction])

    bind(classOf[session.IndexAction])
    bind(classOf[session.ConfigAction])
    bind(classOf[session.EventAction])

    bind("userDashboardHelper", classOf[UserDashboardHelper])
  }
}
