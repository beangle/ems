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
import org.beangle.ems.app.{AppLogger, EmsApp}
import org.beangle.event.bus.{DataEvent, DataEventSerializer, DataEventSubscriberRegistrar, DefaultDataEventBus}
import org.beangle.event.mq.impl.{NullChannelQueue, RedisChannelQueue}

object EventModule extends BindModule {

  protected override def binding(): Unit = {
    wiredEagerly(true)
    val channelBeanName = "publicChannel"
    val queueName = "ems_public"
    val redis = EmsApp.redisConf
    if (redis.nonEmpty) {
      AppLogger.info(s"Using redis on ${queueName} to notify data evict event.")
      bind(channelBeanName, classOf[RedisChannelQueue[DataEvent]]).constructor(queueName, ?, new DataEventSerializer)
      bind(classOf[CacheEvictor])
      bind(classOf[DefaultDataEventBus]).constructor(ref(channelBeanName))
      bind(classOf[AppAuthorizerSubscriber])
    } else {
      AppLogger.warn(s"Disable databus due to missing redis config.")
      bind(channelBeanName, NullChannelQueue)
      bind(classOf[DefaultDataEventBus])
    }
    //绑定数据事件订阅注册表
    bind(classOf[DataEventSubscriberRegistrar])
  }
}
