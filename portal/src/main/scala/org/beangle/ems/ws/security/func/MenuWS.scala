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

import org.beangle.commons.collection.Properties
import org.beangle.data.dao.EntityDao
import org.beangle.ems.core.config.model.{ChannelType, Env}
import org.beangle.ems.core.config.service.AppService
import org.beangle.ems.core.security.model.Menu
import org.beangle.ems.core.security.service.{AppMenus, DomainMenus, GroupMenus, MenuService}
import org.beangle.ems.core.user.service.UserService
import org.beangle.webmvc.annotation.{action, mapping, param, response}
import org.beangle.webmvc.support.ActionSupport

@action("/security/func/{app}/menus")
class MenuWS extends ActionSupport {

  var menuService: MenuService = _

  var appService: AppService = _

  var userService: UserService = _

  var entityDao: EntityDao = _

  @response
  def index(@param("app") appName: String): collection.Seq[Any] = {
    val menus = appService.getApp(appName) match {
      case Some(app) => menuService.getTopMenus(app)
      case None => List.empty[Menu]
    }
    val isEnName = get("request_locale", "zh_CN").startsWith("en")
    menus map (m => menuService.convert(m, isEnName))
  }

  @response()
  @mapping("user/{user}")
  def user(@param("app") appName: String, @param("user") username: String): Any = {
    val user = userService.get(username)
    if (user.isEmpty) {
      return "{}"
    }
    val isEnName = get("request_locale", "zh_CN").startsWith("en")
    val u = user.get
    val app = appService.getApp(appName)
    val forDomain = getBoolean("forDomain", defaultValue = false)

    val channelType = ChannelType.of(get("channel",ChannelType.Pc))
    val env = getLong("profileId").map(id => entityDao.get(classOf[Env], id))
    app match {
      case Some(app) =>
        if (forDomain) {
          menuService.getDomainMenus(u, channelType, isEnName, env)
        } else {
          val menus = menuService.getTopMenus(app, u, channelType)
          val appProps = new Properties(app, "id", "name", "base", "logoUrl")
          appProps.put("title", app.getTitle(isEnName))
          menus.headOption.foreach { m =>
            appProps.put("embedMode", m.channel.embedMode.name)
            if (m.channel.base != null && m.channel.base.nonEmpty) {
              appProps.put("base", m.channel.base)
            }
          }
          val menuProps = menus map (m => menuService.convert(m, isEnName))
          val domain = new Properties(app.domain, "id", "name")
          domain.put("title", app.domain.getTitle(isEnName))
          val group = new Properties(app.group, "id", "name", "indexno")
          group.put("title", app.group.getTitle(isEnName))
          DomainMenus(domain, List(GroupMenus(group, List(AppMenus(appProps, menuProps)))))
        }
      case None =>
        menuService.getDomainMenus(u, channelType, isEnName, env)
    }
  }
}
