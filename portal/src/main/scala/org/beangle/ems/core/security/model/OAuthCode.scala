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

package org.beangle.ems.core.security.model

import org.beangle.commons.json.{JsonMapper, JsonObject}

import java.io.Serializable
import java.time.Instant

/** OAuth2 授权码
 *
 * 用于记录 OAuth2 授权码流程中的授权信息，支持 PKCE (RFC 7636)。
 * 该模型不持久化到数据库，设计用于 Redis 存储，通过 JSON 序列化。
 *
 * @param code          授权码，用户授权后生成，用于换取 access token
 * @param clientId      客户端编码 (ThirdPartyApp.code)
 * @param userId        用户编码 (User.code)
 * @param scope         授权范围，如 "read write profile"，多个用空格分隔
 * @param codeChallenge PKCE 的 code_challenge，token 交换时用于验证 code_verifier；非 PKCE 流程可传空字符串
 * @param expiresAt     授权码过期时间(以秒为单位)
 */
class OAuthCode(val code: String, val clientId: String, val userId: String, val scope: String,
                val codeChallenge: String, val expiresAt: Instant) extends Serializable {
}

object OAuthCode extends JsonMapper[OAuthCode] {

  /** 序列化为 JsonObject，用于 Redis 存储 */
  def toJson(code: OAuthCode): JsonObject = {
    val jo = new JsonObject()
    jo.add("code", code.code)
    jo.add("clientId", code.clientId)
    jo.add("userId", code.userId)
    jo.add("scope", code.scope)
    jo.add("codeChallenge", code.codeChallenge)
    jo.add("expiresAt", code.expiresAt.getEpochSecond)
    jo
  }

  /** 从 JsonObject 反序列化，用于从 Redis 读取 */
  def fromJson(jo: JsonObject): OAuthCode = {
    val code = jo.getString("code")
    val clientId = jo.getString("clientId")
    val userId = jo.getString("userId")
    val scope = jo.getString("scope")
    val codeChallenge = jo.getString("codeChallenge")
    val expiresAt = Instant.ofEpochSecond(jo.getLong("expiresAt"))
    new OAuthCode(code, clientId, userId, scope, codeChallenge, expiresAt)
  }
}
