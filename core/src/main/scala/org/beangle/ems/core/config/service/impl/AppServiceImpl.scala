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
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.app.Ems
import org.beangle.ems.core.config.model.*
import org.beangle.ems.core.config.service.{AppService, DomainService}

/**
 * @author chaostone
 */
class AppServiceImpl(entityDao: EntityDao) extends AppService with Initializing {

  private var appTypes: Map[String, AppType] = _
  var domainService: DomainService = _

  override def init(): Unit = {
    val rs = entityDao.getAll(classOf[AppType])
    appTypes = rs.map(x => (x.name, x)).toMap
  }

  override def getAppType(typeName: String): AppType = {
    appTypes(typeName)
  }

  override def getGroups(): Seq[AppGroup] = {
    val query = OqlBuilder.from(classOf[AppGroup], "ag")
    query.where("ag.domain=:domain", domainService.getDomain)
    entityDao.search(query)
  }

  override def getCredentials(): Seq[Credential] = {
    val query = OqlBuilder.from(classOf[Credential], "c")
    query.where("c.domain=:domain", domainService.getDomain)
    entityDao.search(query)
  }

  override def getApp(name: String, secret: String): Option[App] = {
    val query = OqlBuilder.from(classOf[App], "app")
      .where("app.name=:name and app.secret=:secret", name, secret)
      .where("app.domain=:domain", domainService.getDomain)
      .cacheable()
    entityDao.search(query).headOption
  }

  override def getApp(name: String): Option[App] = {
    val query = OqlBuilder.from(classOf[App], "app")
      .where("app.name=:name ", name)
      .where("app.domain=:domain", domainService.getDomain)
      .cacheable()
    entityDao.search(query).headOption
  }

  override def getWebapps: Seq[App] = {
    entityDao.search(OqlBuilder.from(classOf[App], "app")
      .where("app.appType.name=:typ and app.enabled=true", AppType.Webapp)
      .where("app.domain=:domain", domainService.getDomain)
      .orderBy("app.group.indexno,app.indexno"))
  }

  override def getApps: Seq[App] = {
    entityDao.search(OqlBuilder.from(classOf[App], "app")
      .where("app.enabled=true")
      .where("app.domain=:domain", domainService.getDomain)
      .orderBy("app.group.indexno,app.indexno"))
  }
}
