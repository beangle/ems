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
package org.beangle.ems.ws.config

import org.beangle.commons.activation.MediaTypes
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.core.config.model.Reconfig
import org.beangle.ems.core.config.service.AppService
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.annotation.{mapping, param}
import org.beangle.webmvc.api.view.{Stream, View}

import java.io.ByteArrayInputStream

class ReconfigWS extends ActionSupport {

  var appService: AppService = _

  var entityDao: EntityDao = _

  @mapping(value = "{app}")
  def index(@param("app") app: String): View = {
    val apps = appService.getApp(app)
    if (apps.isEmpty) return null
    val exist = apps.head

    val query = OqlBuilder.from(classOf[Reconfig], "tt")
    query.where("tt.app=:app", exist).cacheable()
    val contents = entityDao.search(query).map(_.contents).headOption.getOrElse("<?xml version=\"1.0\"?>")
    val is = new ByteArrayInputStream(contents.getBytes())
    Stream(is, MediaTypes.ApplicationXml.toString, exist.name + ".xml")
  }

}
