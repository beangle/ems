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

import org.beangle.commons.bean.Initializing
import org.beangle.data.dao.EntityDao
import org.beangle.ems.app.Ems
import org.beangle.ems.core.config.model.{Domain, Org}
import org.beangle.ems.core.config.service.DomainService

class DomainServiceImpl extends DomainService, Initializing {
  var entityDao: EntityDao = _

  private var domain: Domain = _
  private var org: Org = _

  override def getDomain: Domain = {
    domain
  }

  override def getOrg: Org = {
    org
  }

  override def init(): Unit = {
    val rs = entityDao.findBy(classOf[Domain], "hostname", List(Ems.hostname))
    rs.headOption match {
      case Some(d) =>
        domain = d
        org = entityDao.get(classOf[Org], domain.org.id)
      case None =>
        throw new RuntimeException("Cannot find domain with hostname :" + Ems.hostname)
    }
  }
}
