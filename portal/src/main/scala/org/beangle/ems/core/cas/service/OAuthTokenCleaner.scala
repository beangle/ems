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

package org.beangle.ems.core.cas.service

import org.beangle.commons.bean.Scheduled
import org.beangle.data.orm.AbstractDaoTask
import org.beangle.ems.EmsLogger
import org.beangle.ems.core.security.model.OAuthToken

import java.time.Instant

/**
 * 清理过期的Token
 */
class OAuthTokenCleaner(val expression: String) extends AbstractDaoTask, Scheduled {

  def execute(): Unit = {
    val removed = entityDao.executeUpdate(s"delete from ${classOf[OAuthToken].getName} token where token.expiredAt <= ?1", Instant.now)
    if (removed > 0) {
      EmsLogger.info(s"Evict ${removed} expired oauth access tokens")
    }
  }
}
