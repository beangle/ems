/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2020, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.ems.core.oauth.service.impl

import org.beangle.cache.{Cache, CacheManager}
import org.beangle.ems.core.config.model.AccessToken
import org.beangle.ems.core.oauth.service.TokenRepository
import org.beangle.ems.core.config.model.AccessToken

class MemTokenRepository(val tokens: Cache[String, AccessToken]) extends TokenRepository {

  override def get(token: String): Option[AccessToken] = {
    tokens.get(token)
  }

  override def put(token: AccessToken): Unit = {
    tokens.put(token.id, token)
  }

  override def remove(token: String): Unit = {
    tokens.evict(token)
  }
}
