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

package org.beangle.ems.ws.user

import org.beangle.commons.collection.{Collections, Properties}
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.core.config.model.{App, ChannelType}
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.ems.core.security.model.{Channel, FuncPermission}
import org.beangle.ems.core.user.model.{Root, User}
import org.beangle.ems.core.user.service.UserService
import org.beangle.webmvc.annotation.{mapping, param, response}
import org.beangle.webmvc.support.ActionSupport

/**
 * @author chaostone
 */
class AppWS(userService: UserService, entityDao: EntityDao) extends ActionSupport {

  var appService: AppService = _
  var domainService: DomainService = _

  @response(cacheable = true)
  @mapping("{userCode}")
  def index(@param("userCode") userCode: String): collection.Seq[Properties] = {
    userService.get(userCode) match {
      case Some(user) =>
        val domain = domainService.getDomain
        val webapp = appService.getChannelType(ChannelType.Pc)
        val fpAppQuery = OqlBuilder.from[App](classOf[FuncPermission].getName, "fp")
          .join("fp.role.members", "m")
          .where("m.user=:user and m.member=true", user)
          .where("fp.resource.app.enabled=true")
          .where("fp.resource.app.domain=:domain", domain)
          .where("exists(from " + classOf[Channel].getName + " c where c.app=fp.resource.app and c.channelType=:webapp)", webapp)
          .select("distinct fp.resource.app").cacheable()

        val fpApps = entityDao.search(fpAppQuery)

        val apps = Collections.newSet[App]
        apps ++= fpApps

        var appBuffer = apps.toBuffer.sorted
        get("q") foreach { q =>
          appBuffer = appBuffer.filter(a => a.title.contains(q))
        }
        appBuffer.map { app =>
          val p = new Properties(app, "id", "name", "title", "base", "logoUrl")
          entityDao.search(OqlBuilder.from(classOf[Channel], "c")
            .where("c.app=:app and c.channelType=:webapp", app, webapp)
            .cacheable()).headOption.foreach { c =>
            p.put("embedMode", c.embedMode.name)
            if (c.base != null && c.base.nonEmpty) p.put("base", c.base)
          }
          p.add("group", app.group, "id", "name", "title")
          p
        }
      case None => Seq.empty
    }
  }
}
