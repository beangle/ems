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

package org.beangle.ems.portal.action.admin.security

import jakarta.servlet.http.Part
import org.beangle.commons.collection.Collections
import org.beangle.commons.net.Networks
import org.beangle.commons.net.http.HttpUtils
import org.beangle.data.dao.OqlBuilder
import org.beangle.data.model.util.Hierarchicals
import org.beangle.ems.core.config.model.App
import org.beangle.ems.core.security.model.{FuncPermission, FuncResource, Menu}
import org.beangle.ems.core.security.service.MenuService
import org.beangle.ems.portal.action.admin.DomainSupport
import org.beangle.ems.portal.helper.AppHelper
import org.beangle.event.bus.DataEvent
import org.beangle.web.action.annotation.{ignore, param}
import org.beangle.web.action.view.View
import org.beangle.webmvc.support.action.RestfulAction

class MenuAction extends RestfulAction[Menu], DomainSupport {
  var menuService: MenuService = _

  protected override def indexSetting(): Unit = {
    val apps = appService.getWebapps
    AppHelper.putApps(apps, "menu.app.id", entityDao)
  }

  override def search(): View = {
    AppHelper.remember("menu.app.id")
    val app = entityDao.get(classOf[App], getInt("menu.app.id").get)
    val domain = domainService.getDomain
    for (profile <- domain.sashubProfile; url <- domain.sashubBase) {
      put("remoteMenuURL", url + s"/api/${profile}/ems/menus/${app.name}.xml")
    }
    super.search()
    forward()
  }

  override def getQueryBuilder: OqlBuilder[Menu] = {
    val builder = super.getQueryBuilder
    builder.where("menu.app.domain=:domain", domainService.getDomain)
    builder
  }

  protected override def editSetting(menu: Menu): Unit = {
    //search profile in app scope
    val app = entityDao.get(classOf[App], menu.app.id)

    val folders = Collections.newBuffer[Menu]
    // 查找可以作为父节点的菜单
    val folderBuilder = OqlBuilder.from(classOf[Menu], "m")
    folderBuilder.where("m.entry is null and m.app=:app", app)
    folderBuilder.orderBy("m.indexno")
    val rs = entityDao.search(folderBuilder)
    folders ++= rs
    menu.parent foreach { p =>
      if (!folders.contains(p)) folders += p
    }
    folders --= Hierarchicals.getFamily(menu)
    put("parents", folders)

    val alternatives = Collections.newBuffer[FuncResource]
    val resources = Collections.newBuffer[FuncResource]
    val funcBuilder = OqlBuilder.from(classOf[FuncResource], "r").where("r.app=:app", app)
    alternatives ++= entityDao.search(funcBuilder)
    resources ++= alternatives
    alternatives --= menu.resources
    put("alternatives", alternatives)
    put("resources", resources)

    if (!menu.persisted) {
      menu.enabled = true
    }
  }

  @ignore
  protected override def removeAndRedirect(menus: Seq[Menu]): View = {
    val parents = Collections.newBuffer[Menu]
    val step1 = Collections.newSet[Menu]
    for (menu <- menus) {
      menu.parent foreach { p =>
        p.children -= menu
        step1 += menu
        parents += p
      }
    }
    entityDao.saveOrUpdate(parents)
    val step2 = menus.toBuffer
    step2 --= step1
    remove(step2)
    databus.publish(DataEvent.remove(menus))
    redirect("search", "info.remove.success")
  }

  @ignore
  protected override def saveAndRedirect(menu: Menu): View = {
    val resources = entityDao.find(classOf[FuncResource], getIntIds("resource"))
    menu.resources.clear()
    menu.resources ++= resources
    //检查入口资源是否在使用资源列表中
    menu.entry.foreach { entry =>
      if (!resources.contains(entry)) {
        menu.resources += entry
      }
    }

    val newParentId = getInt("parent.id")
    val indexno = getInt("indexno", 0)
    var parent: Menu = null
    if (newParentId.isDefined) parent = entityDao.get(classOf[Menu], newParentId.get)

    menuService.move(menu, parent, indexno)
    if (!menu.enabled) {
      val family = Hierarchicals.getFamily(menu)
      for (one <- family) one.enabled = false
      entityDao.saveOrUpdate(family)
    }

    //refresh all app menus and their children relationships
    menuService.getMenus(menu.app) foreach { m =>
      entityDao.refresh(m)
    }
    databus.publish(DataEvent.update(menu))
    redirect("search", "info.save.success")
  }

  /**
   * 禁用或激活一个或多个模块
   */
  def activate(): View = {
    val menuIds = getIntIds("menu")
    val enabled = getBoolean("isActivate", defaultValue = true)

    val updated = Collections.newSet[Menu]
    val menus = entityDao.find(classOf[Menu], menuIds)
    for (menu <- menus) {
      updated ++= (if (enabled) Hierarchicals.getPath(menu) else Hierarchicals.getFamily(menu))
    }
    for (menu <- updated) menu.enabled = enabled
    entityDao.saveOrUpdate(updated)
    databus.publish(DataEvent.update(menus))
    redirect("search", "info.save.success")
  }

  override def info(@param("id") id: String): View = {
    val menu = this.entityDao.get(classOf[Menu], Integer.parseInt(id))
    put("menu", menu)
    if (menu.resources.nonEmpty) {
      val roleQuery = OqlBuilder.from(classOf[FuncPermission], "auth")
      roleQuery.where("auth.resource in(:resources)", menu.resources).select("distinct auth.role")
      put("roles", entityDao.search(roleQuery))
    }
    forward()
  }

  def exportToXml(): View = {
    val query = getQueryBuilder
    query.limit(null)
    query.where("menu.parent is null")
    val menus = entityDao.search(query)
    put("menus", menus)

    if menus.nonEmpty then
      val app = menus.head.app
      put("resources", entityDao.findBy(classOf[FuncResource], "app", app))
    else
      put("resources", List.empty[FuncResource])
    forward()
  }

  def displayRemoteMenu(): View = {
    val domain = domainService.getDomain
    val app = entityDao.get(classOf[App], getInt("menu.app.id").get)
    for (profile <- domain.sashubProfile; url <- domain.sashubBase) {
      val remoteUrl = url + s"/api/${profile}/ems/menus/${app.name}.xml"
      put("remoteMenuURL", remoteUrl)
      val res = HttpUtils.getText(remoteUrl)
      put("remoteContent", res.getText)
      put("remoteResponse", res.status)
    }
    forward()
  }

  def importFormRemote(): View = {
    val domain = domainService.getDomain
    val app = entityDao.get(classOf[App], getInt("menu.app.id").get)
    var remoteUrl: Option[String] = None
    for (profile <- domain.sashubProfile; url <- domain.sashubBase) {
      remoteUrl = Some(url + s"/api/${profile}/ems/menus/${app.name}.xml")
    }
    remoteUrl foreach { rl =>
      menuService.importFrom(app, scala.xml.XML.load(Networks.openURL(rl).getInputStream))
    }
    redirect("search", "info.save.success")
  }

  def importFromXml(): View = {
    val parts = getAll("menufile", classOf[Part])
    if (parts.isEmpty) {
      forward()
    } else {
      val app = entityDao.get(classOf[App], getInt("menu.app.id").get)
      menuService.importFrom(app, scala.xml.XML.load(parts.head.getInputStream))
      redirect("search", "info.save.success")
    }
  }
}
