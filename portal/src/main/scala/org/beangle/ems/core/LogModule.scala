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

package org.beangle.ems.core

import org.beangle.commons.cdi.BindModule
import org.beangle.ems.app.log.{AsyncAppLogger, LogExceptionHandler, WebBusinessLogger}
import org.beangle.ems.core.log.service.{LogDbAppender, LogPersistBuffer}
import org.beangle.webmvc.dispatch.ExceptionHandler

class LogModule extends BindModule {

  protected override def binding(): Unit = {
    //日志 Web Logger
    bind(classOf[WebBusinessLogger])
    bind(classOf[LogPersistBuffer]).constructor(?, ?, 1024)
    bind(classOf[AsyncAppLogger]).property("appenders", list(classOf[LogDbAppender]))

    //如果生产环境，则启用日志上报功能
    if (!devEnabled) {
      bind(classOf[LogExceptionHandler]).primaryOf(classOf[ExceptionHandler])
    }
  }

}
