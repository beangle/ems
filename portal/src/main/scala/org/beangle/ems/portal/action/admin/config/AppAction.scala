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

package org.beangle.ems.portal.action.admin.config

import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.EmsLogger
import org.beangle.ems.core.config.model.*
import org.beangle.ems.core.config.service.DbService
import org.beangle.ems.core.security.model.FuncPermission
import org.beangle.ems.core.user.model.Role
import org.beangle.ems.portal.action.admin.DomainSupport
import org.beangle.she.webmvc.RestfulAction
import org.beangle.webmvc.annotation.ignore
import org.beangle.webmvc.view.View

class AppAction(dbService: DbService) extends RestfulAction[App], DomainSupport {

  override protected def simpleEntityName = "app"

  def datasource(): View = {
    put("dataSources", dbService.getAll())
    forward()
  }

  override def info(id: String): View = {
    val app = getModel(id.toInt)
    put("app", app)
    put("roles", findAuthorizedRoles(app))
    putEnvById()
    forward()
  }

  override def search(): View = {
    putEnvById()
    super.search()
  }

  protected override def indexSetting(): Unit = {
    put("groups", appService.getGroups)
  }

  private def putEnvById(): Unit = {
    val envs = entityDao.search(OqlBuilder.from(classOf[Env], "env")
      .where("env.domain=:domain", domainService.getDomain)
      .orderBy("env.code"))
    put("envs", envs)
    put("envById", envs.map(e => e.id.toString -> e).toMap)
  }

  protected override def editSetting(entity: App): Unit = {
    if (!entity.persisted) {
      entity.enabled = true
    } else {
      put("roles", findAuthorizedRoles(entity))
    }
    put("groups", appService.getGroups)
    put("credentials", appService.getCredentials)
    put("envs", entityDao.search(OqlBuilder.from(classOf[Env], "env")
      .where("env.domain=:domain", domainService.getDomain)
      .orderBy("env.code")))
    put("selectedEnvs", entityDao.find(classOf[Env], entity.envIdSet))
  }

  /** 查询拥有该应用功能资源授权的角色 */
  private def findAuthorizedRoles(app: App): Seq[Role] = {
    val roleQuery = OqlBuilder.from[Role](classOf[FuncPermission].getName + " fp")
      .where("fp.resource.app=:app", app)
      .select("distinct fp.role")
    entityDao.search(roleQuery).sortBy(_.indexno)
  }

  override protected def getQueryBuilder: OqlBuilder[App] = {
    val builder = super.getQueryBuilder
    builder.where("app.domain=:domain", domainService.getDomain)
    builder
  }

  @ignore
  override protected def saveAndRedirect(app: App): View = {
    app.domain = domainService.getDomain
    try {
      val sets = app.datasources
      val processed = new collection.mutable.HashSet[Integer]
      val removed = new collection.mutable.HashSet[DataSource]
      val ids = getAll("ds", classOf[Integer]).toSet
      sets foreach { ds =>
        if (ids.contains(ds.db.id)) {
          processed += ds.db.id
          populate(ds, "ds" + ds.db.id)
        } else {
          removed += ds
        }
      }
      sets --= removed
      for (id <- ids if !processed.contains(id)) {
        val set = populate(classOf[DataSource], "ds" + id)
        set.app = app
        sets += set
      }

      val envs = entityDao.find(classOf[Env], getLongIds("env"))
      app.setEnvIds(envs.map(_.id))

      saveOrUpdate(app)

      publishUpdate(classOf[DataSource], Map("app.name" -> app.name))
      publishUpdate(app)
      redirect("search", "info.save.success")
    } catch {
      case e: Exception =>
        EmsLogger.error("saveAndForwad failure", e)
        redirect("search", "info.save.failure")
    }
  }
}
