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

package org.beangle.ems.portal.action.admin.user

import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.user.model.{Group, Role}
import org.beangle.web.action.view.View
import org.beangle.webmvc.support.action.RestfulAction

class GroupAction extends RestfulAction[Group] {

  var domainService: DomainService = _

  override protected def editSetting(entity: Group): Unit = {
    val parents = entityDao.findBy(classOf[Group], "org.id", domainService.getOrg.id).toBuffer
    if (entity.persisted) {
      parents.subtractOne(entity)
    } else {
      entity.enabled = true
    }
    put("parents", parents.sortBy(_.indexno))
    put("roles", entityDao.findBy(classOf[Role], "domain.id", domainService.getDomain.id))
    super.editSetting(entity)
  }

  override protected def saveAndRedirect(group: Group): View = {
    group.org = domainService.getOrg
    val roleIds = getAll("role.id", classOf[Int])
    val newRoles = entityDao.find(classOf[Role], roleIds)
    group.roles.clear()
    group.roles.addAll(newRoles)
    super.saveAndRedirect(group)
  }
}
