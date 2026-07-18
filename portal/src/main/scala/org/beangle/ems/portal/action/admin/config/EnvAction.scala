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
import org.beangle.ems.core.config.model.Env
import org.beangle.ems.portal.action.admin.DomainSupport
import org.beangle.she.webmvc.RestfulAction
import org.beangle.webmvc.annotation.ignore
import org.beangle.webmvc.view.View

class EnvAction extends RestfulAction[Env], DomainSupport {

  @ignore
  override protected def saveAndRedirect(env: Env): View = {
    env.domain = domainService.getDomain
    saveOrUpdate(env)
    publishUpdate(env)
    super.saveAndRedirect(env)
  }

  override protected def getQueryBuilder: OqlBuilder[Env] = {
    super.getQueryBuilder.where("env.domain=:domain", domainService.getDomain)
  }
}
