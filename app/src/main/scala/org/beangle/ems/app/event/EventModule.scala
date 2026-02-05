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

package org.beangle.ems.app.event

import org.beangle.commons.cdi.BindModule
import org.beangle.ems.app.AppLogger
import org.beangle.ems.app.cache.Redis
import org.beangle.event.bus.{DataEvent, DataEventSerializer, DefaultDataEventBus}
import org.beangle.event.mq.impl.{NullChannelQueue, RedisChannelQueue}

object EventModule extends BindModule {

  val channelName = "publicChannel"

  val queueName = "ems_public"

  protected override def binding(): Unit = {
    wiredEagerly(true)

    val redis = Redis.conf
    if (redis.nonEmpty) {
      AppLogger.info(s"using redis on ${queueName} to notify data evict event.")
      bind(channelName, classOf[RedisChannelQueue[DataEvent]]).constructor(queueName, ?, new DataEventSerializer)
      bind(classOf[CacheEvictorRegister])
      bind(classOf[DefaultDataEventBus]).constructor(ref(channelName))
      bind(classOf[RemoteAuthorizerRefresher])
    } else {
      AppLogger.warn(s"Disable databus due to missing redis config.")
      bind(channelName, NullChannelQueue)
      bind(classOf[DefaultDataEventBus])
    }
  }

}

/** It only bind publishing channel.
 */
object EventPublishModule extends BindModule {
  protected override def binding(): Unit = {
    wiredEagerly(true)
    val redis = Redis.conf
    if (redis.nonEmpty) {
      bind(EventModule.channelName, classOf[RedisChannelQueue[DataEvent]]).constructor(EventModule.queueName, ?, new DataEventSerializer)
        .property("publishOnly", true)
    } else {
      bind(EventModule.channelName, NullChannelQueue)
    }
  }
}
