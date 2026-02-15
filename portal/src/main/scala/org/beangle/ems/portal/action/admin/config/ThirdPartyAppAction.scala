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

package org.beangle.ems.portal.action.admin.config

import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.core.config.model.ThirdPartyApp
import org.beangle.ems.portal.action.admin.DomainSupport
import org.beangle.webmvc.annotation.ignore
import org.beangle.she.webmvc.RestfulAction
import org.beangle.webmvc.view.View

import java.time.Instant

class ThirdPartyAppAction extends RestfulAction[ThirdPartyApp], DomainSupport {

  override protected def simpleEntityName = "app"

  override protected def getQueryBuilder: OqlBuilder[ThirdPartyApp] = {
    val builder = super.getQueryBuilder
    builder.where("app.domain=:domain", domainService.getDomain)
    builder
  }

  @ignore
  override protected def saveAndRedirect(app: ThirdPartyApp): View = {
    app.domain = domainService.getDomain
    app.updatedAt = Instant.now
    entityDao.saveOrUpdate(app)
    publishUpdate(app)
    redirect("search", "info.save.success")
  }
}
