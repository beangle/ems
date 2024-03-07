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

import org.beangle.cdi.bind.BindModule
import org.beangle.commons.logging.Logging
import org.beangle.commons.net.http.HttpUtils
import org.beangle.ems.app.EmsApi
import org.beangle.event.bus.{DataEvent, DataEventSerializer, DefaultDataEventBus}
import org.beangle.event.mq.impl.{NullChannelQueue, RedisChannelQueue}

class EventModule extends BindModule, Logging {

  protected override def binding(): Unit = {
    wiredEagerly(true)

    val res = HttpUtils.getText(EmsApi.getRedisUrl)
    if (res.isOk) {
      val root = scala.xml.XML.loadString(res.getText)
      if (root \\ "redis").nonEmpty then
        bind("appChannel", classOf[RedisChannelQueue[DataEvent]]).constructor("app_data", ?, new DataEventSerializer)
        bind(classOf[CacheEvictorRegister])
        bind(classOf[DefaultDataEventBus]).constructor(ref("appChannel"))
        bind(classOf[RemoteAuthorizerRefresher])
      else
        logger.error(s"Cannot parse app redis config")
    } else {
      bind("appChannel", NullChannelQueue)
      bind(classOf[DefaultDataEventBus])
    }
  }

}

/** It only bind publishing channel.
  */
class EventPublishModule extends BindModule, Logging {
  protected override def binding(): Unit = {
    wiredEagerly(true)
    val res = HttpUtils.getText(EmsApi.getRedisUrl)
    if (res.isOk) {
      val root = scala.xml.XML.loadString(res.getText)
      if (root \\ "redis").nonEmpty then
        bind("appChannel", classOf[RedisChannelQueue[DataEvent]]).constructor("app_data", ?, new DataEventSerializer)
          .property("publishOnly", true)
      else
        logger.error(s"Cannot parse app redis config")
    } else {
      logger.error(s"Cannot fetch redis config from ems service")
      bind("appChannel", NullChannelQueue)
    }
  }
}
