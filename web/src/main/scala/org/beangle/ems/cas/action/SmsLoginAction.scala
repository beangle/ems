package org.beangle.ems.cas.action

import jakarta.servlet.http.HttpServletResponse
import org.beangle.commons.lang.Strings
import org.beangle.ems.core.user.model.Account
import org.beangle.ems.core.user.service.AccountService
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

  var accountService: AccountService = _

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
          accountService.get(userName) match
            case None =>
              toLogin("登录失败，用户名不存在。")
            case Some(account) =>
              account.user.mobile match
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
    val result = accountService.get(userName) match
      case None => "发送失败，用户名不存在。"
      case Some(account) =>
        account.user.mobile match
          case None => "发送失败，该用户未绑定手机。"
          case Some(mobile) => smsCodeService.send(Receiver(mobile, account.user.name))

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
