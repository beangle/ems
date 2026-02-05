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

package org.beangle.ems.portal.action.admin.user

import org.beangle.data.dao.OqlBuilder
import org.beangle.data.model.util.Hierarchicals
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.config.model.App
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.security.service.ProfileService
import org.beangle.ems.core.user.model.{Role, User}
import org.beangle.ems.core.user.service.impl.CsvDataResolver
import org.beangle.ems.core.user.service.{DimensionService, RoleService, UserService}
import org.beangle.ems.portal.helper.ProfileHelper
import org.beangle.security.Securities
import org.beangle.security.context.SecurityContext
import org.beangle.webmvc.support.action.{ExportSupport, RestfulAction}
import org.beangle.webmvc.view.View

import java.time.Instant

/**
 * 角色信息维护响应类
 *
 * @author chaostone 2005-9-29
 */
class RoleAction(val roleService: RoleService, val userService: UserService) extends RestfulAction[Role], ExportSupport[Role] {

  private val dataResolver = CsvDataResolver
  var domainService: DomainService = _
  var dimensionService: DimensionService = _
  var profileService: ProfileService = _

  override protected def indexSetting(): Unit = {
    put("isRoot", SecurityContext.get.root)
  }

  /**
   * 对组可管理意为<br>
   * 1 建立下级组
   * 2 移动下级组顺序
   * 不能改变组的1）权限和2）直接成员，3）删除组，4）重命名，这些将看作组同部分一起看待的。
   * 只要拥有上级组的管理权限，才能变更这些，这些称之为写权限。
   * 成员关系可以等价于读权限
   * 授权关系可以等价于读权限传播
   * 拥有某组的管理权限，不意味拥有下级组的管理权限。新建组情况自动授予该组的其他管理者管理权限。
   */
  override def editSetting(role: Role): Unit = {
    put("role", role)
    val query = OqlBuilder.from(classOf[Role], "role")
    val me = userService.get(Securities.user).head
    if (!userService.isRoot(me, EmsApp.name)) {
      query.join("role.members", "gm")
      query.where("gm.user=:me and gm.manager=true", me)
    }
    query.where("role.domain=:domain", domainService.getDomain)
    val parents = new collection.mutable.ListBuffer[Role]
    parents ++= entityDao.search(query)
    parents --= Hierarchicals.getFamily(role)
    put("parents", parents)

    if (!role.persisted) {
      role.enabled = true
    }
    forward()
  }

  protected override def getQueryBuilder: OqlBuilder[Role] = {
    val entityQuery = OqlBuilder.from(classOf[Role], "role")
    populateConditions(entityQuery)
    entityQuery.where("role.domain=:domain", domainService.getDomain)
    val orderBy = get("orderBy", "role.indexno")
    entityQuery.limit(getPageLimit).orderBy(orderBy)
  }

  protected override def saveAndRedirect(entity: Role): View = {
    entity.domain = domainService.getDomain
    val me = entityDao.findBy(classOf[User], "code", List(Securities.user)).head
    val role = entity
    if (entity.persisted) {
      if (!roleService.isManagedBy(me, role)) {
        return redirect("search", "不能修改该组,你没有" + role.parent.map(p => p.name).orNull + "的管理权限")
      }
    }
    if entityDao.duplicate(classOf[Role], role.id, Map("name" -> role.getName, "domain" -> entity.domain)) then
      return redirect("edit", "error.notUnique")

    if (!role.persisted) {
      role.indexno = "tmp"
      roleService.create(me, role)
    } else {
      role.updatedAt = Instant.now
      entityDao.saveOrUpdate(role)
    }
    var parent: Role = null
    val indexno = getInt("indexno", 1)
    getInt("parent.id") match {
      case Some(parentId) => parent = entityDao.get(classOf[Role], parentId)
      case None =>
    }
    roleService.move(role, parent, indexno)
    if (!role.enabled) {
      val family = Hierarchicals.getFamily(role.asInstanceOf[Role])
      for (one <- family) one.asInstanceOf[Role].enabled = false
      entityDao.saveOrUpdate(family)
    }
    redirect("search", "info.save.success")
  }

  def profile(): View = {
    val role = entityDao.get(classOf[Role], getIntId("role"))
    val helper = new ProfileHelper(entityDao, profileService, dimensionService)
    helper.populateInfo(List(role))
    put("role", role)
    forward()
  }

  def editProfile(): View = {
    val role = entityDao.get(classOf[Role], getIntId("role"))
    val helper = new ProfileHelper(entityDao, profileService, dimensionService)
    helper.fillEditInfo(role, isAdmin = true)
    put("role", role)
    forward()
  }

  def removeProfile(): View = {
    val role = entityDao.get(classOf[Role], getIntId("role"))
    role.properties.clear()
    entityDao.saveOrUpdate(role)
    redirect("profile", "info.save.success")
  }

  def saveProfile(): View = {
    val me = entityDao.findBy(classOf[User], "code", List(Securities.user)).head
    val helper = new ProfileHelper(entityDao, profileService, dimensionService)
    helper.dataResolver = dataResolver
    val role = entityDao.get(classOf[Role], getIntId("role"))
    val app = entityDao.get(classOf[App], getIntId("app"))
    helper.populateSaveInfo(role, userService.isRoot(me, app.name))
    entityDao.saveOrUpdate(role)
    redirect("profile", "info.save.success")
  }

  /**
   * 删除一个或多个角色
   */
  override def remove(): View = {
    val me = userService.get(Securities.user).head
    roleService.remove(me, entityDao.find(classOf[Role], getIntIds("role")))
    redirect("search", "info.remove.success")
  }

}
