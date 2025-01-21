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

package org.beangle.ems.ws.oa

import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.data.json.JsonAPI
import org.beangle.data.json.JsonAPI.Context
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.oa.model.{Flow, FlowTask}
import org.beangle.ems.core.user.model.Group
import org.beangle.webmvc.annotation.{mapping, param, response}
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.support.{ActionSupport, ServletSupport}

class FlowWS(entityDao: EntityDao) extends ActionSupport, ServletSupport {
  var domainService: DomainService = _

  @mapping(value = "business/{businessCode}")
  @response
  def list(@param("businessCode") businessCode: String): AnyRef = {
    val query = OqlBuilder.from(classOf[Flow], "flow")
    query.where("flow.domain=:domain", domainService.getDomain)
    query.where("flow.business.code=:businessCode", businessCode)
    query.cacheable()
    val flows = entityDao.search(query)
    convert(flows, false)
  }

  @mapping(value = "{flowCode}")
  @response
  def info(@param("flowCode") flowCode: String): AnyRef = {
    val query = OqlBuilder.from(classOf[Flow], "flow")
    query.where("flow.domain=:domain", domainService.getDomain)
    query.where("flow.code=:flowCode", flowCode)
    query.cacheable()
    val flows = entityDao.search(query)
    convert(flows, true)
  }

  private def convert(docs: Iterable[Flow], hasDetail: Boolean): JsonAPI.Json = {
    given context: Context = JsonAPI.context(ActionContext.current.params)

    if (hasDetail) {
      context.filters.include(classOf[Flow], "id", "name", "code", "tasks")
      context.filters.include(classOf[FlowTask], "id", "name", "idx", "group")
      context.filters.include(classOf[Group], "id", "name")
    } else {
      context.filters.include(classOf[Flow], "id", "name", "code")
    }
    val resources = docs.map { g => JsonAPI.create(g, "") }
    JsonAPI.newJson(resources)
  }
}
