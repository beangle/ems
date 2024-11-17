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

import org.beangle.commons.bean.{Disposable, Initializing}
import org.beangle.data.dao.EntityDao
import org.beangle.ems.app.log.BusinessLogProto
import org.beangle.ems.core.config.service.AppService
import org.beangle.ems.core.log.model.BusinessLog
import org.beangle.webmvc.annotation.mapping
import org.beangle.webmvc.support.{ActionSupport, ServletSupport}
import org.beangle.webmvc.view.{Status, View}
import org.hibernate.SessionFactory

import java.io.InputStream
import java.time.Instant

class PushWS extends ActionSupport with ServletSupport with Initializing with Disposable {

  var appService: AppService = _

  var logDao: EntityDao = _

  var sf: SessionFactory = _

  var buffer: PersistBuffer = _

  @mapping("")
  def index(): View = {
    buffer.push(BusinessLogProto.BusinessLogEvent.parseFrom(request.getInputStream))
    Status.Ok
  }

  override def init(): Unit = {
    buffer = new PersistBuffer(logDao, sf, 1024, appService)
  }

  override def destroy(): Unit = {
    buffer.destroy()
  }
}
