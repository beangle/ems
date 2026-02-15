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

package org.beangle.ems.portal.action.admin.oa

import org.beangle.commons.lang.Strings
import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.core.config.model.Business
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.oa.model.FlowProcess
import org.beangle.she.webmvc.RestfulAction

class FlowProcessAction extends RestfulAction[FlowProcess] {

  var domainService: DomainService = _

  override def indexSetting(): Unit = {
    super.indexSetting()
    put("businesses", getBusinesses())
  }

  private def getBusinesses(): Seq[Business] = {
    entityDao.findBy(classOf[Business], "domain", domainService.getDomain)
  }

  override protected def getQueryBuilder: OqlBuilder[FlowProcess] = {
    val query = super.getQueryBuilder
    query.where("process.flow.domain=:domain", domainService.getDomain)
    get("initiator") foreach { i =>
      if (Strings.isNotBlank(i)) {
        query.where("process.initiator.code like :initiator or process.initiator.name like :initiator", s"%${i}%")
      }
    }
    query
  }

  override protected def simpleEntityName: String = {
    "process"
  }

}
