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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.ems.cas.action

import org.beangle.data.dao.EntityDao
import org.beangle.ems.core.config.model.ThirdPartyApp
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.security.service.OAuthService
import org.beangle.ems.core.user.model.{MemberShip, User}
import org.beangle.security.Securities
import org.beangle.webmvc.annotation.action
import org.beangle.webmvc.annotation.mapping
import org.beangle.webmvc.annotation.param
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.support.ServletSupport
import org.beangle.webmvc.view.View

@action("/oauth/authorize")
class AuthorizeAction extends ActionSupport with ServletSupport {

  var entityDao: EntityDao = _
  var domainService: DomainService = _
  var oauthService: OAuthService = _
  var userService: org.beangle.ems.core.user.service.UserService = _

  /** GET: 展示授权页面 */
  @mapping("")
  def index(@param("client_id") clientId: String, @param("redirect_uri") redirectUri: String,
            @param("response_type") responseType: String, @param("scope") scope: String,
            @param("state") state: String, @param("code_challenge") codeChallenge: String,
            @param("code_challenge_method") codeChallengeMethod: String): View = {
    if (!Securities.session.isDefined) {
      return redirect(to("/login?service=" + java.net.URLEncoder.encode(request.getRequestURI + "?" + Option(request.getQueryString).getOrElse(""), "UTF-8")))
    }
    if (clientId == null || clientId.isEmpty) {
      put("error", "client_id is required")
      return forward("error")
    }
    val domain = domainService.getDomain
    val apps = entityDao.findBy(classOf[ThirdPartyApp], "domain" -> domain, "code" -> clientId)
    if (apps.isEmpty) {
      put("error", "Invalid client_id")
      return forward("error")
    }
    val app = apps.head
    if (app.redirectUri == null || app.redirectUri.isEmpty) {
      put("error", "Client redirect_uri not configured")
      return forward("error")
    }
    if (redirectUri != null && redirectUri.nonEmpty && !redirectUri.startsWith(app.redirectUri)) {
      put("error", "redirect_uri mismatch")
      return forward("error")
    }
    val userOpt = userService.get(Securities.user)
    if (userOpt.isEmpty) {
      put("error", "User not found")
      return forward("error")
    }
    val user = userOpt.get
    val roleMembers = userService.getRoles(user, MemberShip.Member)
    val roles = roleMembers.map(_.role).filter(_.domain == domain)
    put("app", app)
    put("user", user)
    put("roles", roles)
    put("clientId", clientId)
    put("redirectUri", if (redirectUri != null && redirectUri.nonEmpty) redirectUri else app.redirectUri)
    put("responseType", responseType)
    put("scope", scope)
    put("state", state)
    put("codeChallenge", codeChallenge)
    put("codeChallengeMethod", if (codeChallengeMethod != null && codeChallengeMethod.nonEmpty) codeChallengeMethod else "S256")
    forward()
  }

  /** POST: 用户授权确认 */
  @mapping("", "post")
  def approve(@param("client_id") clientId: String, @param("redirect_uri") redirectUri: String,
              @param("response_type") responseType: String, @param("scope") scope: String,
              @param("state") state: String, @param("code_challenge") codeChallenge: String,
              @param("code_challenge_method") codeChallengeMethod: String,
              @param("approved") approved: Boolean): View = {
    if (!Securities.session.isDefined) {
      return redirect(to("/login?service=" + java.net.URLEncoder.encode(request.getRequestURI, "UTF-8")))
    }
    val domain = domainService.getDomain
    val apps = entityDao.findBy(classOf[ThirdPartyApp], "domain" -> domain, "code" -> clientId)
    if (apps.isEmpty) {
      val uri = if (redirectUri != null && redirectUri.nonEmpty) redirectUri else ""
      return redirectToError(uri, state, "invalid_client")
    }
    val app = apps.head
    val targetRedirectUri = if (redirectUri != null && redirectUri.nonEmpty) redirectUri else app.redirectUri
    if (!approved) {
      return redirectToError(targetRedirectUri, state, "access_denied")
    }
    if (codeChallenge == null || codeChallenge.isEmpty) {
      return redirectToError(targetRedirectUri, state, "invalid_request")
    }
    val userOpt = userService.get(Securities.user)
    if (userOpt.isEmpty) {
      return redirectToError(targetRedirectUri, state, "server_error")
    }
    val scopeValues = request.getParameterValues("scope")
    val scopeValue = if (scopeValues != null && scopeValues.nonEmpty) scopeValues.mkString(" ") else ""
    val code = oauthService.generateAuthCode(clientId, userOpt.get.code, scopeValue, codeChallenge)
    val sep = if (targetRedirectUri.contains("?")) "&" else "?"
    redirect(to(targetRedirectUri + sep + "code=" + code + "&state=" + (if (state != null) state else "")))
  }

  private def redirectToError(redirectUri: String, state: String, error: String): View = {
    if (redirectUri == null || redirectUri.isEmpty) {
      put("error", error)
      return forward("error")
    }
    val sep = if (redirectUri.contains("?")) "&" else "?"
    redirect(to(redirectUri + sep + "error=" + error + "&state=" + (if (state != null) state else "")))
  }
}
