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
package org.beangle.ems.portal.admin.action.security

import jakarta.servlet.http.Part
import org.beangle.commons.collection.Collections
import org.beangle.data.dao.OqlBuilder
import org.beangle.data.model.util.Hierarchicals
import org.beangle.webmvc.api.annotation.{ignore, param}
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.entity.action.RestfulAction
import org.beangle.ems.portal.admin.helper.AppHelper
import org.beangle.ems.core.config.model.App
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.ems.core.security.model.{FuncPermission, FuncResource, Menu}
import org.beangle.ems.core.security.service.MenuService

class MenuAction extends RestfulAction[Menu] {
  var menuService: MenuService = _
  var appService: AppService = _
  var domainService: DomainService = _

  protected override def indexSetting(): Unit = {
    val apps = appService.getWebapps
    AppHelper.putApps(apps, "menu.app.id", entityDao)
  }

  override def search(): View = {
    AppHelper.remember("menu.app.id")
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

    var folders = Collections.newBuffer[Menu]
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
  protected override def removeAndRedirect(entities: Seq[Menu]): View = {
    val parents = Collections.newBuffer[Menu]
    for (menu <- entities) {
      menu.parent foreach { p =>
        p.children -= menu
        parents += p
      }
    }
    entityDao.saveOrUpdate(parents)
    super.removeAndRedirect(entities)
  }

  @ignore
  protected override def saveAndRedirect(menu: Menu): View = {
    val resources = entityDao.find(classOf[FuncResource], intIds("resource"))
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
    entityDao.evict(menu)
    if (null != parent) {
      entityDao.evict(parent)
    }
    redirect("search", "info.save.success")
  }

  /**
   * 禁用或激活一个或多个模块
   */
  def activate(): View = {
    val menuIds = intIds("menu")
    val enabled = getBoolean("isActivate", defaultValue = true)

    val updated = Collections.newSet[Menu]
    val menus = entityDao.find(classOf[Menu], menuIds)
    for (menu <- menus) {
      updated ++= (if (enabled) Hierarchicals.getPath(menu) else Hierarchicals.getFamily(menu))
    }
    for (menu <- updated) menu.enabled = enabled
    entityDao.saveOrUpdate(updated)
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
    forward()
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
