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

import org.beangle.commons.codec.binary.Aes
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.EntityDao
import org.beangle.ems.app.Ems
import org.beangle.ems.app.web.WebBusinessLogger
import org.beangle.ems.core.user.model.User
import org.beangle.ids.cas.ticket.TicketRegistry
import org.beangle.ids.cas.web.helper.SessionHelper
import org.beangle.security.Securities
import org.beangle.security.authc.DBCredentialStore
import org.beangle.security.codec.DefaultPasswordEncoder
import org.beangle.security.session.Session
import org.beangle.security.web.WebSecurityManager
import org.beangle.security.web.session.CookieSessionIdPolicy
import org.beangle.webmvc.annotation.mapping
import org.beangle.webmvc.support.{ActionSupport, ServletSupport}
import org.beangle.webmvc.view.View

class EditAction(secuirtyManager: WebSecurityManager, ticketRegistry: TicketRegistry)
  extends ActionSupport with ServletSupport {

  var entityDao: EntityDao = _

  var businessLogger: WebBusinessLogger = _

  var credentialStore: DBCredentialStore = _

  @mapping(value = "")
  def index(): View = {
    put("principal", Securities.session.get.principal)
    put("emsapi", Ems.api)
    forward()
  }

  def save(): View = {
    get("password") foreach { pd =>
      var passwd = pd
      if (passwd.startsWith("?")) {
        passwd = Aes.ECB.decodeHex(loginKey, passwd.substring(1))
      }
      val users = entityDao.findBy(classOf[User], "code", List(Securities.user))
      if (users.size == 1) {
        credentialStore.updatePassword(Securities.user, DefaultPasswordEncoder.generate(passwd, null, "sha"))
      }
      businessLogger.info(Securities.user + "修改了自己的密码", users.head.id, "密码长度" + passwd.length)
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

  private def loginKey: String = {
    val serverName = request.getServerName
    if (serverName.length >= 16) {
      serverName.substring(0, 16)
    } else {
      Strings.rightPad(serverName, 16, '0')
    }
  }
}
