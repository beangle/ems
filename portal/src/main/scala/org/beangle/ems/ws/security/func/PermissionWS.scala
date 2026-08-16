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

package org.beangle.ems.ws.security.func

import org.beangle.commons.collection.Properties
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.core.config.service.AppService
import org.beangle.ems.core.security.model.{FuncPermission, FuncResource}
import org.beangle.ems.core.security.service.FuncPermissionService
import org.beangle.ems.core.user.service.UserService
import org.beangle.webmvc.annotation.{action, mapping, param, response}
import org.beangle.webmvc.support.ActionSupport

/** 查询app中某个roleId的资源范围
 *
 * @author chaostone
 */
@action("/security/func/{app}/permissions")
class PermissionWS(entityDao: EntityDao) extends ActionSupport {

  var appService: AppService = _
  var userService: UserService = _
  var funcPermissionService: FuncPermissionService = _

  @response
  @mapping("role/{roleId}")
  def role(@param("app") appName: String, @param("roleId") roleId: Int): Seq[Properties] = {
    val app = appService.getApp(appName).head
    val roleQuery = OqlBuilder.from[FuncResource](classOf[FuncPermission].getName, "fp")
      .where("fp.resource.app = :app", app).where("fp.role.id = :roleId", roleId)
      .cacheable()
      .select("fp.resource")
    val resources = entityDao.search(roleQuery)
    resources.map { r => new Properties(r, "id", "name", "title", "scope") }
  }

  /** 查找一个app对应的某个用户的权限
   * 返回资源的集合
   *
   * @param appName
   * @param username
   * @return
   */
  @response
  @mapping("user/{user}")
  def user(@param("app") appName: String, @param("user") username: String): collection.Iterable[String] = {
    val user = userService.get(username)
    if (user.isEmpty) {
      return List.empty
    }
    val u = user.get
    val app = appService.getApp(appName)
    if (app.isEmpty) {
      return List.empty
    }
    funcPermissionService.getResources(app.get, u).map(_.name)
  }
}
