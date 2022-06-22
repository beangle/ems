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

import org.beangle.security.Securities

import java.time.Instant

object BusinessLogStore {
  def newEntry(summary: String): BusinessLogEvent = {
    val entry = new BusinessLogEvent()
    entry.operator = Securities.user
    entry.operateAt = Instant.now
    entry.entry = Securities.resource
    Securities.session foreach { s =>
      entry.agent = s"${s.agent.os} ${s.agent.name}"
    }
    entry.summary = summary
    entry
  }
}

trait BusinessLogStore {

  def publish(entry: BusinessLogEvent): Unit
}
