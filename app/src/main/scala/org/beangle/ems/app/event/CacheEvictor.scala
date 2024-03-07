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

import org.beangle.data.dao.EntityDao
import org.beangle.data.model.Entity
import org.beangle.data.orm.hibernate.SessionHelper
import org.beangle.event.bus.DataEvent
import org.beangle.event.bus.DataEventType.*
import org.beangle.event.mq.EventSubscriber
import org.hibernate.SessionFactory

class CacheEvictor(entityDao: EntityDao, sessionFactory: SessionFactory) extends EventSubscriber[DataEvent] {
  override def process(event: DataEvent): Unit = {
    val domain = entityDao.domain
    val entityName = event.entityName
    domain.getEntity(entityName) foreach { et =>
      if et.cacheable then
        val h = SessionHelper.openSession(sessionFactory)
        try {
          entityDao.evict(et.clazz.asInstanceOf[Class[_ <: Entity[_]]])
        } finally {
          SessionHelper.closeSession(h.session)
        }
    }
  }

}
