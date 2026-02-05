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

package org.beangle.ems.portal.helper

import org.beangle.commons.codec.digest.Digests
import org.beangle.commons.collection.page.PageLimit
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.app.Ems
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.security.model.SessionEvent
import org.beangle.ems.core.security.service.{FuncPermissionService, MenuService, ProfileService, SessionInfoService}
import org.beangle.ems.core.user.model.{Profile, User}
import org.beangle.ems.core.user.service.DimensionService
import org.beangle.webmvc.context.ActionContext

/**
  * @author chaostone
  */
class UserDashboardHelper {

  var entityDao: EntityDao = _

  var permissionService: FuncPermissionService = _

  var menuService: MenuService = _

  var profileService: ProfileService = _

  var dimensionService: DimensionService = _

  var sessionInfoService: SessionInfoService = _

  var domainService: DomainService = _

  def buildDashboard(user: User): Unit = {
    ActionContext.current.attribute("user", user)
    ActionContext.current.attribute("sessioninfoes", sessionInfoService.find(Some(user.code), new PageLimit(1, 20), None))
    val myProfiles = entityDao.findBy(classOf[Profile], "user", List(user))
    val menus = menuService.getDomainMenus(user, false)
    ActionContext.current.attribute("menus", menus)

    ActionContext.current.attribute("avatar_url", Ems.api + "/platform/user/avatars/" + Digests.md5Hex(user.code)+"?t="+System.currentTimeMillis())

    val seQuery = OqlBuilder.from(classOf[SessionEvent], "se")
    seQuery.where("se.domain=:domain and se.principal=:principal", domainService.getDomain, user.code)
    seQuery.orderBy("se.updatedAt desc")
    ActionContext.current.attribute("sessionEvents", entityDao.topN(20, seQuery))
    new ProfileHelper(entityDao, profileService, dimensionService).populateInfo(myProfiles)
  }
}
