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

package org.beangle.ems.core.security.service

import org.beangle.commons.collection.Collections
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.config.model.App
import org.beangle.ems.core.config.service.AppService
import org.beangle.ems.core.security.model.{FuncPermission, FuncResource}
import org.beangle.ems.core.user.model.Root
import org.beangle.security.authz.{AbstractRoleBasedAuthorizer, Authority, AuthorityDomain}

/**
 * 从数据库中获取角色和权限
 */
class DefaultEmsAuthorizer extends AbstractRoleBasedAuthorizer {

  var appService: AppService = _
  var entityDao: EntityDao = _

  override def fetchDomain(): AuthorityDomain = {
    val app = appService.getApp(EmsApp.name).get
    AuthorityDomain(getRoots(app), getAuthorities(app))
  }

  private def getRoots(app: App): Iterable[String] = {
    val query = OqlBuilder.from[String](classOf[Root].getName, "r")
    query.where("r.app = :app", app).select("r.user.code").cacheable()
    entityDao.search(query)
  }

  private def getAuthorities(app: App): Seq[Authority] = {
    val query = OqlBuilder.from(classOf[FuncResource], "fr").where("fr.app=:app", app).cacheable()

    val resources = entityDao.search(query)
    val premQuery = OqlBuilder.from[Array[Object]](classOf[FuncPermission].getName, "fp")
      .where("fp.resource.app = :app", app)
      .select("fp.resource.id,fp.role.id")
      .cacheable()

    val permissions = Collections.newMap[Number, collection.mutable.Set[String]]
    entityDao.search(premQuery) foreach { p =>
      val roles = permissions.getOrElseUpdate(p(0).asInstanceOf[Number], new collection.mutable.HashSet[String])
      roles += p(1).toString
    }

    resources.map(r => new Authority(r.name, r.scope, permissions.getOrElse(r.id, Set.empty).toSet))
  }

  override def init(): Unit = {
    refresh()
  }
}
