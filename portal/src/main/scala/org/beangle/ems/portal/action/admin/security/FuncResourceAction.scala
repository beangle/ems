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

import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.core.security.model.{FuncPermission, FuncResource, Menu}
import org.beangle.ems.core.security.service.FuncPermissionService
import org.beangle.ems.portal.action.admin.DomainSupport
import org.beangle.ems.portal.helper.AppHelper
import org.beangle.security.authz.Scope
import org.beangle.webmvc.annotation.ignore
import org.beangle.webmvc.view.View
import org.beangle.webmvc.support.action.{ExportSupport, RestfulAction}

/**
  * 系统模块管理响应类
  *
  * @author chaostone 2005-10-9
  */
class FuncResourceAction extends RestfulAction[FuncResource], ExportSupport[FuncResource], DomainSupport {

  var funcPermissionService: FuncPermissionService = _

  /**
    * 禁用或激活一个或多个模块
    */
  def activate(): View = {
    val resourceIds = getIntIds("resource")
    val enabled = getBoolean("enabled", defaultValue = false)
    funcPermissionService.activate(resourceIds, enabled)
    redirect("search", "info.save.success")
  }

  protected override def saveAndRedirect(resource: FuncResource): View = {
    if (null != resource) {
      val builder = OqlBuilder.from[Int](classOf[FuncResource].getName, "fr").where("fr.name=:name and fr.app = :app", resource.name, resource.app).select("fr.id")
      val ids = entityDao.search(builder)
      if (!resource.persisted && ids.nonEmpty || resource.persisted && ids.nonEmpty && !ids.contains(resource.id)) {
        return redirect("edit", "error.notUnique")
      }
    }
    entityDao.saveOrUpdate(resource)
    publishUpdate(resource)
    redirect("search", "info.save.success")
  }

  override def search(): View = {
    AppHelper.remember("resource.app.id")
    super.search()
    forward()
  }

  override def getQueryBuilder: OqlBuilder[FuncResource] = {
    val builder = super.getQueryBuilder
    builder.where("resource.app.domain=:domain", domainService.getDomain)
    builder
  }

  override def info(id: String): View = {
    val entity: FuncResource = getModel(id.toInt)
    val query = OqlBuilder.from(classOf[Menu], "menu")
    query.join("menu.resources", "r").where("r.id=:resourceId", entity.id)
      .orderBy("menu.app.id,menu.indexno")

    val roleQuery = OqlBuilder.from(classOf[FuncPermission], "auth")
    roleQuery.where("auth.resource=:resource", entity).select("auth.role")
    put(simpleEntityName, entity)
    put("roles", entityDao.search(roleQuery))
    put("menus", entityDao.search(query))
    forward()
  }

  protected override def editSetting(resource: FuncResource): Unit = {
    put("apps", appService.getApps)
    if (!resource.persisted) {
      resource.scope = Scope.Private
      resource.enabled = true
    }
  }

  protected override def simpleEntityName: String = {
    "resource"
  }

  protected override def indexSetting(): Unit = {
    AppHelper.putApps(appService.getApps, "resource.app.id", entityDao)
  }

  @ignore
  protected override def removeAndRedirect(entities: Seq[FuncResource]): View = {
    try {
      funcPermissionService.removeResources(entities)
      redirect("search", "info.remove.success")
    } catch {
      case e: Exception =>
        logger.info("removeAndForwad failure", e)
        redirect("search", "info.delete.failure")
    }
  }
}
