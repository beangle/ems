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

package org.beangle.ems.ws.log

import org.beangle.data.dao.EntityDao
import org.beangle.ems.app.log.BusinessLogProto
import org.beangle.ems.app.log.BusinessLogProto.BusinessLogEvent
import org.beangle.ems.core.config.model.App
import org.beangle.ems.core.config.service.AppService
import org.beangle.ems.core.log.model.{BusinessLog, Level}
import org.beangle.web.action.annotation.{action, mapping, param}
import org.beangle.web.action.support.{ActionSupport, EntitySupport, ServletSupport}
import org.beangle.web.action.view.{Status, View}

import java.io.InputStream
import java.time.Instant

@action("")
class IndexWS extends ActionSupport with EntitySupport[BusinessLog] with ServletSupport {

  var appService: AppService = _

  var logDao: EntityDao = _

  @mapping("/info/{app}")
  def info(@param("app") appName: String): View = {
    appService.getApp(appName) match {
      case Some(app) =>
        logDao.saveOrUpdate(convert(app, request.getInputStream, Level.Info))
        Status.Ok
      case None => response.getWriter.print("bad appName"); Status.BadRequest
    }
  }

  @mapping("/warn/{app}")
  def warn(@param("app") appName: String): View = {
    appService.getApp(appName) match {
      case Some(app) =>
        logDao.saveOrUpdate(convert(app, request.getInputStream, Level.Warning))
        Status.Ok
      case None => response.getWriter.print("bad appName"); Status.BadRequest
    }
  }

  @mapping("/error/{app}")
  def error(@param("app") appName: String): View = {
    appService.getApp(appName) match {
      case Some(app) =>
        logDao.saveOrUpdate(convert(app, request.getInputStream, Level.Error))
        Status.Ok
      case None => response.getWriter.print("bad appName"); Status.BadRequest
    }
  }

  private def convert(app: App, is: InputStream, levelId: Int): BusinessLog = {
    val e = BusinessLogProto.BusinessLogEvent.parseFrom(is)
    val l = new BusinessLog()
    l.app = app
    l.operator = e.getOperator
    l.operateAt = Instant.ofEpochMilli(e.getOperateAt)
    l.summary = e.getSummary
    l.details = e.getDetails
    l.resources = e.getResources
    l.ip = e.getIp
    l.agent = e.getAgent
    l.entry = e.getEntry
    l.level = new Level(levelId)
    l
  }
}
