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

package org.beangle.ems.ws.config

import org.beangle.commons.lang.Strings
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.config.model.File
import org.beangle.ems.core.config.service.AppService
import org.beangle.webmvc.annotation.{mapping, param}
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.view.{Status, View}

/** 应用配置文件
 */
class FileWS extends ActionSupport {

  var appService: AppService = _

  var entityDao: EntityDao = _

  @mapping(value = "{app}/{path*}", method = "head")
  def info(@param("app") app: String, @param("path") path: String): View = {
    val response = ActionContext.current.response
    getFile(app, path) match {
      case Some(template) =>
        response.addDateHeader("Last-Modified", template.updatedAt.toEpochMilli)
        response.setContentLength(template.fileSize)
        response.setStatus(200)
      case None =>
        response.setStatus(404)
    }
    null
  }

  private def getFile(app: String, path: String): Option[File] = {
    val ext = Strings.substringAfterLast(ActionContext.current.request.getRequestURI, ".")
    val name = path + "." + ext
    val apps = appService.getApp(app)
    if (apps.isEmpty) return None
    val exist = apps.head

    val query = OqlBuilder.from(classOf[File], "tt")
    query.where("tt.app=:app and tt.name=:name", exist, name)
    entityDao.search(query).headOption
  }

  @mapping(value = "{app}/{path*}")
  def index(@param("app") app: String, @param("path") path: String): View = {
    val response = ActionContext.current.response
    if (path == "ls") {
      val query = OqlBuilder.from(classOf[File], "tt")
      query.where("tt.app.name=:app", app).cacheable()
      val templates = entityDao.search(query)
      val contents = templates.map(_.name).mkString(",")
      response.getWriter.write(contents)
      null
    } else {
      getFile(app, path) match {
        case Some(template) =>
          val repo = EmsApp.getBlobRepository()
          redirect(to(repo.path(template.filePath)), "")
        case None => Status.NotFound
      }
    }
  }
}
