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

package org.beangle.ems.core

import org.beangle.commons.cdi.BindModule
import org.beangle.ems.app.event.CacheEvictorRegister
import org.beangle.event.bus.{DataEvent, DataEventSerializer, DefaultDataEventBus}
import org.beangle.event.mq.impl.RedisChannelQueue

class EventModule extends BindModule {

  protected override def binding(): Unit = {
    wiredEagerly(true)
    //using redis as pubsub
    val queueBean = "channelQueue"
    bind(queueBean, classOf[RedisChannelQueue[DataEvent]])
      .constructor("ems_platform", ref("jedis.Factory"), new DataEventSerializer).primary()
    bind(classOf[CacheEvictorRegister]).constructor(ref(queueBean))
    bind("databus", classOf[DefaultDataEventBus]).constructor(ref(queueBean))
  }

}
