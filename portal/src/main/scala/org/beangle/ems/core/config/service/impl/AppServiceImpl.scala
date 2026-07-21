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
import org.beangle.ems.core.config.model.*
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.ems.core.security.model.Channel

/**
 * @author chaostone
 */
class AppServiceImpl(entityDao: EntityDao) extends AppService, Initializing {

  private var channelTypes: Map[String, ChannelType] = _
  var domainService: DomainService = _

  override def init(): Unit = {
    val rs = entityDao.getAll(classOf[ChannelType])
    channelTypes = rs.map(x => (x.name, x)).toMap
  }

  override def getChannelType(typeName: String): ChannelType = {
    channelTypes(typeName)
  }

  override def getGroups: Seq[AppGroup] = {
    val query = OqlBuilder.from(classOf[AppGroup], "ag")
    query.where("ag.domain=:domain", domainService.getDomain)
    entityDao.search(query)
  }

  override def getCredentials: Seq[Credential] = {
    val query = OqlBuilder.from(classOf[Credential], "c")
    query.where("c.domain=:domain", domainService.getDomain)
    entityDao.search(query)
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
      .where("app.enabled=true and app.domain=:domain", domainService.getDomain)
      .where(s"exists(from ${classOf[Channel].getName} c where c.app=app)")
      .orderBy("app.group.indexno,app.indexno"))
  }

  override def getApps: Seq[App] = {
    entityDao.search(OqlBuilder.from(classOf[App], "app")
      .where("app.enabled=true")
      .where("app.domain=:domain", domainService.getDomain)
      .orderBy("app.group.indexno,app.indexno"))
  }

  override def getChannels(channelType: ChannelType): Seq[Channel] = {
    entityDao.search(OqlBuilder.from(classOf[Channel], "c")
      .where("c.enabled=true and c.app.domain=:domain", domainService.getDomain)
      .where("c.channelType=:channelType", channelType)
      .orderBy("c.app.group.indexno,c.app.indexno"))
  }
}
