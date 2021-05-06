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
package org.beangle.ems.portal.admin.action.config

import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.core.config.model.Reconfig
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.webmvc.entity.action.RestfulAction

class ReconfigAction extends RestfulAction[Reconfig] {
  var domainService: DomainService = _
  var appService: AppService = _

  override protected def indexSetting(): Unit = {
    put("apps", appService.getWebapps)
  }

  override protected def getQueryBuilder: OqlBuilder[Reconfig] = {
    val query = super.getQueryBuilder
    val domain = domainService.getDomain
    query.where("reconfig.app.domain=:domain", domain)
  }

  override protected def editSetting(entity: Reconfig): Unit = {
    put("apps", appService.getWebapps)
    super.editSetting(entity)
  }
}
