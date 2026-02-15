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

package org.beangle.ems.portal.action.admin.log

import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.app.log.Level
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.ems.core.log.model.ErrorLog
import org.beangle.she.webmvc.{ExportSupport, RestfulAction}
import org.beangle.she.webmvc.QueryHelper

class ErrorAction extends RestfulAction[ErrorLog], ExportSupport[ErrorLog] {

  var appService: AppService = _
  var domainService: DomainService = _

  override protected def indexSetting(): Unit = {
    put("apps", appService.getApps)
    put("levels", Level.values)
    super.indexSetting()
  }

  override protected def getQueryBuilder: OqlBuilder[ErrorLog] = {
    val builder = super.getQueryBuilder
    builder.where("errorLog.app.domain=:domain", domainService.getDomain)
    QueryHelper.dateBetween(builder, null, "occurredAt", "beginOn", "endOn")
    builder
  }

}
