/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2020, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.ems.ws.security.func

import org.beangle.commons.collection.{Collections, Properties}
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.security.authz.Scopes
import org.beangle.webmvc.api.action.{ActionSupport, EntitySupport}
import org.beangle.webmvc.api.annotation.{mapping, param, response}
import org.beangle.ems.core.config.service.AppService
import org.beangle.ems.core.security.model.{FuncPermission, FuncResource}

/**
 * 系统功能资源web服务
 */
class ResourceWS(entityDao: EntityDao) extends ActionSupport with EntitySupport[FuncResource] {

  var appService: AppService = _

  @response
  def index(@param("app") appName: String): Seq[Any] = {
    val appResult = appService.getApp(appName)
    if(appResult.isEmpty){
      return List.empty
    }
    val app = appResult.head
    val query = OqlBuilder.from(classOf[FuncResource], "fr")
      .where("fr.app=:app", app)

    get("scope") foreach { s =>
      query.where("fr.scope = :scope", Scopes.withName(s))
    }
    val resources = entityDao.search(query)
    val premQuery = OqlBuilder.from[Array[Object]](classOf[FuncPermission].getName, "fp")
      .where("fp.resource.app = :app", app)
      .select("fp.resource.id,fp.role.id")
      .cacheable()

    val permissions = Collections.newMap[Number, collection.mutable.Set[Number]]
    entityDao.search(premQuery) foreach { p =>
      val roles = permissions.getOrElseUpdate(p(0).asInstanceOf[Number], new collection.mutable.HashSet[Number])
      roles += p(1).asInstanceOf[Number]
    }

    resources map { r =>
      val p = new Properties(r, "id", "name", "title", "scope")
      p.put("roles", permissions.getOrElse(r.id, Set.empty).toArray)
      p
    }
  }

  @response
  def info(@param("app") appName: String, @param("name") name: String): Properties = {
    val app = appService.getApp(appName).head
    val query = OqlBuilder.from(classOf[FuncResource], "fr").where("fr.app=:app", app)
    query.where("fr.name=:name", name).cacheable()
    val resources = entityDao.search(query)
    if (resources.nonEmpty) {
      val roleQuery = OqlBuilder.from[Integer](classOf[FuncPermission].getName, "fp")
        .where("fp.resource.app = :app", app)
        .where("fp.resource.name =:resourceName", name)
        .select("fp.role.id")
        .cacheable()
      val p = new Properties(resources.head, "id", "name", "title", "scope")
      p.put("roles", entityDao.search(roleQuery).toArray)
      p
    } else {
      new Properties
    }
  }

  @response
  @mapping("public")
  def pub(@param("app") appName: String): Seq[Any] = {
    val app = appService.getApp(appName).head
    val query = OqlBuilder.from(classOf[FuncResource], "fr")
      .where("fr.app=:app", app)
      .where("fr.scope=:scope", Scopes.Public)
      .cacheable()
    entityDao.search(query)
  }

}
