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

import org.beangle.commons.concurrent.Timers
import org.beangle.commons.lang.Numbers
import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.config.model.{App, Env}
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.ems.core.security.model.{FuncPermission, FuncResource, Menu, RoleAppEnv}
import org.beangle.ems.core.security.service.{FuncPermissionService, MenuService}
import org.beangle.ems.core.user.model.{Role, User}
import org.beangle.ems.core.user.service.UserService
import org.beangle.ems.portal.action.admin.DomainSupport
import org.beangle.ems.portal.helper.AppHelper
import org.beangle.event.bus.{DataEvent, DataEventBus}
import org.beangle.event.mq.ChannelQueue
import org.beangle.security.Securities
import org.beangle.security.authz.{Authority, Authorizer}
import org.beangle.she.webmvc.RestfulAction
import org.beangle.webmvc.annotation.{mapping, param}
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.view.View

/**
 * 权限分配与管理响应类
 *
 * @author chaostone 2005-10-9
 */
class PermissionAction extends RestfulAction[FuncPermission], DomainSupport {

  var menuService: MenuService = _
  var funcPermissionService: FuncPermissionService = _
  var userService: UserService = _
  var publicChannel: ChannelQueue[DataEvent] = _
  var authorizer: Authorizer = _

  /**
   * 根据菜单配置来分配权限
   */
  @mapping(value = "{role.id}/edit")
  override def edit(@param("role.id") id: String): View = {
    val roleId = Numbers.toInt(id)
    val role = entityDao.get(classOf[Role], roleId)
    val user = userService.get(Securities.user).head
    put("manager", user)
    val isPlatformRoot = userService.isRoot(user)
    val mngRoles = new collection.mutable.ListBuffer[Role]
    val roleQuery = OqlBuilder.from(classOf[Role], "r").orderBy("r.indexno")
    roleQuery.where("r.domain=:domain", domainService.getDomain)
    val roles = entityDao.search(roleQuery)
    val myMngRoles = user.roles filter (m => m.manager) map (m => m.role)
    for (r <- roles) {
      if (myMngRoles.contains(r) || isPlatformRoot) mngRoles += r
    }
    put("mngRoles", mngRoles)
    val apps = filterAppsByRoleEnvs(appService.getWebapps, role)
    AppHelper.putApps(apps, "app.id", entityDao)

    val app: App = ActionContext.current.attribute("current_app")
    val mngMenus = new collection.mutable.ListBuffer[Menu]
    if (null != app) {
      var mngResources: collection.Seq[Object] = null
      if (isPlatformRoot) {
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

      val allowAllEnv = app.envs.isEmpty
      val appEnvs = resolveAppEnvs(app)
      put("allowAllEnv", allowAllEnv)
      put("appEnvs", appEnvs)
      val (allEnvMode, selectedEnvIds) = resolveEnvSelection(allowAllEnv, appEnvs, funcPermissionService.getRoleAppEnvs(app, role))
      put("allEnvMode", allEnvMode)
      put("selectedEnvIds", selectedEnvIds)
      put("selectedEnvIdStrs", selectedEnvIds.map(_.toString).toSet)

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
      put("allowAllEnv", true)
      put("appEnvs", Seq.empty)
      put("allEnvMode", true)
      put("selectedEnvIds", Set.empty)
      put("selectedEnvIdStrs", Set.empty)
    }
    put("mngMenus", mngMenus)
    put("role", role)
    forward()
  }

  /**
   * 角色与应用均配置了业务场景且交集为空时，不展示该应用。
   * 任一方场景为空则仍展示（空表示不限制）。
   */
  private def filterAppsByRoleEnvs(apps: Seq[App], role: Role): Seq[App] = {
    val roleEnvIds = role.envs.map(_.id)
    if (roleEnvIds.isEmpty) {
      apps
    } else {
      apps.filter { app =>
        val appEnvIds = app.envs.map(_.id)
        appEnvIds.isEmpty || appEnvIds.exists(roleEnvIds.contains)
      }
    }
  }

  /**
   * 可选场景列表：应用配置了 envs 则仅这些；否则为域下全部（供「指定场景」使用）。
   */
  private def resolveAppEnvs(app: App): Seq[Env] = {
    if (app.envs.isEmpty) {
      entityDao.search(OqlBuilder.from(classOf[Env], "env")
        .where("env.domain=:domain", domainService.getDomain)
        .orderBy("env.code"))
    } else {
      app.envs.toSeq.sortBy(_.code)
    }
  }

  /**
   * 回显场景选择：读 RoleAppEnv；无记录表示全部场景。
   */
  private def resolveEnvSelection(allowAllEnv: Boolean, appEnvs: Seq[Env], roleAppEnvs: Seq[RoleAppEnv]): (Boolean, Set[Long]) = {
    val allowedIds = appEnvs.map(_.id).toSet
    val selected = roleAppEnvs.map(_.env.id).toSet.intersect(allowedIds)
    if (allowAllEnv) {
      if (roleAppEnvs.isEmpty) (true, Set.empty)
      else (false, selected)
    } else {
      val display = if (selected.nonEmpty) selected else allowedIds
      (false, display)
    }
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
    if (userService.isRoot(manager)) {
      mngMenus = menuService.getMenus(app).toSet
    } else {
      mngMenus = menuService.getMenus(app, manager).toSet
    }
    for (m <- mngMenus) {
      mngResources ++= m.resources
    }

    newResources.dropWhile(p => !mngResources.contains(p))

    val where = to(this, "edit")
    where.param("role.id", role.id).param("app.id", app.id)
    val displayFreezen = get("displayFreezen")
    if (null != displayFreezen) where.param("displayFreezen", displayFreezen)

    resolveGlobalEnvIds(app) match {
      case Left(msg) =>
        return redirect(where, msg)
      case Right(envIds) =>
        funcPermissionService.authorize(app, role, newResources, envIds)
    }

    databus.publishUpdate(classOf[FuncPermission], Map("resource.app.name" -> app.name))
    // authority rest service need time to clean cache.
    // notify app or refresh itself
    Timers.setTimeout(5, () => {
      if app.name == EmsApp.name then authorizer.refresh()
      else publicChannel.publish(DataEvent.update(classOf[Authority], Map("app.name" -> app.getName)))
    })
    redirect(where, "info.save.success")
  }

  /**
   * 解析并校验角色×应用的场景范围。
   * 应用 envs 为空才允许全部场景；否则必须在 app.envs 范围内至少选一个。
   */
  private def resolveGlobalEnvIds(app: App): Either[String, Seq[Long]] = {
    val allowedIds = app.envs.map(_.id).toSet
    if (allowedIds.isEmpty) {
      val envScope = get("envScope", "all")
      if (envScope == "specific") {
        val selected = getLongIds("env").toSeq.distinct
        if (selected.isEmpty) Left("指定场景时请至少选择一个业务场景")
        else Right(selected)
      } else {
        Right(Seq.empty)
      }
    } else {
      val selected = getLongIds("env").toSeq.distinct.filter(allowedIds.contains)
      if (selected.isEmpty) Left("请至少选择一个业务场景")
      else Right(selected)
    }
  }

}
