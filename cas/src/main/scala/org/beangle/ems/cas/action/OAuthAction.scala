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

import org.beangle.commons.json.JsonObject
import org.beangle.data.dao.EntityDao
import org.beangle.ems.core.config.model.ThirdPartyApp
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.security.service.OAuthService
import org.beangle.ems.core.user.service.UserService
import org.beangle.security.Securities
import org.beangle.web.servlet.url.UrlBuilder
import org.beangle.webmvc.annotation.{action, param}
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.support.{ActionSupport, ServletSupport}
import org.beangle.webmvc.view.View

@action("/oauth")
class OAuthAction extends ActionSupport with ServletSupport {

  var entityDao: EntityDao = _
  var domainService: DomainService = _
  var oauthService: OAuthService = _
  var userService: UserService = _

  /** GET: 展示授权页面 */
  def authorize(@param("client_id") clientId: String): View = {
    if (Securities.session.isEmpty) {
      val service = UrlBuilder(ActionContext.current.request).buildUrl()
      return redirect(to("/login", Map("service" -> service)))
    }
    if (clientId == null || clientId.isEmpty) {
      put("error", "client_id is required")
      return forward("error")
    }
    val codeChallenge = get("code_challenge")
    if (codeChallenge.isEmpty) {
      put("error", "code_challenge is required")
      return forward("error")
    }
    val domain = domainService.getDomain
    val apps = entityDao.findBy(classOf[ThirdPartyApp], "domain" -> domain, "code" -> clientId)
    if (apps.isEmpty) {
      put("error", "Invalid client_id")
      return forward("error")
    }
    val app = apps.head
    val redirectUri = get("redirect_uri").orNull
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

    put("app", app)
    put("user", user)
    put("roles", userService.getRoles(user, domain))
    put("clientId", clientId)
    put("tokenTTL", oauthService.tokenTTL)
    put("redirectUri", if (redirectUri != null && redirectUri.nonEmpty) redirectUri else app.redirectUri)
    forward()
  }

  /** POST: 用户授权确认 */
  def approve(@param("client_id") clientId: String, @param("code_challenge") codeChallenge: String): View = {
    val redirectUri = get("redirect_uri").orNull
    val scope = get("scope").orNull
    val state = get("state").orNull

    val domain = domainService.getDomain
    val apps = entityDao.findBy(classOf[ThirdPartyApp], "domain" -> domain, "code" -> clientId)
    if (apps.isEmpty) {
      val uri = if (redirectUri != null && redirectUri.nonEmpty) redirectUri else ""
      return redirectToError(uri, state, "invalid_client")
    }
    val app = apps.head
    val targetRedirectUri = if (redirectUri != null && redirectUri.nonEmpty) redirectUri else app.redirectUri
    val approved = getBoolean("approved", false)
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
    redirect(to(targetRedirectUri + sep + "code=" + code + "&state=" + (if (state != null) state else "")), "")
  }

  private def redirectToError(redirectUri: String, state: String, error: String): View = {
    if (redirectUri == null || redirectUri.isEmpty) {
      put("error", error)
      return forward("error")
    }
    val sep = if (redirectUri.contains("?")) "&" else "?"
    redirect(to(redirectUri + sep + "error=" + error + "&state=" + (if (state != null) state else "")), null)
  }

  /** 尝试用授权码换取 token，并将结果直接展示到网页
   */
  def token(): View = {
    val clientId = get("client_id").orNull
    val code = get("code").orNull
    val codeVerifier = get("code_verifier").orNull
    val rs = new JsonObject()
    if (clientId == null || clientId.isEmpty || code == null || code.isEmpty || codeVerifier == null || codeVerifier.isEmpty) {
      rs.add("error", "invalid_request")
      rs.add("error_description", "Missing required params: client_id, code, code_verifier")
      error(400, rs)
    } else {
      val request = ActionContext.current.request
      val response = ActionContext.current.response
      val (success, tokenOrError) = oauthService.exchangeCode(code, clientId, codeVerifier)(request, response)
      if (success) {
        rs.add("access_token", tokenOrError)
        ok(rs)
      } else {
        rs.add("error", "invalid_grant")
        rs.add("error_description", tokenOrError)
        error(400, rs)
      }
    }
  }

  //  var verifyCode: String = _
  //
  //  def testSendAuthorize(): View = {
  //    val config = new OAuthConfig("http://local.openurp.net/cas", "myself")
  //    val helper = new OAuthHelper(config)
  //    val req = helper.buildAuthorizeUrl("http://local.openurp.net/cas/oauth/test", Some("test"))
  //    verifyCode = req.codeVerifier
  //    redirect(to(req.url), "")
  //  }
  //
  //  /** 测试：假装 OAuth2 code 消费方
  //   *
  //   * 支持两种模式：
  //   * 1) 直接用已有 authorization code 换 token：传 client_id/code/code_verifier
  //   * 2) 若不传 code，则用 code_challenge 直接生成 code：传 client_id/code_verifier/code_challenge(+scope可选)
  //   */
  //  def test(): View = {
  //    val config = new OAuthConfig("http://local.openurp.net/cas", "myself")
  //    val helper = new OAuthHelper(config)
  //    val codeFromClient = get("code").orNull
  //    val response = helper.getToken(codeFromClient, verifyCode)
  //    ActionContext.current.response.getWriter.println(response.getText)
  //    null
  //  }
}
