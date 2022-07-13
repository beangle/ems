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

package org.beangle.ems.portal.admin.action.session

import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.session.model.SessionEvent
import org.beangle.webmvc.support.action.RestfulAction
import org.beangle.webmvc.support.helper.QueryHelper

import java.time.LocalDate

class EventAction extends RestfulAction[SessionEvent] {
  var domainService: DomainService = _

  override protected def indexSetting(): Unit = {
    put("beginOn", LocalDate.of(LocalDate.now().getYear, 1, 1))
    put("endOn", LocalDate.of(LocalDate.now().getYear, 12, 31))
    super.indexSetting()
  }

  override protected def getQueryBuilder: OqlBuilder[SessionEvent] = {
    val query = super.getQueryBuilder
    QueryHelper.dateBetween(query, "sessionEvent", "updatedAt", "beginOn", "endOn")
    query.where("sessionEvent.domain=:domain", domainService.getDomain)
  }
}
