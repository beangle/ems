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

import jakarta.servlet.http.HttpServletResponse
import org.beangle.commons.lang.Strings
import org.beangle.ems.core.user.service.UserService
import org.beangle.ids.cas.service.CasService
import org.beangle.ids.cas.ticket.TicketRegistry
import org.beangle.ids.cas.web.helper.LoginHelper
import org.beangle.ids.sms.service.SmsCodeService
import org.beangle.notify.sms.Receiver
import org.beangle.security.Securities
import org.beangle.security.authc.PreauthToken
import org.beangle.security.context.SecurityContext
import org.beangle.security.session.Session
import org.beangle.security.web.WebSecurityManager
import org.beangle.security.web.access.SecurityContextBuilder
import org.beangle.web.action.annotation.{mapping, param}
import org.beangle.web.action.support.{ActionSupport, ServletSupport}
import org.beangle.web.action.view.View

class SmsLoginAction(securityManager: WebSecurityManager, ticketRegistry: TicketRegistry)
  extends ActionSupport with ServletSupport {

  var casService: CasService = _

  var userService: UserService = _

  var smsCodeService: SmsCodeService = _

  var securityContextBuilder: SecurityContextBuilder = _

  @mapping(value = "")
  def login(@param(value = "service", required = false) service: String): View = {
    Securities.session match {
      case Some(session) => forwardService(service, session)
      case None =>
        val userName = get("username", "--").trim()
        val smsCode = get("smsCode", "--").trim()
        if (userName == "--" || smsCode == "--") {
          toLogin(null)
        } else {
          userService.get(userName) match
            case None =>
              toLogin("登录失败，用户名不存在。")
            case Some(user) =>
              user.mobile match
                case None => toLogin("该用户未绑定手机。")
                case Some(mobile) =>
                  if (smsCodeService.verify(mobile, smsCode)) {
                    val token = PreauthToken(userName, null)
                    val session = securityManager.login(request, response, token)
                    SecurityContext.set(securityContextBuilder.build(request, Some(session)))
                    forwardService(service, session)
                  } else {
                    toLogin("验证码错误")
                  }
        }
    }
  }

  def send(): View = {
    val userName = get("username", "")
    val result = userService.get(userName) match
      case None => "发送失败，用户名不存在。"
      case Some(user) =>
        user.mobile match
          case None => "发送失败，该用户未绑定手机。"
          case Some(mobile) =>
            if smsCodeService.validate(mobile) then
              smsCodeService.send(Receiver(mobile, user.name))
            else
              s"手机号码${mobile}不正确"
    response.setCharacterEncoding("utf-8")
    response.getWriter.print(result)
    null
  }

  private def forwardService(service: String, session: Session): View = {
    new LoginHelper(securityManager, ticketRegistry, casService).forwardService(request, response, this, service, session)
  }

  def success(): View = {
    forward()
  }

  private def redirectService(response: HttpServletResponse, service: String): View = {
    response.sendRedirect(service)
    null
  }

  private def toLogin(msg: String): View = {
    if (Strings.isNotBlank(msg)) put("error", msg)
    forward("index")
  }
}
