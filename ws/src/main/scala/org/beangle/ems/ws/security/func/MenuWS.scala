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
package org.beangle.ems.ws.security.func

import org.beangle.commons.collection.{Collections, Properties}
import org.beangle.data.dao.EntityDao
import org.beangle.webmvc.api.action.{ActionSupport, EntitySupport}
import org.beangle.webmvc.api.annotation.{mapping, param, response}
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.ems.core.security.model.Menu
import org.beangle.ems.core.security.service.MenuService
import org.beangle.ems.core.user.model.User
import org.beangle.ems.core.user.service.UserService

import scala.collection.mutable

class MenuWS extends ActionSupport with EntitySupport[Menu] {

  var entityDao: EntityDao = _

  var menuService: MenuService = _

  var appService: AppService = _

  var domainService: DomainService = _

  var userService: UserService = _

  @response
  def index(@param("app") appName: String): collection.Seq[Any] = {
    val menus = appService.getApp(appName) match {
      case Some(app) => menuService.getTopMenus(app)
      case None => List.empty[Menu]
    }
    menus map (m => convert(m))
  }

  @response
  @mapping("user/{user}")
  def user(@param("app") appName: String, @param("user") username: String): Any = {
    val up = userService.get(username)
    if (up.isEmpty) {
      return "{}"
    }
    val u = up.get
    val app = appService.getApp(appName)
    val forDomain = getBoolean("forDomain", defaultValue = false)
    app match {
      case Some(app) =>
        if (forDomain) {
          getDomainMenus(u)
        } else {
          val appProps = new Properties(app, "id", "name", "title", "base", "url", "logoUrl", "navStyle")
          val menus = menuService.getTopMenus(app, u) map (m => convert(m))
          val domain = new Properties(app.domain, "id", "name", "title")
          val group = new Properties(app.group, "id", "name", "title", "indexno")
          DomainMenus(domain, List(GroupMenus(group, List(AppMenus(appProps, menus)))))
        }
      case None =>
        getDomainMenus(u)
    }
  }

  private def getDomainMenus(u: User): DomainMenus = {
    val menus = menuService.getTopMenus(u)
    val appsMenus = menus.groupBy(_.app)
    val groupApps = appsMenus.keys.groupBy(_.group)
    val directMenuMaps = groupApps map {
      case (oned, _) =>
        val group = new Properties(oned, "id", "name", "title", "indexno")
        val appMenus = groupApps(oned).toBuffer.sorted map { app =>
          val appProps = new Properties(app, "id", "name", "title", "base", "url", "logoUrl", "navStyle")
          AppMenus(appProps, appsMenus(app).map(convert))
        }
        (oned, GroupMenus(group, appMenus))
    }

    val groups = Collections.newBuffer[GroupMenus]
    directMenuMaps.keys.toSeq.sorted foreach { g =>
      groups += directMenuMaps(g)
    }
    val domain = new Properties(domainService.getDomain, "id", "name", "title")
    DomainMenus(domain, groups)
  }

  private def convert(one: Menu): Properties = {
    val menu = new Properties(one, "id", "title", "indexno")
    if (one.entry.isDefined) menu.put("entry", one.entry.get.name + (if (one.params.isDefined) "?" + one.params.get else ""))
    if (one.children.nonEmpty) {
      val children = new mutable.ListBuffer[Properties]
      one.children foreach { child =>
        children += convert(child)
      }
      menu.put("children", children)
    }
    menu
  }
}

case class AppMenus(app: Properties, menus: Iterable[Properties])

case class GroupMenus(group: Properties, appMenus: Iterable[AppMenus])

case class DomainMenus(domain: Properties, groups: Iterable[GroupMenus]);
