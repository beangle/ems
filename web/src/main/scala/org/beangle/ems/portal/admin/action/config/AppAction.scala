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
package org.beangle.ems.portal.admin.action.config

import org.beangle.data.dao.OqlBuilder
import org.beangle.webmvc.api.annotation.ignore
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.entity.action.RestfulAction
import org.beangle.ems.core.config.model._
import org.beangle.ems.core.config.service.{AppService, DbService, DomainService}

class AppAction(dbService: DbService) extends RestfulAction[App] {

  override def simpleEntityName = "app"

  var domainService: DomainService = _
  var appService: AppService = _

  def datasource(): View = {
    put("dataSources", dbService.getAll())
    forward()
  }

  protected override def indexSetting(): Unit = {
    put("groups", appService.getGroups())
    put("appTypes", entityDao.getAll(classOf[AppType]))
  }

  protected override def editSetting(entity: App): Unit = {
    if(!entity.persisted){
      entity.enabled=true
    }
    put("groups", appService.getGroups())
    put("appTypes", entityDao.getAll(classOf[AppType]))
    put("credentials", entityDao.getAll(classOf[Credential]))
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
      redirect("search", "info.save.success")
    } catch {
      case e: Exception =>
        logger.info("saveAndForwad failure", e)
        redirect("search", "info.save.failure")
    }
  }
}
