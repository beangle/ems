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
package org.beangle.ems.portal.admin.action.user

import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.user.model.PasswordConfig
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.entity.action.RestfulAction

class PasswordConfigAction extends RestfulAction[PasswordConfig] {

  var domainService: DomainService = _

  override def index(): View = {
    val builder = getQueryBuilder
    builder.where("passwordConfig.domain=:domain", domainService.getDomain)
    put("passwordConfigs", entityDao.search(builder))
    forward()
  }

  override protected def saveAndRedirect(entity: PasswordConfig): View = {
    entity.domain = domainService.getDomain
    saveOrUpdate(entity)
    redirect("index", "info.save.success")
  }
}
