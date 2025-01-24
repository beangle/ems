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

package org.beangle.ems.ws.oa

import org.beangle.commons.collection.Properties
import org.beangle.commons.json.JsonObject
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.data.json.JsonAPI
import org.beangle.data.json.JsonAPI.Context
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.ems.core.oa.model.{Doc, Notice, NoticeStatus}
import org.beangle.webmvc.annotation.{mapping, param, response}
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.support.{ActionSupport, JsonAPISupport}

import java.time.LocalDate

class NoticeWS(entityDao: EntityDao) extends ActionSupport, JsonAPISupport {

  var domainService: DomainService = _

  var appService: AppService = _

  @mapping(value = "{app}/{category}")
  @response
  def list(@param("app") app: String, @param("category") category: String): JsonObject = {
    val query = buildQuery(category)
    app match {
      case "all" => query.where("notice.app.domain=:domain", domainService.getDomain)
      case _ =>
        appService.getApp(app) match {
          case Some(pp) => query.where("notice.app=:app", pp)
          case None => query.where("1=0")
        }
    }
    convert(entityDao.search(query))
  }

  private def buildQuery(category: String): OqlBuilder[Notice] = {
    val query = OqlBuilder.from(classOf[Notice], "notice")
    query.join("notice.categories", "uc")
    query.where("uc.id=:categoryId", category.toInt)
    query.where(":now between notice.beginOn and notice.endOn", LocalDate.now)
    query.where("notice.status=:status", NoticeStatus.Passed)
    query.orderBy("notice.sticky desc,notice.publishedAt desc")
    query.cacheable(true)
    for (pi <- getInt("pageIndex"); ps <- getInt("pageSize")) {
      query.limit(pi, ps)
    }
    query
  }

  @mapping(value = "{id}")
  @response
  def info(@param("id") id: String): AnyRef = {
    val query = OqlBuilder.from(classOf[Notice], "notice")
    query.where("notice.id=:id", id.toLong)
    query.where("notice.status=:status", NoticeStatus.Passed)
    val notices = entityDao.search(query)
    if (notices.nonEmpty) convert(notices.head) else null
  }

  private def convert(notices: Iterable[Notice]): JsonObject = {
    given context: Context = JsonAPI.context(ActionContext.current.params)

    context.filters.include(classOf[Notice], "id", "title", "createdAt", "popup", "sticky", "docs")
    context.filters.include(classOf[Doc], "id", "name", "url")
    val resources = notices.map { g => JsonAPI.create(g, "").linkSelf(url("!info?id=" + g.id)) }
    JsonAPI.newJson(resources)
  }

  private def convert(notice: Notice): Properties = {
    val not = new Properties(notice, "id", "title", "createdAt", "popup", "sticky", "contents")
    val docs = notice.docs map { doc =>
      new Properties(doc, "id", "name", "url")
    }
    not.put("docs", docs)
    not
  }
}
