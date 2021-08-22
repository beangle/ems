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

package org.beangle.ems.ws.bulletin

import java.time.LocalDate

import org.beangle.commons.collection.Properties
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.web.action.support.{ActionSupport, EntitySupport}
import org.beangle.web.action.annotation.{mapping, param, response}
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.bulletin.model.{Notice, NoticeStatus}
import org.beangle.ems.core.config.service.{AppService, DomainService}

class NoticeWS(entityDao: EntityDao) extends ActionSupport with EntitySupport[Notice] {

  var domainService: DomainService = _

  var appService: AppService = _

  @mapping(value = "{app}/{category}")
  @response
  def app(@param("app") app: String, @param("category") category: String): AnyRef = {
    val query = buildQuery(category)
    query.where("notice.app=:app", appService.getApp(app))
    val notices = entityDao.search(query)
    notices.map(convertTitle)
  }

  @mapping(value = "{category}")
  @response
  def domain(@param("category") category: String): AnyRef = {
    val query = buildQuery(category)
    query.where("notice.app.domain=:domain", domainService.getDomain)
    val notices = entityDao.search(query)
    notices.map(convertTitle)
  }

  private def buildQuery(category: String): OqlBuilder[Notice] = {
    val query = OqlBuilder.from(classOf[Notice], "notice")
    query.join("notice.userCategories", "uc")
    query.where("uc.id=:categoryId", category.toInt)
    query.where(":now between notice.beginOn and notice.endOn", LocalDate.now)
    query.where("notice.status=:status", NoticeStatus.Passed)
    query.orderBy("notice.sticky desc,notice.publishedAt desc")
    for (pi <- getInt("pageIndex"); ps <- getInt("pageSize")) {
      query.limit(pi, ps)
    }
    query
  }

  @mapping(value = "{id}")
  def info(@param("id") id: String): AnyRef = {
    val query = OqlBuilder.from(classOf[Notice], "notice")
    query.where("notice.id=:id", id.toLong)
    query.where("notice.status=:status", NoticeStatus.Passed)
    val notices = entityDao.search(query)
    if (notices.nonEmpty) convert(notices.head) else null
  }

  private def convertTitle(notice: Notice): Properties = {
    new Properties(notice, "id", "title", "title", "createdAt", "popup", "sticky")
  }

  private def convert(notice: Notice): Properties = {
    val not = new Properties(notice, "id", "title", "title", "createdAt", "popup", "sticky", "contents")
    val docs = notice.docs map { doc =>
      val d = new Properties(doc, "id", "name")
      EmsApp.getBlobRepository(true).path(doc.filePath) foreach { url =>
        d.put("url", url)
      }
      d
    }
    not.put("docs", docs)
    not
  }
}
