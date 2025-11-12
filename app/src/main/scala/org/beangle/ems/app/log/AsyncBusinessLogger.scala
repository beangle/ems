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

class AsyncBusinessLogger extends BusinessLogger with Initializing with Disposable {
  var appenders: List[Appender] = _
  var sidecar: Sidecar[BusinessLogEvent] = _

  override def publish(entry: BusinessLogEvent): Unit = {
    sidecar.offer(entry)
  }

  override def init(): Unit = {
    sidecar = new Sidecar[BusinessLogEvent]("beangle-ems-async-logger", e => {
      appenders foreach (ap => ap.append(e))
    })
  }

  override def destroy(): Unit = {
    sidecar.destroy()
  }
}
