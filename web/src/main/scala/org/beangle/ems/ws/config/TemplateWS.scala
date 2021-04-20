package org.beangle.ems.ws.config

import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.lang.Charsets
import org.beangle.commons.lang.Strings.substringAfterLast
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.core.config.model.{DocTemplate, TextTemplate}
import org.beangle.ems.core.config.service.AppService
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.annotation.{mapping, param}
import org.beangle.webmvc.api.view.{Status, Stream, View}

import java.io.ByteArrayInputStream

class TemplateWS extends ActionSupport {

  var appService: AppService = _

  var entityDao: EntityDao = _

  @mapping(value = "{app}/{path*}")
  def index(@param("app") app: String, @param("path") path: String): View = {
    val apps = appService.getApp(app)
    if (apps.isEmpty) return Status.NotFound
    val exist = apps.head
    val contentType = MediaTypes.get(substringAfterLast(path, "."), MediaTypes.ApplicationOctetStream).toString

    val fileName = substringAfterLast(path, "/")
    if (contentType.startsWith("text/")) {
      val query = OqlBuilder.from(classOf[TextTemplate], "tt")
      query.where("tt.app=:app and tt.path=:path", exist, path)
      val templates = entityDao.search(query)
      templates.headOption match {
        case Some(tt) =>
          val is = new ByteArrayInputStream(tt.contents.getBytes(Charsets.UTF_8))
          Stream(is, contentType, fileName)
        case None => Status.NotFound
      }
    } else {
      val query = OqlBuilder.from(classOf[DocTemplate], "tt")
      query.where("tt.app=:app and tt.path=:path", exist, path)
      val templates = entityDao.search(query)
      templates.headOption match {
        case Some(tt) =>
          val is = new ByteArrayInputStream(tt.contents)
          Stream(is, contentType, fileName)
        case None => Status.NotFound
      }
    }

  }
}
