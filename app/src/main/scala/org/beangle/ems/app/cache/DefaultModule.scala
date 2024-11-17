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

package org.beangle.ems.app.cache

import org.beangle.cache.caffeine.CaffeineCacheManager
import org.beangle.cache.redis.JedisPoolFactory
import org.beangle.commons.cdi.BindModule
import org.beangle.commons.logging.Logging

class DefaultModule extends BindModule, Logging {

  protected override def binding(): Unit = {
    bind("cache.Caffeine", classOf[CaffeineCacheManager]).constructor(true)

    val redis = Redis.conf
    if (redis.nonEmpty) {
      bind("jedis.Factory", classOf[JedisPoolFactory]).constructor(redis)
    }
  }
}
