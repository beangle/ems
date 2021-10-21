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

package org.beangle.ems.ws.security.data

import org.beangle.commons.collection.Properties
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.web.action.support.{ActionSupport, EntitySupport}
import org.beangle.web.action.annotation.{param, response}
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.security.model.DataResource

class ResourceWS extends ActionSupport with EntitySupport[DataResource] {

  var entityDao: EntityDao = _

  var domainService: DomainService = _

  @response
  def info(@param("name") name: String): Properties = {
    val query = OqlBuilder.from(classOf[DataResource], "dr")
      .where("dr.name=:name", name)
      .where("dr.domain=:domain", domainService.getDomain)
    val resources = entityDao.search(query)
    if (resources.nonEmpty) new Properties(resources.head, "id", "name", "title", "scope")
    else new Properties
  }

}
