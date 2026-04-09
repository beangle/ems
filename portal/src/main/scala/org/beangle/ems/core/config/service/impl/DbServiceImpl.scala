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

package org.beangle.ems.core.config.service.impl

import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.core.config.model.Db
import org.beangle.ems.core.config.service.{DbService, DomainService}

class DbServiceImpl(entityDao: EntityDao) extends DbService {

  var domainService: DomainService = _

  override def getAll(): Seq[Db] = {
    val query = OqlBuilder.from(classOf[Db], "o")
    query.where("o.domain=:domain", domainService.getDomain)
    entityDao.search(query)
  }

}
