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
import org.beangle.security.authc.Account
import org.beangle.security.session.{EventType, LoginEvent}

import java.time.Instant

class LoginEventTracker extends EventListener[LoginEvent] {
  var entityDao: EntityDao = _

  var domainService: DomainService = _

  override def onEvent(event: LoginEvent): Unit = {
    val login = new SessionEvent
    login.eventType = EventType.Login
    login.updatedAt = Instant.now
    val session = event.session

    login.principal = session.principal.getName
    login.username = session.principal.asInstanceOf[Account].description

    login.name = login.principal + " " + login.username + " 登录"

    login.ip = session.agent.ip
    login.detail = session.agent.name + " " + session.agent.os
    login.domain = domainService.getDomain
    entityDao.saveOrUpdate(login)
  }

  override def supportsEventType(eventType: Class[_ <: Event]): Boolean = {
    classOf[LoginEvent].isAssignableFrom(eventType)
  }

  override def supportsSourceType(sourceType: Class[_]): Boolean = {
    true
  }
}
