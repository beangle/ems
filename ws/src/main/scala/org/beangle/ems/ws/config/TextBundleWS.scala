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

import org.beangle.commons.conversion.string.LocaleConverter
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.core.config.model.TextBundle
import org.beangle.ems.core.config.service.AppService
import org.beangle.webmvc.annotation.{mapping, param}
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.view.{Status, View}

/** 查询应用的国际化资源
 */
class TextBundleWS extends ActionSupport {

  var appService: AppService = _

  var entityDao: EntityDao = _

  @mapping(value = "{app}/ls")
  def list(@param("app") appName: String): View = {
    appService.getApp(appName) match
      case None => Status.NotFound
      case Some(app) =>
        val query = OqlBuilder.from(classOf[TextBundle], "b")
        query.where("b.app=:app", app)
        val bundles = entityDao.search(query)
        val contents = bundles.map(b => b.name.replace(".", "/") + "." + b.locale.toString).mkString("\n")
        val response = ActionContext.current.response
        response.getWriter.write(contents)
        null
  }

  @mapping(value = "{app}/{path*}")
  def texts(@param("app") appName: String, @param("path") path: String): View = {
    appService.getApp(appName) match
      case None => Status.NotFound
      case Some(app) =>
        val locale = Strings.substringAfterLast(ActionContext.current.request.getRequestURI, ".")
        val bundleName = path.replace("/", ".")
        val query = OqlBuilder.from(classOf[TextBundle], "b")
        query.where("b.app=:app", app)
        query.where("b.name=:name", bundleName)
        query.where("b.locale=:locale", LocaleConverter(locale))
        entityDao.search(query).headOption match
          case None => Status.NotFound
          case Some(b) =>
            val response = ActionContext.current.response
            response.setCharacterEncoding("utf-8")
            response.getWriter.write(b.texts)
            null
  }
}
