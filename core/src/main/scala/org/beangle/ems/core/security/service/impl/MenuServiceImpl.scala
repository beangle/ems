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
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.data.model.util.Hierarchicals
import org.beangle.ems.core.config.model.App
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.security.model.{FuncPermission, FuncResource, Menu}
import org.beangle.ems.core.security.service.{AppMenus, DomainMenus, GroupMenus, MenuService}
import org.beangle.ems.core.user.model.{Role, User}
import org.beangle.security.authz.Scope

import scala.collection.mutable

/**
 * @author chaostone
 */
class MenuServiceImpl(val entityDao: EntityDao) extends MenuService {

  var domainService: DomainService = _

  private def getRoles(user: User): Seq[Role] = {
    val domain = domainService.getDomain
    val roles = user.roles.filter(m => m.member && m.role.domain == domain).map { m => m.role }
    user.group foreach { g =>
      roles.addAll(g.roles filter (r => r.domain == domain))
    }
    user.groups foreach { gm =>
      roles.addAll(gm.group.roles filter (r => r.domain == domain))
    }
    roles.toSet.toSeq //去重后返回
  }

  def getTopMenus(app: App, user: User): collection.Seq[Menu] = {
    getTopMenus(Some(app), getRoles(user))
  }

  def getTopMenus(user: User): collection.Seq[Menu] = {
    getTopMenus(None, getRoles(user))
  }

  def getTopMenus(app: App, role: Role): collection.Seq[Menu] = {
    getTopMenus(Some(app), List(role))
  }

  private def getTopMenus(app: Option[App], roles: Iterable[Role]): collection.Seq[Menu] = {
    val menuSet = Collections.newSet[Menu]
    roles foreach { role =>
      val query = OqlBuilder.from[Menu](classOf[Menu].getName + " menu," + classOf[FuncPermission].getName + " fp")
        .where("menu.enabled=true")
        .where("fp.role =:role", role)
        .where("fp.resource=menu.entry")
        .select("menu")

      app foreach { p => query.where("menu.app=:app", p) }
      query.where("menu.app.domain =:domain and menu.app.enabled=true", domainService.getDomain)

      query.cacheable()

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
    val builder = OqlBuilder.from(classOf[Menu]).where("menu.app= :app and menu.parent = null", app).orderBy("menu.indexno").cacheable()
    entityDao.search(builder)
  }

  override def getMenus(app: App): collection.Seq[Menu] = {
    val builder = OqlBuilder.from(classOf[Menu]).where("menu.app= :app", app).orderBy("menu.indexno").cacheable()
    entityDao.search(builder)
  }

  private def buildMenuQuery(app: App, role: Role): OqlBuilder[Menu] = {
    val builder = OqlBuilder.from(classOf[Menu])
    builder.join("menu.resources", "mr")
    builder.where("exists(from " + classOf[FuncPermission].getName
      + " a where a.role=:role and a.resource=mr)", role)
    builder.where("mr=menu.entry")
    builder.where("menu.app = :app", app)
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
          .where("m.app = :app and m.parent is null", menu.app)
          .orderBy("m.indexno")
        Hierarchicals.move(menu, entityDao.search(builder).toBuffer, index)
      }
    entityDao.saveOrUpdate(nodes)

    if (null != location) {
      entityDao.refresh(location)
    }
  }

  def importFrom(app: App, xml: scala.xml.Node): Unit = {
    parseMenu(app, None, xml)
  }

  override def getDomainMenus(user: User, isEnName: Boolean): DomainMenus = {
    val menus = getTopMenus(user)
    val appsMenus = menus.groupBy(_.app)
    val groupApps = appsMenus.keys.groupBy(_.group)
    val directMenuMaps = groupApps map {
      case (oned, _) =>
        val group = new Properties(oned, "id", "name", "indexno")
        group.put("title", if isEnName then oned.enTitle else oned.title)
        val appMenus = groupApps(oned).toBuffer.sorted map { app =>
          val appProps = new Properties(app, "id", "name", "base", "url", "logoUrl", "navStyle")
          appProps.put("title", if isEnName then app.enTitle else app.title)
          AppMenus(appProps, appsMenus(app).map(x => convert(x, isEnName)))
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

  private def parseMenu(app: App, parent: Option[Menu], xml: scala.xml.Node): Unit = {
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
      val menu = findMenu(app, parent, name).getOrElse(new Menu)
      menu.name = name
      menu.indexno = indexno
      menu.app = app

      val enName = m \ "@enName"
      menu.enName = if enName.isEmpty then menu.name else enName.text.trim
      val params = m \ "@params"
      if (params.isEmpty || Strings.isBlank(params.text)) {
        menu.params = None
      } else {
        menu.params = Some(params.text.trim)
      }
      val enabled = m \ "@enabled"
      if (enabled.isEmpty) {
        menu.enabled = true
      } else {
        menu.enabled = enabled.text.trim.toBoolean
      }
      (m \ "@fonticon") foreach { fi =>
        menu.fonticon = Some(fi.text.trim)
      }

      val entry = findFuncResource(app, (m \ "@entry").text.trim)
      val resources = m \ "@resources"
      if resources.nonEmpty then
        Strings.split(resources.text) foreach { n =>
          findFuncResource(app, n) foreach { r => menu.resources += r }
        }
      entry foreach { r => menu.resources += r }
      menu.entry = entry
      menu.parent = parent
      entityDao.saveOrUpdate(menu)
      val children = m \ "children"
      if (children.nonEmpty) {
        parseMenu(app, Some(menu), children.head)
      }
    }
  }

  private def findMenu(app: App, parent: Option[Menu], name: String): Option[Menu] = {
    val builder = OqlBuilder.from(classOf[Menu], "m").where("m.app=:app", app)
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
