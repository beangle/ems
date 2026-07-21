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

package org.beangle.ems.app.web

import jakarta.servlet.http.HttpServletRequest
import org.beangle.commons.codec.digest.Digests
import org.beangle.commons.collection.Collections
import org.beangle.ems.app.security.RemoteService
import org.beangle.ems.app.{Ems, EmsApp}
import org.beangle.security.Securities
import org.beangle.security.authc.Account
import org.beangle.security.context.RunAs
import org.beangle.security.web.{CookieKeys, ProfileCookie}
import org.beangle.web.servlet.url.UrlBuilder
import org.beangle.web.servlet.util.{CookieUtils, RequestUtils}
import org.beangle.webmvc.context.ActionContext

object NavContext {
  def get(request: HttpServletRequest): NavContext = {
    val ctx = new NavContext
    ctx.domain = RemoteService.getDomain(ActionContext.current.locale)
    if null == ctx.domain.org then ctx.org = RemoteService.getOrg
    else ctx.org = ctx.domain.org

    val builder = new UrlBuilder(request.getContextPath)
    builder.setScheme(if (RequestUtils.isHttps(request)) "https" else "http")
      .setServerName(request.getServerName)
      .setPort(RequestUtils.getServerPort(request))
    ctx.app = App(EmsApp.name, builder.buildUrl())
    ctx.params += ("webapp" -> Ems.webapp)
    if (null == ActionContext.current) {
      val names = request.getParameterNames
      while (names.hasMoreElements) {
        val n = names.nextElement()
        ctx.params.put(n, request.getParameter(n))
      }
    } else {
      ActionContext.current.params foreach {
        case (k, v) =>
          ctx.params += (k -> v.toString)
      }
    }
    val account = Securities.session.get.principal
    if (null == account.profiles) {
      ctx.profiles = Some("[]")
    } else {
      val response = ActionContext.current.response

      var profiles = account.profiles.toSeq
      val runAsJson = CookieUtils.getCookieValue(request, CookieKeys.RunAsKey)
      if (null != runAsJson) {
        RunAs.parseJson(runAsJson) foreach { r => profiles = r.profiles }
      }

      // cookie / URL 参数 → 校验是否在可用列表中；无则回落到第一个 profile
      val resolvedProfileId = ProfileCookie.check(profiles, ProfileCookie.get(request, response).getOrElse(""))
      resolvedProfileId foreach { pid =>
        ProfileCookie.update(request, response, pid, true)
        ctx.profileId = pid
      }

      val sb = Collections.newBuffer[String]

      profiles foreach { profile =>
        sb += profile.toJson
      }
      ctx.profiles = Some("[" + sb.mkString(",") + "]")
    }
    ctx.menusJson = RemoteService.getDomainMenusJson(ActionContext.current.locale, Option(ctx.profileId))
    ctx.theme = RemoteService.getTheme
    ctx
  }
}

class NavContext {
  val params = Collections.newMap[String, String]
  var menusJson: String = _
  var org: Ems.Org = _
  var domain: Ems.Domain = _
  var app: App = _
  var principal = Securities.session.get.principal
  var username = Securities.user
  var profiles: Option[String] = None
  /** 当前选中的 profile id；无可用 profile 时为 null */
  var profileId: String = _
  var theme: Ems.Theme = _

  def ems = Ems

  def avatarUrl: String = {
    Ems.api + "/platform/user/avatars/" + Digests.md5Hex(principal.getName)
  }
}

case class App(name: String, base: String) {
}
