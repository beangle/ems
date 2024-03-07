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
import org.beangle.ems.app.event.CacheEvictor
import org.beangle.event.bus.DataEvent
import org.beangle.event.mq.ChannelQueue
import org.hibernate.SessionFactory

class CacheEvictorRegister(queue: ChannelQueue[DataEvent]) extends Initializing {
  var entityDao: EntityDao = _
  var sessionFactory: SessionFactory = _

  override def init(): Unit = {
    queue.subscribe(new CacheEvictor(entityDao, sessionFactory))
  }
}
