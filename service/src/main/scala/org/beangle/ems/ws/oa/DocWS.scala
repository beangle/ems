package org.beangle.ems.ws.oa

import org.beangle.commons.collection.Properties
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.data.jsonapi.JsonAPI
import org.beangle.data.jsonapi.JsonAPI.Context
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.ems.core.oa.model.{Doc, Notice}
import org.beangle.web.action.annotation.{mapping, param, response}
import org.beangle.web.action.context.ActionContext
import org.beangle.web.action.support.{ActionSupport, EntitySupport, ServletSupport}
import org.beangle.web.action.view.{Status, Stream, View}

import java.io.File
import java.time.LocalDate

class DocWS(entityDao: EntityDao) extends ActionSupport, EntitySupport[Doc], ServletSupport {

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
    query.join("doc.userCategories", "uc")
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

    context.filters.include(classOf[Doc], "id", "name", "updatedAt","url")
    val resources = docs.map { g => JsonAPI.create(g, "") }
    JsonAPI.newJson(resources)
  }
}
