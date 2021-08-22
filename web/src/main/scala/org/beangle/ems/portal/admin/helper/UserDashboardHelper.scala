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

import org.beangle.data.dao.EntityDao
import org.beangle.security.session.SessionRegistry
import org.beangle.web.action.context.ActionContext
import org.beangle.ems.core.security.service.{FuncPermissionService, MenuService, ProfileService}
import org.beangle.ems.core.user.model.{Account, User, UserProfile}
import org.beangle.ems.core.user.service.DimensionService

/**
 * @author chaostone
 */
class UserDashboardHelper {

  var entityDao: EntityDao = _

  var sessionRegistry: SessionRegistry = _

  var permissionService: FuncPermissionService = _

  var menuService: MenuService = _

  var profileService: ProfileService = _

  var dimensionService: DimensionService = _

  def buildDashboard(user: User): Unit = {
    ActionContext.current.attribute("user", user)
    entityDao.findBy(classOf[Account], "user", List(user)) foreach { c =>
      ActionContext.current.attribute("credential", c)
    }
    val myProfiles = entityDao.findBy(classOf[UserProfile], "user", List(user))
    new ProfileHelper(entityDao, profileService, dimensionService).populateInfo(myProfiles)
  }
}
