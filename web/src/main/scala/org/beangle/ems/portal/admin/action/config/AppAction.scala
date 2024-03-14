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

package org.beangle.ems.portal.admin.action.config

import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.core.config.model.*
import org.beangle.ems.core.config.service.DbService
import org.beangle.ems.portal.admin.action.DomainSupport
import org.beangle.web.action.annotation.ignore
import org.beangle.web.action.view.View
import org.beangle.webmvc.support.action.RestfulAction

class AppAction(dbService: DbService) extends RestfulAction[App], DomainSupport {

  override protected def simpleEntityName = "app"

  def datasource(): View = {
    put("dataSources", dbService.getAll())
    forward()
  }

  protected override def indexSetting(): Unit = {
    put("groups", appService.getGroups())
    put("appTypes", entityDao.getAll(classOf[AppType]))
  }

  protected override def editSetting(entity: App): Unit = {
    if (!entity.persisted) {
      entity.enabled = true
    }
    put("groups", appService.getGroups())
    put("appTypes", entityDao.getAll(classOf[AppType]))
    put("credentials", appService.getCredentials())
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

      saveOrUpdate(app)

      publishUpdate(classOf[DataSource], Map("app.name" -> app.name))
      publishUpdate(app)
      redirect("search", "info.save.success")
    } catch {
      case e: Exception =>
        logger.info("saveAndForwad failure", e)
        redirect("search", "info.save.failure")
    }
  }
}
