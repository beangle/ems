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
import org.beangle.commons.xml.Document
import org.beangle.data.dao.OqlBuilder
import org.beangle.data.model.util.Hierarchicals
import org.beangle.ems.core.config.model.{App, ChannelType}
import org.beangle.ems.core.security.model.{Channel, FuncPermission, FuncResource, Menu}
import org.beangle.ems.core.security.service.MenuService
import org.beangle.ems.portal.action.admin.DomainSupport
import org.beangle.ems.portal.helper.AppHelper
import org.beangle.event.bus.DataEvent
import org.beangle.she.webmvc.RestfulAction
import org.beangle.webmvc.annotation.{ignore, param}
import org.beangle.webmvc.view.View

class MenuAction extends RestfulAction[Menu], DomainSupport {
  var menuService: MenuService = _

  private val appParam = "menu.channel.app.id"
  private val channelTypeParam = "menu.channel.channelType.id"

  protected override def indexSetting(): Unit = {
    val apps = appService.getWebapps
    AppHelper.putApps(apps, appParam, entityDao)
    val channelTypes = entityDao.getAll(classOf[ChannelType]).sortBy(_.id)
    put("channelTypes", channelTypes)
    put("current_channelType", resolveChannelType(channelTypes))
  }

  override def search(): View = {
    AppHelper.remember(appParam)
    val app = entityDao.get(classOf[App], getInt(appParam).get)
    val domain = domainService.getDomain
    for (profile <- domain.sashubProfile; url <- domain.sashubBase) {
      put("remoteMenuURL", url + s"/api/${profile}/ems/menus/${app.name}.xml")
    }
    super.search()
    forward()
  }

  override def getQueryBuilder: OqlBuilder[Menu] = {
    val builder = super.getQueryBuilder
    builder.where("menu.channel.app.domain=:domain", domainService.getDomain)
    val channelTypeId = getInt(channelTypeParam).getOrElse(ChannelType.PcId)
    builder.where("menu.channel.channelType.id=:channelTypeId", channelTypeId)
    builder
  }

  protected override def editSetting(menu: Menu): Unit = {
    ensureChannel(menu)
    val channel = menu.channel

    val folders = Collections.newBuffer[Menu]
    val folderBuilder = OqlBuilder.from(classOf[Menu], "m")
    folderBuilder.where("m.route is null and m.channel=:channel", channel)
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
    val funcBuilder = OqlBuilder.from(classOf[FuncResource], "r").where("r.app=:app", channel.app)
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
    ensureChannel(menu)
    val resources = entityDao.find(classOf[FuncResource], getIntIds("resource"))
    menu.resources.clear()
    menu.resources ++= resources

    val newParentId = getInt("parent.id")
    val indexno = getInt("indexno", 0)
    var parent: Menu = null
    if (newParentId.isDefined) parent = entityDao.get(classOf[Menu], newParentId.get)

    menuService.move(menu, parent, indexno)
    entityDao.saveOrUpdate(menu)
    if (!menu.enabled) {
      val family = Hierarchicals.getFamily(menu)
      for (one <- family) one.enabled = false
      entityDao.saveOrUpdate(family)
    }
    entityDao.evict(classOf[Menu])
    databus.publish(DataEvent.update(menu))
    redirect("search", "info.save.success")
  }

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
      put("resources", entityDao.findBy(classOf[FuncResource], "app", menus.head.app))
    else
      put("resources", List.empty[FuncResource])
    forward()
  }

  def displayRemoteMenu(): View = {
    val domain = domainService.getDomain
    val app = entityDao.get(classOf[App], getInt(appParam).get)
    for (profile <- domain.sashubProfile; url <- domain.sashubBase) {
      val remoteUrl = url + s"/api/${profile}/ems/menus/${app.name}.xml"
      put("remoteMenuURL", remoteUrl)
      val res = HttpUtils.get(remoteUrl)
      put("remoteContent", res.getText)
      put("remoteResponse", res.status)
    }
    forward()
  }

  def importFormRemote(): View = {
    val domain = domainService.getDomain
    val app = entityDao.get(classOf[App], getInt(appParam).get)
    var remoteUrl: Option[String] = None
    for (profile <- domain.sashubProfile; url <- domain.sashubBase) {
      remoteUrl = Some(url + s"/api/${profile}/ems/menus/${app.name}.xml")
    }
    remoteUrl foreach { rl =>
      menuService.importFrom(app, Document.parse(Networks.url(rl)))
    }
    redirect("search", "info.save.success")
  }

  def importFromXml(): View = {
    val parts = getAll("menufile", classOf[Part])
    if (parts.isEmpty) {
      forward()
    } else {
      val app = entityDao.get(classOf[App], getInt(appParam).get)
      menuService.importFrom(app, Document.parse(parts.head.getInputStream))
      redirect("search", "info.save.success")
    }
  }

  private def ensureChannel(menu: Menu): Unit = {
    if (menu.channel != null) return
    getInt("menu.channel.id") match {
      case Some(cid) => menu.channel = entityDao.get(classOf[Channel], cid)
      case None =>
        val appId = getInt(appParam).orElse(getInt("menu.app.id"))
        val channelType = resolveChannelType(entityDao.getAll(classOf[ChannelType]))
        appId foreach { id =>
          menu.channel = resolveChannel(entityDao.get(classOf[App], id), channelType)
        }
    }
  }

  private def resolveChannelType(channelTypes: Iterable[ChannelType]): ChannelType = {
    getInt(channelTypeParam) match {
      case Some(id) => channelTypes.find(_.id == id).getOrElse(entityDao.get(classOf[ChannelType], id))
      case None =>
        channelTypes.find(_.id == ChannelType.PcId)
          .orElse(channelTypes.find(_.name == ChannelType.Pc))
          .getOrElse(entityDao.get(classOf[ChannelType], ChannelType.PcId))
    }
  }

  private def resolveChannel(app: App, channelType: ChannelType): Channel = {
    entityDao.search(OqlBuilder.from(classOf[Channel], "c")
      .where("c.app=:app and c.channelType=:channelType", app, channelType)
      .orderBy("c.id")).headOption.getOrElse {
      throw new IllegalStateException(s"App ${app.name} has no Channel for ${channelType.name}")
    }
  }
}
