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

package org.beangle.ems.core.security.service

trait OAuthService {

  /** 生成授权码并存入 Redis，强制要求 PKCE */
  def generateAuthCode(clientId: String, userId: String, scope: String, codeChallenge: String): String

  /** 验证授权码并生成 access token，强制要求 PKCE */
  def exchangeCode(code: String, clientId: String, codeVerifier: String): (Boolean, String)
}
