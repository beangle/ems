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

package org.beangle.ems.app.log

import org.beangle.commons.bean.{Disposable, Initializing}
import org.beangle.commons.concurrent.Sidecar

class AsyncAppLogger extends BusinessLogger, ErrorLogger, Initializing, Disposable {
  var appenders: List[Appender] = _
  private var sidecar: Sidecar[LogEvent] = _

  override def publish(event: BusinessLogEvent): Unit = {
    sidecar.offer(event)
  }

  override def publish(event: ErrorLogEvent): Unit = {
    sidecar.offer(event)
  }

  override def init(): Unit = {
    sidecar = new Sidecar[LogEvent]("beangle-ems-logger", e => {
      appenders foreach (ap => ap.append(e))
    })
  }

  override def destroy(): Unit = {
    sidecar.destroy()
  }
}
