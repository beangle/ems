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

package org.beangle.ems.core.security.service.impl

import org.beangle.commons.collection.{Collections, Properties}
import org.beangle.commons.lang.Strings
import org.beangle.commons.xml.Node
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.data.model.util.Hierarchicals
import org.beangle.ems.core.config.model.{App, ChannelType, Env}
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.security.model.*
import org.beangle.ems.core.security.service.{AppMenus, DomainMenus, GroupMenus, MenuService}
import org.beangle.ems.core.user.model.{Role, User}
import org.beangle.security.authz.Scope

import scala.collection.mutable

/**
 * @author chaostone
 */
class MenuServiceImpl(val entityDao: EntityDao) extends MenuService {

  var domainService: DomainService = _

  private def getRoles(user: User, env: Option[Env]): Seq[Role] = {
    val domain = domainService.getDomain
    val roles = user.roles.filter(m => m.member && m.role.domain == domain && env.forall(m.suitable)).map(_.role)
    user.group foreach { g =>
      roles.addAll(g.roles filter (r => r.domain == domain))
    }
    user.groups foreach { gm =>
      roles.addAll(gm.group.roles filter (r => r.domain == domain))
    }
    //去重后返回
    env match {
      case None => roles.toSet.toSeq
      case Some(e) => roles.toSet.filter(_.suitable(e)).toSeq
    }
  }

  override def getTopMenus(app: App, user: User, channelType: ChannelType): collection.Seq[Menu] = {
    getTopMenus(Some(app), channelType, getRoles(user, None), None)
  }

  override def getTopMenus(user: User, channelType: ChannelType, env: Option[Env]): collection.Seq[Menu] = {
    getTopMenus(None, channelType, getRoles(user, env), env)
  }

  def getTopMenus(app: App, role: Role, channelType: ChannelType): collection.Seq[Menu] = {
    getTopMenus(Some(app), channelType, List(role), None)
  }

  /**
   * 解析可用 Channel：端类型 + 启用状态 + 域；可选限定 App；有 env 时再按 App.envIds 过滤。
   */
  private def findChannels(app: Option[App], channelType: ChannelType, env: Option[Env]): Seq[Channel] = {
    val query = OqlBuilder.from(classOf[Channel], "c")
      .where("c.channelType=:channelType", channelType)
      .where("c.enabled=true and c.app.enabled=true and c.app.domain=:domain", domainService.getDomain)
    app.foreach(a => query.where("c.app=:app", a))
    val channels = entityDao.search(query.cacheable())
    env match {
      case Some(e) => channels.filter(_.app.suitable(e))
      case None => channels
    }
  }

  /**
   * 在指定场景下，各角色因 RoleAppEnv 限定而不可用的应用。
   * 某角色在某应用上无 RoleAppEnv 记录表示不限制，不会出现在结果中；
   * 有记录但不包含该 env 时，该应用记入该角色的不可用集合。
   */
  private def findUnavailableApps(roles: Iterable[Role], env: Env): Map[Role, Set[App]] = {
    val roleList = roles.toSeq
    if (roleList.isEmpty) return Map.empty
    val raes = entityDao.search(OqlBuilder.from(classOf[RoleAppEnv], "rae")
      .where("rae.role in (:roles)", roleList)
      .cacheable())
    raes.groupBy(_.role).flatMap { case (role, list) =>
      val unavailable = list.groupBy(_.app).collect {
        case (app, items) if !items.exists(_.env.id == env.id) => app
      }.toSet
      if (unavailable.isEmpty) None else Some(role -> unavailable)
    }
  }

  private def getTopMenus(app: Option[App], channelType: ChannelType, roles: Iterable[Role], env: Option[Env]): collection.Seq[Menu] = {
    val channels = findChannels(app, channelType, env)
    if (channels.isEmpty) return Nil

    val unavailableByRole = env.map(e => findUnavailableApps(roles, e)).getOrElse(Map.empty)
    val menuSet = Collections.newSet[Menu]
    roles foreach { role =>
      val roleChannels = unavailableByRole.get(role) match {
        case Some(apps) if apps.nonEmpty => channels.filterNot(c => apps.contains(c.app))
        case _ => channels
      }
      if (roleChannels.nonEmpty) {
        val query = OqlBuilder.from[Menu](classOf[Menu].getName + " menu," + classOf[FuncPermission].getName + " fp")
          .where("menu.enabled=true")
          .where("fp.role =:role", role)
          .where("exists(from menu.resources r where r=fp.resource)")
          .where("menu.channel in (:channels)", roleChannels)
          .select("menu")
          .cacheable()

        entityDao.search(query).foreach { m =>
          menuSet += m
          var p = m.parent.orNull
          while (null != p) {
            if (!menuSet.contains(p)) {
              menuSet += p
              p = p.parent.orNull
            } else {
              p = null
            }
          }
        }
      }
    }

    val menus = Collections.newBuffer[Menu]
    menuSet foreach { m =>
      if (m.parent.isEmpty) {
        menus += m
        reserveChildren(m, menuSet)
      }
    }
    menus.sorted
  }

  private def reserveChildren(menu: Menu, menus: collection.Set[Menu]): Unit = {
    menu.children --= menu.children.filter(f => !menus.contains(f))
    menu.children.foreach { c => reserveChildren(c, menus) }
  }

  def getMenus(app: App, role: Role): collection.Seq[Menu] = {
    val query = buildMenuQuery(app, role)
    query.where("menu.enabled = true")
    val menus = Collections.newSet[Menu]
    menus ++= entityDao.search(query)
    addParentMenus(menus)
  }

  def getMenus(app: App, user: User): collection.Seq[Menu] = {
    val menus = Collections.newSet[Menu]
    for (rm <- user.roles) {
      if (rm.member) {
        val query = buildMenuQuery(app, rm.role)
        query.where("menu.enabled= true")
        menus ++= entityDao.search(query)
      }
    }
    addParentMenus(menus)
  }

  override def getTopMenus(app: App): collection.Seq[Menu] = {
    val builder = OqlBuilder.from(classOf[Menu]).where("menu.channel.app= :app and menu.parent = null", app).orderBy("menu.indexno").cacheable()
    entityDao.search(builder)
  }

  override def getMenus(app: App): collection.Seq[Menu] = {
    val builder = OqlBuilder.from(classOf[Menu]).where("menu.channel.app= :app", app).orderBy("menu.indexno").cacheable()
    entityDao.search(builder)
  }

  private def buildMenuQuery(app: App, role: Role): OqlBuilder[Menu] = {
    val builder = OqlBuilder.from(classOf[Menu])
    builder.join("menu.resources", "mr")
    builder.where("exists(from " + classOf[FuncPermission].getName
      + " a where a.role=:role and a.resource=mr)", role)
    builder.where("menu.channel.app = :app", app)
    builder.cacheable()
    builder
  }

  private def addParentMenus(menus: collection.mutable.Set[Menu]): collection.Seq[Menu] = {
    Hierarchicals.addParent(menus)
    menus.toList.sorted
  }

  def move(menu: Menu, location: Menu, index: Int): Unit = {
    menu.parent foreach { p =>
      if (null == location || p != location) {
        menu.parent = None
        entityDao.saveOrUpdate(menu)
        entityDao.refresh(p)
      }
    }

    val nodes =
      if (null != location) {
        Hierarchicals.move(menu, location, index)
      } else {
        val builder = OqlBuilder.from(classOf[Menu], "m")
          .where("m.channel = :channel and m.parent is null", menu.channel)
          .orderBy("m.indexno")
        Hierarchicals.move(menu, entityDao.search(builder).toBuffer, index)
      }
    entityDao.saveOrUpdate(nodes)

    if null != location then entityDao.refresh(location)
    if null == menu.indexno then menu.indexno = index.toString
  }

  override def getDomainMenus(user: User, channelType: ChannelType, isEnName: Boolean, env: Option[Env]): DomainMenus = {
    val menus = getTopMenus(user, channelType, env)
    val appsMenus = menus.groupBy(_.app)
    val groupApps = appsMenus.keys.groupBy(_.group)
    val directMenuMaps = groupApps map {
      case (oned, _) =>
        val group = new Properties(oned, "id", "name", "indexno")
        group.put("title", if isEnName then oned.enTitle else oned.title)
        val appMenus = groupApps(oned).toBuffer.sorted map { app =>
          val menusOfApp = appsMenus(app)
          val appProps = new Properties(app, "id", "name", "logoUrl")
          appProps.put("title", if isEnName then app.enTitle else app.title)
          menusOfApp.headOption.foreach { m =>
            appProps.put("embedMode", m.channel.embedMode.name)
            appProps.put("base", m.channel.base)
          }
          AppMenus(appProps, menusOfApp.map(x => convert(x, isEnName)))
        }
        (oned, GroupMenus(group, appMenus))
    }

    val groups = Collections.newBuffer[GroupMenus]
    directMenuMaps.keys.toSeq.sorted foreach { g =>
      groups += directMenuMaps(g)
    }
    val domain = domainService.getDomain
    val domainp = new Properties(domain, "id", "name")
    domainp.put("title", domain.getTitle(isEnName))
    DomainMenus(domainp, groups)
  }

  override def convert(one: Menu, isEnName: Boolean): Properties = {
    val menu = new Properties(one, "id", "indexno")
    menu.put("title", if isEnName then one.enName else one.name)
    one.route.foreach(r => menu.put("route", r))
    one.icon.foreach(i => menu.put("icon", i))
    if (one.children.nonEmpty) {
      val children = new mutable.ListBuffer[Properties]
      one.children foreach { child =>
        children += convert(child, isEnName)
      }
      menu.put("children", children)
    }
    menu
  }

  def importFrom(app: App, xml: Node): Unit = {
    parseMenu(app, resolveDefaultChannel(app), None, xml)
  }

  /** 优先取 PC 端，否则取应用下第一个 Channel。 */
  private def resolveDefaultChannel(app: App): Channel = {
    val channels = entityDao.search(OqlBuilder.from(classOf[Channel], "c")
      .where("c.app=:app", app)
      .orderBy("c.id"))
    channels.find(_.channelType.name == ChannelType.Pc).orElse(channels.headOption).getOrElse {
      throw new IllegalStateException(s"App ${app.name} has no Channel; create a Channel before importing menus")
    }
  }

  private def parseMenu(app: App, channel: Channel, parent: Option[Menu], xml: Node): Unit = {
    (xml \ "resources" \ "resource") foreach { r =>
      val name = (r \ "@name").text.trim
      val title = (r \ "@title").text.trim
      val scope = (r \ "@scope").text.trim
      val enabled = (r \ "@enabled").text.trim
      val fr = findOrCreateFuncResource(app, name, title, scope, enabled)
      entityDao.saveOrUpdate(fr)
    }

    (xml \ "menu") foreach { m =>
      val indexno = (m \ "@indexno").text.trim
      val name = (m \ "@name").text.trim
      val menu = findMenu(channel, parent, name).getOrElse(new Menu)
      menu.name = name
      menu.indexno = indexno
      menu.channel = channel

      val enName = m \ "@enName"
      menu.enName = if enName.isEmpty then menu.name else enName.text.trim
      val enabled = m \ "@enabled"
      if (enabled.isEmpty) {
        menu.enabled = true
      } else {
        menu.enabled = enabled.text.trim.toBoolean
      }
      (m \ "@icon").headOption foreach { fi =>
        menu.icon = Some(fi.text.trim)
      }

      val routeAttr = (m \ "@route").text.trim
      menu.route = if Strings.isNotBlank(routeAttr) then Some(routeAttr) else None

      val resources = m \ "@resources"
      if resources.nonEmpty then
        Strings.split(resources.text) foreach { n =>
          findFuncResource(app, n) foreach { r => menu.resources += r }
        }
      menu.parent = parent
      entityDao.saveOrUpdate(menu)
      val children = m \ "children"
      if (children.nonEmpty) {
        parseMenu(app, channel, Some(menu), children.head)
      }
    }
  }

  private def findMenu(channel: Channel, parent: Option[Menu], name: String): Option[Menu] = {
    val builder = OqlBuilder.from(classOf[Menu], "m").where("m.channel=:channel", channel)
    parent match
      case None => builder.where("m.name=:name and m.parent is null", name)
      case Some(p) => builder.where("m.name=:name and m.parent=:p", name, p)
    entityDao.search(builder).headOption
  }

  private def findFuncResource(app: App, name: String): Option[FuncResource] = {
    val builder = OqlBuilder.from(classOf[FuncResource], "fr").where("fr.app=:app", app)
    builder.where("fr.name=:name", name)
    entityDao.search(builder).headOption
  }

  private def findOrCreateFuncResource(app: App, name: String, title: String, scope: String, enabled: String): FuncResource = {
    findFuncResource(app, name) match {
      case None =>
        val resource = new FuncResource()
        resource.app = app
        resource.name = name
        resource.title = title
        resource.scope = if Strings.isEmpty(scope) then Scope.Private else Scope.valueOf(scope)
        if (Strings.isEmpty(enabled)) {
          resource.enabled = true
        } else {
          resource.enabled = enabled.toBoolean
        }
        entityDao.saveOrUpdate(resource)
        resource
      case Some(r) => r
    }
  }
}
