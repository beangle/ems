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

package org.beangle.ems.portal.admin.action.log

import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.core.bulletin.model.Doc
import org.beangle.ems.core.config.service.AppService
import org.beangle.ems.core.log.model.{BusinessLog, Level}
import org.beangle.webmvc.support.action.RestfulAction
import org.beangle.webmvc.support.helper.QueryHelper

class BusinessAction extends RestfulAction[BusinessLog] {

  var appService: AppService = _

  override protected def indexSetting(): Unit = {
    put("apps", appService.getApps)
    put("levels", entityDao.getAll(classOf[Level]))
    super.indexSetting()
  }

  override protected def getQueryBuilder: OqlBuilder[BusinessLog] = {
    val builder = super.getQueryBuilder
    QueryHelper.dateBetween(builder, null, "operateAt", "beginOn", "endOn")
    builder
  }

}
