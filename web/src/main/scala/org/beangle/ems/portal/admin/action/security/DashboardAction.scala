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

import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.web.action.support.ActionSupport
import org.beangle.web.action.view.View
import org.beangle.ems.core.config.service.AppService
import org.beangle.ems.core.security.model.{DataPermission, FuncResource, Menu}
import org.beangle.ems.core.user.model.{Dimension, RoleMember}

class DashboardAction extends ActionSupport {

  var entityDao: EntityDao = _

  var appService: AppService = _

  def stat(): View = {
    populateUserStat()
    // state menus
    val apps = appService.getWebapps
    val menuStats = new collection.mutable.HashMap[Integer, Seq[_]]
    for (app <- apps) {
      val menuQuery = OqlBuilder.from(classOf[Menu], "menu")
      menuQuery.where("menu.app=:app", app).select("menu.enabled,count(*)").groupBy("enabled")
      menuStats.put(app.id, entityDao.search(menuQuery))
    }
    put("apps", apps)
    put("menuStats", menuStats)

    // stat resource
    val resourceQuery = OqlBuilder.from(classOf[FuncResource], "resource")
    resourceQuery.select("resource.enabled,count(*)").groupBy("enabled")
    put("resourceStat", entityDao.search(resourceQuery))

    // stat dataPermission and restriction
    put(
      "dataPermissionCnt",
      entityDao.search(OqlBuilder.from(classOf[DataPermission], "p").select("count(*)")))
    put("fieldCnt", entityDao.search(OqlBuilder.from(classOf[Dimension], "param").select("count(*)")))
    forward()
  }

  private def populateUserStat(): Unit = {
    val userQuery = OqlBuilder.from(classOf[RoleMember], "gm")
    userQuery.select("gm.role.indexno,gm.role.name,gm.user.enabled,count(*)").groupBy(
      "gm.role.indexno,gm.role.name,gm.user.enabled")
    val datas = entityDao.search(userQuery)
    val rs = new collection.mutable.HashMap[String, collection.mutable.Map[Object, Object]]
    for (data <- datas) {
      val roleStat = data.asInstanceOf[Array[Object]]
      val key = s"${roleStat(0)} ${roleStat(1)}"
      val statusMap = rs.getOrElseUpdate(key, new collection.mutable.HashMap[Object, Object])
      statusMap.put(roleStat(2), roleStat(3))
    }
    put("userStat", rs)
  }

}
