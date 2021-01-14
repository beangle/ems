/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2020, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.ems.ws.user

import org.beangle.commons.collection.{Collections, Properties}
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.webmvc.api.action.{ActionSupport, EntitySupport}
import org.beangle.webmvc.api.annotation.{mapping, param, response}
import org.beangle.ems.core.config.model.{App, AppType}
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.security.model.FuncPermission
import org.beangle.ems.core.user.model.{Root, User}
import org.beangle.ems.core.user.service.UserService

/**
 * @author chaostone
 */
class AppWS(userService: UserService, entityDao: EntityDao) extends ActionSupport with EntitySupport[User] {

  var domainService: DomainService = _

  @response(cacheable = true)
  @mapping("{userCode}")
  def index(@param("userCode") userCode: String): collection.Seq[Properties] = {
    userService.get(userCode) match {
      case Some(user) =>
        val domain = domainService.getDomain
        val fpAppQuery = OqlBuilder.from[App](classOf[FuncPermission].getName, "fp")
          .join("fp.role.members", "m")
          .where("m.user=:user and m.member=true", user)
          .where("fp.resource.app.enabled=true")
          .where("fp.resource.app.domain=:domain", domain)
          .where(s"fp.resource.app.appType.name='${AppType.Webapp}'")
          .select("distinct fp.resource.app").cacheable()

        val fpApps = entityDao.search(fpAppQuery)

        val apps = Collections.newSet[App]
        apps ++= fpApps

        val rootsQuery = OqlBuilder.from(classOf[Root], "root")
          .where("root.app.domain=:domain", domain)
          .where(s"root.user=:user and root.app.enabled=true and root.app.appType.name='${AppType.Webapp}'", user)
          .cacheable()
        val roots = entityDao.search(rootsQuery)
        apps ++= roots.map(a => a.app)
        var appBuffer = apps.toBuffer.sorted
        get("q") foreach { q =>
          appBuffer = appBuffer.filter(a => a.title.contains(q))
        }
        appBuffer.map { app =>
          val p = new Properties(app, "id", "name", "title", "base", "url", "logoUrl", "navStyle")
          p.add("group", app.group, "id", "name", "title")
          p
        }
      case None => Seq.empty
    }
  }
}
