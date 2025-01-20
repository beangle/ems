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
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.data.json.JsonAPI
import org.beangle.data.json.JsonAPI.Context
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.ems.core.oa.model.{Doc, Notice}
import org.beangle.webmvc.annotation.{mapping, param, response}
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.support.{ActionSupport, ServletSupport}
import org.beangle.webmvc.view.{Status, Stream, View}

import java.io.File
import java.time.LocalDate

class DocWS(entityDao: EntityDao) extends ActionSupport, ServletSupport {

  var domainService: DomainService = _

  var appService: AppService = _

  @mapping(value = "{app}/{category}")
  @response
  def list(@param("app") app: String, @param("category") category: String): AnyRef = {
    val query = buildQuery(category)
    app match {
      case "all" => query.where("doc.app.domain=:domain", domainService.getDomain)
      case _ =>
        appService.getApp(app) match {
          case Some(pp) => query.where("doc.app=:app", pp)
          case None => query.where("1=0")
        }
    }
    convert(entityDao.search(query))
  }

  private def buildQuery(category: String): OqlBuilder[Doc] = {
    val query = OqlBuilder.from(classOf[Doc], "doc")
    query.join("doc.categories", "uc")
    query.where("uc.id=:categoryId", category.toInt)
    query.where("doc.archived=false")
    query.orderBy("doc.updatedAt desc")
    for (pi <- getInt("pageIndex"); ps <- getInt("pageSize")) {
      query.limit(pi, ps)
    }
    query
  }

  @mapping(value = "{id}")
  def info(@param("id") id: String): View = {
    val doc = entityDao.get(classOf[Doc], id.toLong)
    EmsApp.getBlobRepository().path(doc.filePath) match {
      case Some(p) =>
        if p.startsWith("http") then
          response.sendRedirect(p)
          null
        else Stream(new File(p), doc.name)
      case None => Status.NotFound
    }
  }

  private def convert(docs: Iterable[Doc]): JsonAPI.Json = {
    given context: Context = JsonAPI.context(ActionContext.current.params)

    context.filters.include(classOf[Doc], "id", "name", "updatedAt", "url")
    val resources = docs.map { g => JsonAPI.create(g, "") }
    JsonAPI.newJson(resources)
  }
}
