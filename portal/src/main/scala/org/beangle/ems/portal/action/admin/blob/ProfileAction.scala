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

package org.beangle.ems.portal.action.admin.blob

import org.beangle.commons.lang.Strings
import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.core.blob.model.Profile
import org.beangle.ems.core.config.model.App
import org.beangle.ems.core.config.service.DomainService
import org.beangle.webmvc.view.View
import org.beangle.she.webmvc.RestfulAction

class ProfileAction extends RestfulAction[Profile] {

  var domainService: DomainService = _

  override protected def getQueryBuilder: OqlBuilder[Profile] = {
    val builder = super.getQueryBuilder
    builder.where("profile.domain=:domain", domainService.getDomain)
  }

  override def saveAndRedirect(entity: Profile): View = {
    entity.domain = domainService.getDomain
    super.saveAndRedirect(entity)
  }
}
