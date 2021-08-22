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

package org.beangle.ems.cas.action

import org.beangle.data.dao.EntityDao
import org.beangle.ems.app.Ems
import org.beangle.ems.core.user.model.User
import org.beangle.ids.cas.ticket.TicketRegistry
import org.beangle.ids.cas.web.helper.SessionHelper
import org.beangle.security.Securities
import org.beangle.security.authc.DBCredentialStore
import org.beangle.security.codec.DefaultPasswordEncoder
import org.beangle.security.session.Session
import org.beangle.security.web.WebSecurityManager
import org.beangle.security.web.session.CookieSessionIdPolicy
import org.beangle.web.action.support.{ActionSupport, ServletSupport}
import org.beangle.web.action.annotation.mapping
import org.beangle.web.action.view.View

class EditAction(secuirtyManager: WebSecurityManager, ticketRegistry: TicketRegistry) extends ActionSupport with ServletSupport {

  var entityDao: EntityDao = _

  var credentialStore: DBCredentialStore = _

  @mapping(value = "")
  def index(): View = {
    put("principal", Securities.session.get.principal)
    put("emsapi", Ems.api)
    forward()
  }

  def save(): View = {
    get("password") foreach { p =>
      val users = entityDao.findBy(classOf[User], "code", List(Securities.user))
      if (users.size == 1) {
        val user = users.head
        credentialStore.updatePassword(Securities.user, DefaultPasswordEncoder.generate(p, null, "sha"))
      }
    }
    get("service") match {
      case None =>
        put("portal", Ems.portal)
        forward("success")
      case Some(service) => forwardService(service, Securities.session.get)
    }
  }

  private def forwardService(service: String, session: Session): View = {
    if (null == service) {
      redirect("success", null)
    } else {
      val idPolicy = secuirtyManager.sessionIdPolicy.asInstanceOf[CookieSessionIdPolicy]
      val isMember = SessionHelper.isMember(request, service, idPolicy)
      if (isMember) {
        if (SessionHelper.isSameDomain(request, service, idPolicy)) {
          redirect(to(service), null)
        } else {
          val serviceWithSid =
            service + (if (service.contains("?")) "&" else "?") + idPolicy.name + "=" + session.id
          redirect(to(serviceWithSid), null)
        }
      } else {
        val ticket = ticketRegistry.generate(session, service)
        redirect(to(service + (if (service.contains("?")) "&" else "?") + "ticket=" + ticket), null)
      }
    }
  }
}
