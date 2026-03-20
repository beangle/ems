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

package org.beangle.ems.core.security.service.impl

import org.beangle.cache.redis.RedisCacheManager
import org.beangle.commons.bean.Initializing
import org.beangle.commons.cache.Cache
import org.beangle.commons.io.DefaultBinarySerializer
import org.beangle.commons.json.{Json, JsonObject}
import org.beangle.data.dao.EntityDao
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.config.model.ThirdPartyApp
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.security.model.{OAuthCode, OAuthToken}
import org.beangle.ems.core.security.service.OAuthService
import org.beangle.ems.core.user.model.User
import org.beangle.security.realm.jwt.{JwtDigest, Jwts}
import redis.clients.jedis.RedisClient

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.{Duration, Instant}
import java.util.{Base64, UUID}

class OAuthServiceImpl extends OAuthService with Initializing {

  var codeTTL: Duration = Duration.ofMinutes(5)
  var tokenTTL: Duration = Duration.ofHours(1)
  private var digest: JwtDigest = _
  private[this] var codes: Cache[String, String] = _
  var domainService: DomainService = _
  var entityDao: EntityDao = _

  override def init(): Unit = {
    val s = EmsApp.properties.getOrElse("openapi.secret", "org.beangle.ems:ems-ws.openapi.secret").toString
    this.digest = Jwts.digest(s)
  }

  def this(client: RedisClient) = {
    this()
    val cacheManager = new RedisCacheManager(client, DefaultBinarySerializer, true)
    cacheManager.ttl = codeTTL.getSeconds.toInt
    codes = cacheManager.getCache("oauth2_code", classOf[String], classOf[String])
  }

  def this(factory: java.util.function.Supplier[RedisClient]) = {
    this(factory.get())
  }

  /** 生成授权码并存入 Redis，强制要求 PKCE
   *
   * @param clientId      客户端编码 (ThirdPartyApp.code)
   * @param userId        用户编码 (User.code)
   * @param scope         授权范围，如 "read write profile"
   * @param codeChallenge PKCE 的 code_challenge，必传
   * @return 生成的授权码
   */
  def generateAuthCode(clientId: String, userId: String, scope: String, codeChallenge: String): String = {
    assert(codeChallenge != null && codeChallenge.nonEmpty, "PKCE code_challenge is required")
    val code = UUID.randomUUID().toString.replace("-", "")
    val expiresAt = Instant.now().plusSeconds(codeTTL.getSeconds)
    val oauthCode = new OAuthCode(code, clientId, userId, scope, codeChallenge, expiresAt)
    codes.put(code, OAuthCode.toJson(oauthCode).toJson)
    code
  }

  /** 验证授权码并生成 access token，强制要求 PKCE
   *
   * 验证通过后授权码将被移除（一次性使用），并生成 JWT token 及数据库记录。
   *
   * @param code         授权码
   * @param clientId     客户端编码，需与授权时一致
   * @param codeVerifier PKCE 的 code_verifier，必传
   * @return (成功, token或错误信息)
   */
  def exchangeCode(code: String, clientId: String, codeVerifier: String): (Boolean, String) = {
    if (code == null || code.isEmpty || clientId == null || codeVerifier == null || codeVerifier.isEmpty)
      return (false, "Invalid parameters")
    val jsonOpt = codes.get(code)
    codes.evict(code)
    if (jsonOpt.isEmpty) return (false, "Invalid code")
    val json = jsonOpt.get

    val oauthCode = OAuthCode.fromJson(Json.parseObject(json))

    if (Instant.now().isAfter(oauthCode.expiresAt)) return (false, "Code expired")
    if (clientId != oauthCode.clientId) return (false, "Client ID mismatch")

    if (!verifyPkceS256(codeVerifier, oauthCode.codeChallenge)) return (false, "PKCE verification failed")

    val domain = domainService.getDomain
    val app = entityDao.findBy(classOf[ThirdPartyApp], "domain" -> domain, "code" -> clientId).headOption
    if (app.isEmpty) {
      return (false, "Client not found")
    }
    val user = entityDao.findBy(classOf[User], "org" -> domain.org, "code" -> oauthCode.userId).headOption
    if (user.isEmpty) {
      return (false, "User not found")
    }
    val tokenData = new JsonObject()
    tokenData.add("userId", oauthCode.userId)
    tokenData.add("clientId", oauthCode.clientId)
    tokenData.add("scope", oauthCode.scope)
    val token = digest.generateToken(tokenData, tokenTTL)

    val otoken = new OAuthToken
    otoken.token = token
    otoken.user = user.get
    otoken.client = app.get
    otoken.scope = oauthCode.scope
    otoken.issuedAt = Instant.now()
    otoken.expiresAt = Instant.now().plusSeconds(tokenTTL.getSeconds)
    entityDao.saveOrUpdate(otoken)
    (true, token)
  }

  private def verifyPkceS256(verifier: String, challenge: String): Boolean = {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(verifier.getBytes(StandardCharsets.UTF_8))
    val computed = Base64.getUrlEncoder.withoutPadding.encodeToString(hash)
    computed == challenge
  }
}
