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

package org.beangle.ems.ws.security.data

import org.beangle.commons.collection.Properties
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.core.config.service.AppService
import org.beangle.ems.core.security.model.DataPermission
import org.beangle.ems.core.user.service.UserService
import org.beangle.webmvc.annotation.{mapping, param, response}
import org.beangle.webmvc.support.ActionSupport

/** 查询app对应的用户授权信息
 *
 * @author chaostone
 */
class PermissionWS(entityDao: EntityDao) extends ActionSupport {

  var appService: AppService = _
  var userService: UserService = _

  @response
  @mapping("user/{userCode}")
  def index(@param("app") appName: String, @param("userCode") userCode: String, @param("data") dataName: String): Any = {
    val users = userService.get(userCode)
    val apps = appService.getApp(appName)
    if (users.isEmpty || apps.isEmpty) {
      List.empty
    } else {
      val u = users.head
      val app = apps.head

      val roleSet = u.roles.filter(r => r.member).map(r => r.role).toSet
      val premQuery = OqlBuilder.from(classOf[DataPermission], "dp")
      premQuery.where("dp.domain=:domain and dp.resource.name=:dataName", app.domain, dataName)
        .cacheable(true)
      val permissions = entityDao.search(premQuery)
      val mostFavorates = permissions find (p => p.app.isDefined && p.role.isDefined && roleSet.contains(p.role.get))
      val p = mostFavorates match {
        case Some(p) => p
        case None =>
          permissions find (x => x.app.isDefined && x.role.isEmpty) match {
            case Some(p) => p
            case None =>
              permissions find (x => x.app.isEmpty && x.role.isDefined) match {
                case Some(p) => p
                case None =>
                  val pp = permissions find (x => x.app.isEmpty && x.role.isEmpty)
                  pp.orNull
              }
          }
      }
      val props = new Properties()
      if (p != null) props.put("filters", p.filters)
      props
    }
  }
}
