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

import org.beangle.commons.bean.Initializing
import org.beangle.data.dao.EntityDao
import org.beangle.data.hibernate.SessionHelper
import org.beangle.data.model.Entity
import org.beangle.event.bus.DataEvent
import org.beangle.event.mq.{ChannelQueue, EventSubscriber}

class CacheEvictor(entityDao: EntityDao, queue: ChannelQueue[DataEvent]) extends EventSubscriber[DataEvent], Initializing {

  override def init(): Unit = {
    queue.subscribe(this)
  }

  override def process(event: DataEvent): Unit = {
    val domain = entityDao.domain
    val dataType = event.dataType
    domain.getEntity(dataType) foreach { et =>
      if et.cacheable then
        val s = entityDao.openSession()
        try {
          entityDao.evict(et.clazz.asInstanceOf[Class[_ <: Entity[_]]])
        } finally {
          SessionHelper.closeSession(s)
        }
    }
  }

}
