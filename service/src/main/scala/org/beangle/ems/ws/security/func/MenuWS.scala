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

package org.beangle.ems.ws.security.func

import org.beangle.commons.collection.{Collections, Properties}
import org.beangle.data.dao.EntityDao
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.ems.core.security.model.Menu
import org.beangle.ems.core.security.service.MenuService
import org.beangle.ems.core.user.model.User
import org.beangle.ems.core.user.service.UserService
import org.beangle.web.action.annotation.{mapping, param, response}
import org.beangle.web.action.context.ActionContext
import org.beangle.web.action.support.{ActionSupport, EntitySupport}

import java.util.Locale
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
    val isEnName = get("request_locale","zh_CN").startsWith("en")
    menus map (m => convert(m, isEnName))
  }

  @response(cacheable = true)
  @mapping("user/{user}")
  def user(@param("app") appName: String, @param("user") username: String): Any = {
    val user = userService.get(username)
    if (user.isEmpty) {
      return "{}"
    }
    val isEnName = get("request_locale","zh_CN").startsWith("en")
    val u = user.get
    val app = appService.getApp(appName)
    val forDomain = getBoolean("forDomain", defaultValue = false)
    app match {
      case Some(app) =>
        if (forDomain) {
          getDomainMenus(u, isEnName)
        } else {
          val appProps = new Properties(app, "id", "name", "title", "base", "url", "logoUrl", "navStyle")
          val menus = menuService.getTopMenus(app, u) map (m => convert(m, isEnName))
          val domain = new Properties(app.domain, "id", "name", "title")
          val group = new Properties(app.group, "id", "name", "title", "indexno")
          DomainMenus(domain, List(GroupMenus(group, List(AppMenus(appProps, menus)))))
        }
      case None =>
        getDomainMenus(u, isEnName)
    }
  }

  private def getDomainMenus(u: User, isEnName: Boolean): DomainMenus = {
    val menus = menuService.getTopMenus(u)
    val appsMenus = menus.groupBy(_.app)
    val groupApps = appsMenus.keys.groupBy(_.group)
    val directMenuMaps = groupApps map {
      case (oned, _) =>
        val group = new Properties(oned, "id", "name", "title", "indexno")
        val appMenus = groupApps(oned).toBuffer.sorted map { app =>
          val appProps = new Properties(app, "id", "name", "title", "base", "url", "logoUrl", "navStyle")
          AppMenus(appProps, appsMenus(app).map(x => convert(x, isEnName)))
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

  private def convert(one: Menu, isEnName: Boolean): Properties = {
    val menu = new Properties(one, "id", "fonticon", "indexno")
    menu.put("title", if isEnName then one.enName else one.name)
    if (one.entry.isDefined) menu.put("entry", one.entry.get.name + (if (one.params.isDefined) "?" + one.params.get else ""))
    if (one.children.nonEmpty) {
      val children = new mutable.ListBuffer[Properties]
      one.children foreach { child =>
        children += convert(child, isEnName)
      }
      menu.put("children", children)
    }
    menu
  }
}

case class AppMenus(app: Properties, menus: Iterable[Properties])

case class GroupMenus(group: Properties, appMenus: Iterable[AppMenus])

case class DomainMenus(domain: Properties, groups: Iterable[GroupMenus]);
