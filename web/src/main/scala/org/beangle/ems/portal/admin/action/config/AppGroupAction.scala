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

package org.beangle.ems.portal.admin.action.config

import org.beangle.data.dao.OqlBuilder
import org.beangle.web.action.annotation.ignore
import org.beangle.web.action.view.View
import org.beangle.webmvc.support.action.RestfulAction
import org.beangle.ems.core.config.model.AppGroup
import org.beangle.ems.core.config.service.DomainService

class AppGroupAction extends RestfulAction[AppGroup] {

  var domainService: DomainService = _

  @ignore
  override protected def saveAndRedirect(group: AppGroup): View = {
    group.domain = domainService.getDomain
    super.saveAndRedirect(group)
  }

  override protected def getQueryBuilder: OqlBuilder[AppGroup] = {
      super.getQueryBuilder.where("appGroup.domain=:domain", domainService.getDomain)
  }
}
