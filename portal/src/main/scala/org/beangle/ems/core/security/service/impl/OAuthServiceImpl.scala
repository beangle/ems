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

package org.beangle.ems.core.security.service.impl

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.beangle.data.dao.EntityDao
import org.beangle.ems.app.Ems
import org.beangle.ems.core.config.model.ThirdPartyApp
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.security.model.OAuthToken
import org.beangle.ems.core.user.model.User
import org.beangle.ems.core.user.service.UserService
import org.beangle.ids.cas.service.{AbstractOAuthService, OAuthClient, OAuthCode, OAuthResources}
import org.beangle.security.authc.PreauthToken
import org.beangle.security.session.SessionProfile
import org.beangle.security.web.WebSecurityManager
import org.beangle.web.servlet.util.CookieUtils
import redis.clients.jedis.RedisClient

import java.time.Instant

/** ems 的 OAuth2 授权服务实现。
 *
 * 复用 ids 的 {@link AbstractOAuthService} 通用逻辑，仅实现 ems 特有的客户端校验、
 * 用户资源获取与授权码验证通过后的会话建立和令牌持久化。
 */
class OAuthServiceImpl extends AbstractOAuthService {

  var domainService: DomainService = _
  var entityDao: EntityDao = _
  var securityManager: WebSecurityManager = _
  var userService: UserService = _

  def this(client: RedisClient) = {
    this()
    initRedis(client)
  }

  def this(factory: java.util.function.Supplier[RedisClient]) = {
    this()
    initRedis(factory.get())
  }

  protected override def secret: String = {
    Ems.key
  }

  override def findClient(clientId: String): Option[OAuthClient] = {
    val domain = domainService.getDomain
    val apps = entityDao.findBy(classOf[ThirdPartyApp], "domain" -> domain, "code" -> clientId)
    apps.headOption.map(a => OAuthClient(a.id, a.name, a.redirectUri))
  }

  override def getAuthResources(userId: String): Option[OAuthResources] = {
    val domain = domainService.getDomain
    userService.get(userId).map(u => OAuthResources(u, userService.getRoles(u, domain, None)))
  }

  /** 授权码验证通过后，建立用户会话并签发、持久化访问令牌。 */
  override protected def onCodeValidated(oauthCode: OAuthCode)
                                        (request: HttpServletRequest, response: HttpServletResponse): (Boolean, String) = {
    val domain = domainService.getDomain
    val app = entityDao.findBy(classOf[ThirdPartyApp], "domain" -> domain, "code" -> oauthCode.clientId).headOption
    if (app.isEmpty) {
      (false, "Client not found")
    } else {
      val user = entityDao.findBy(classOf[User], "org" -> domain.org, "code" -> oauthCode.userId).headOption
      if (user.isEmpty) {
        (false, "User not found")
      } else {
        // 把token放到credential中
        val authToken = new PreauthToken(user.get.code, oauthCode.code)
        authToken.addDetail("authorities", oauthCode.scope)
        authToken.addDetail("session_profile", SessionProfile.tti(tokenTTL))
        request.setAttribute(CookieUtils.DisableCookie, true)

        val session = securityManager.login(request, response, authToken)
        val jwtToken = buildAccessToken(oauthCode.userId, oauthCode.clientId, oauthCode.scope, session.id)

        val otoken = new OAuthToken
        otoken.token = jwtToken
        otoken.user = user.get
        otoken.client = app.get
        otoken.scope = oauthCode.scope
        otoken.issuedAt = Instant.now()
        otoken.expiredAt = Instant.now().plusSeconds(tokenTTL.getSeconds)
        entityDao.saveOrUpdate(otoken)
        (true, jwtToken)
      }
    }
  }
}
