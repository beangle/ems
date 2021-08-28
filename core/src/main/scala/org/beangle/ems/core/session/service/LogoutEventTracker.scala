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

package org.beangle.ems.core.session.service

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}

import org.beangle.commons.event.{Event, EventListener}
import org.beangle.data.dao.EntityDao
import org.beangle.security.authc.Account
import org.beangle.security.session.{EventType, LogoutEvent}
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.session.model.SessionEvent

class LogoutEventTracker extends EventListener[LogoutEvent] {
  var entityDao: EntityDao = _

  var domainService: DomainService = _
  private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

  override def onEvent(event: LogoutEvent): Unit = {
    val logout = new SessionEvent
    logout.eventType = EventType.Logout
    logout.updatedAt = Instant.now
    val session = event.session
    logout.principal = session.principal.getName
    logout.username = session.principal.asInstanceOf[Account].description

    val logoutType =
      if (session.ttiSeconds == 0) {
        "强制退出"
      } else if (session.expired) {
        "过期"
      } else {
        "退出"
      }
    logout.name = logout.principal + " " + logout.username + " " + logoutType

    logout.ip = session.agent.ip
    val details = session.agent.name + " " +
      session.agent.os + " 最后访问" + formatter.format(session.lastAccessAt.atZone(ZoneId.systemDefault())) +
      " " + (if (null != event.reason) event.reason + "退出" else "")
    logout.detail = details
    logout.domain = domainService.getDomain
    entityDao.saveOrUpdate(logout)
  }

  override def supportsEventType(eventType: Class[_ <: Event]): Boolean = {
    classOf[LogoutEvent].isAssignableFrom(eventType)
  }

  override def supportsSourceType(sourceType: Class[_]): Boolean = {
    true
  }
}
