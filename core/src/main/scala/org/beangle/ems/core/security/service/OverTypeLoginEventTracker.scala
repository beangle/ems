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

package org.beangle.ems.core.security.service

import org.beangle.commons.event.{Event, EventListener}
import org.beangle.data.dao.EntityDao
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.security.model.SessionEvent
import org.beangle.ems.core.user.service.UserService
import org.beangle.security.session.{EventType, OverTryLoginEvent}

import java.time.Instant

class OverTypeLoginEventTracker extends EventListener[OverTryLoginEvent] {
  var entityDao: EntityDao = _

  var userService: UserService = _

  var domainService: DomainService = _

  override def onEvent(event: OverTryLoginEvent): Unit = {

    userService.get(event.principal) foreach { user =>
      val login = new SessionEvent
      login.eventType = EventType.Login
      login.updatedAt = Instant.now
      login.principal = event.principal
      login.username = user.name

      login.name = login.principal + " " + login.username + s" 登录失败${event.failCount}次"

      val agent = event.agent
      login.ip = agent.ip
      login.detail = agent.name + " " + agent.os
      login.domain = domainService.getDomain
      entityDao.saveOrUpdate(login)
    }
  }

  override def supportsEventType(eventType: Class[_ <: Event]): Boolean = {
    classOf[OverTryLoginEvent].isAssignableFrom(eventType)
  }

  override def supportsSourceType(sourceType: Class[_]): Boolean = {
    true
  }
}
