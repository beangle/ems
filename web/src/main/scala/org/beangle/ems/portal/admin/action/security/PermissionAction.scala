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

package org.beangle.ems.portal.admin.action.security

import org.beangle.commons.lang.Numbers
import org.beangle.data.dao.OqlBuilder
import org.beangle.security.Securities
import org.beangle.web.action.annotation.{mapping, param}
import org.beangle.web.action.context.ActionContext
import org.beangle.web.action.view.View
import org.beangle.webmvc.support.action.RestfulAction
import org.beangle.ems.app.EmsApp
import org.beangle.ems.portal.admin.helper.AppHelper
import org.beangle.ems.core.config.model.App
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.ems.core.security.model.{FuncPermission, FuncResource, Menu}
import org.beangle.ems.core.security.service.{FuncPermissionService, MenuService}
import org.beangle.ems.core.user.model.{Role, User}
import org.beangle.ems.core.user.service.UserService

/**
 * 权限分配与管理响应类
 * @author chaostone 2005-10-9
 */
class PermissionAction extends RestfulAction[FuncPermission] {

  var menuService: MenuService = _
  var funcPermissionService: FuncPermissionService = _
  var userService: UserService = _
  var appService: AppService = _
  var domainService: DomainService = _

  /**
   * 根据菜单配置来分配权限
   */
  @mapping(value = "{role.id}/edit")
  override def edit(@param("role.id") id: String): View = {
    val roleId = Numbers.toInt(id)
    val role = entityDao.get(classOf[Role], roleId)
    val user = userService.get(Securities.user).head
    put("manager", user)
    val isPlatformRoot = userService.isRoot(user, EmsApp.name)
    val mngRoles = new collection.mutable.ListBuffer[Role]
    val roleQuery = OqlBuilder.from(classOf[Role], "r").orderBy("r.indexno")
    roleQuery.where("r.domain=:domain", domainService.getDomain)
    val roles = entityDao.search(roleQuery)
    val granterRoles = user.roles filter (m => m.granter) map (m => m.role)
    for (r <- roles) {
      if (granterRoles.contains(r) || isPlatformRoot) mngRoles += r
    }
    put("mngRoles", mngRoles)
    val apps = appService.getWebapps
    AppHelper.putApps(apps, "app.id", entityDao)

    val app: App = ActionContext.current.attribute("current_app")
    val mngMenus = new collection.mutable.ListBuffer[Menu]
    if (null != app) {
      var mngResources: collection.Seq[Object] = null
      if (userService.isRoot(user, app.name) || isPlatformRoot) {
        mngMenus ++= menuService.getMenus(app)
        mngResources = funcPermissionService.getResources(app)
      } else {
        mngResources = new collection.mutable.ListBuffer[FuncResource]
        val params = new collection.mutable.HashMap[String, Any]
        val hql = "select distinct fp.resource from " + classOf[FuncPermission].getName + " fp where fp.role.id = :roleId"
        val menuSet = new collection.mutable.HashSet[Menu]
        for (m <- user.roles) {
          if (m.granter) {
            menuSet ++= menuService.getMenus(app, m.role)
            params.put("roleId", m.role.id)
            mngResources ++= entityDao.search(OqlBuilder.oql[FuncResource](hql).params(params))
          }
        }
        mngMenus ++= menuSet.toList.sorted
      }
      put("mngResources", mngResources.toSet)
      val displayFreezen = getBoolean("displayFreezen", defaultValue = false)
      if (!displayFreezen) {
        val freezed = new collection.mutable.ListBuffer[Menu]
        for (menu <- mngMenus) if (!menu.enabled) freezed += menu
        mngMenus --= freezed
      }
      val permissions = funcPermissionService.getPermissions(app, role)
      val roleMenus = menuService.getMenus(app, role)
      val roleResources = permissions.map(p => p.resource).toSet
      put("roleMenus", roleMenus.toSet)
      put("roleResources", roleResources)

      val parents = new collection.mutable.HashSet[Role]
      val parentResources = new collection.mutable.HashSet[FuncResource]
      val parentMenus = new collection.mutable.HashSet[Menu]
      var parent = role.parent.orNull
      while (null != parent && !parents.contains(parent)) {
        val parentPermissions = funcPermissionService.getPermissions(app, parent)
        parentMenus ++= menuService.getMenus(app, parent)
        for (permission <- parentPermissions) {
          parentResources += permission.resource
        }
        parents += parent
        parent = parent.parent.orNull
      }
      put("parentMenus", parentMenus)
      put("parentResources", parentResources)
    } else {
      put("roleMenus", Set.empty)
      put("roleResources", Set.empty)
      put("parentMenus", Set.empty)
      put("parentResources", Set.empty)
    }
    put("mngMenus", mngMenus)
    put("role", role)
    forward()
  }

  /**
   * 显示权限操作提示界面
   */
  def prompt(): View = {
    forward()
  }

  /**
   * 保存模块级权限
   */
  override def save(): View = {
    val role = entityDao.get(classOf[Role], getIntId("role"))
    val app = entityDao.get(classOf[App], getIntId("app"))
    val newResources = entityDao.findBy(classOf[FuncResource], "id", getIntIds("resource")).toSet

    // 管理员拥有的菜单权限和系统资源
    val manager = entityDao.findBy(classOf[User], "code", List(Securities.user)).head
    var mngMenus: collection.Set[Menu] = null
    val mngResources = new collection.mutable.HashSet[FuncResource]
    if (userService.isRoot(manager, app.name)) {
      mngMenus = menuService.getMenus(app).toSet
    } else {
      mngMenus = menuService.getMenus(app, manager).toSet
    }
    for (m <- mngMenus) {
      mngResources ++= m.resources
    }

    newResources.dropWhile(p => !mngResources.contains(p))
    funcPermissionService.authorize(app, role, newResources)

    val where = to(this, "edit")
    where.param("role.id", role.id).param("app.id", app.id)
    val displayFreezen = get("displayFreezen")
    if (null != displayFreezen) where.param("displayFreezen", displayFreezen)

    redirect(where, "info.save.success")
  }

}
