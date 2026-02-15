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
import org.beangle.webmvc.annotation.ignore
import org.beangle.webmvc.view.View
import org.beangle.she.webmvc.RestfulAction
import org.beangle.ems.core.config.model.Credential
import org.beangle.ems.core.config.service.DomainService

class CredentialAction extends RestfulAction[Credential] {

  var domainService: DomainService = _

  @ignore
  override protected def saveAndRedirect(credential: Credential): View = {
    credential.domain = domainService.getDomain
    super.saveAndRedirect(credential)
  }

  override protected def getQueryBuilder: OqlBuilder[Credential] = {
    val builder = super.getQueryBuilder
    builder.where("credential.domain=:domain", domainService.getDomain)
    builder
  }
}
