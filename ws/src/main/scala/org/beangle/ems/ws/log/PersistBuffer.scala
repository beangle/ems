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

package org.beangle.ems.ws.log

import org.beangle.commons.bean.Disposable
import org.beangle.data.dao.EntityDao
import org.beangle.data.orm.hibernate.SessionHelper
import org.beangle.ems.app.log.BusinessLogProto.BusinessLogEvent
import org.beangle.ems.app.log.Level
import org.beangle.ems.core.config.model.App
import org.beangle.ems.core.config.service.AppService
import org.beangle.ems.core.log.model.BusinessLog
import org.beangle.ems.ws.log.PersistBuffer.Worker
import org.hibernate.SessionFactory

import java.time.Instant
import java.util as ju
import java.util.concurrent.ArrayBlockingQueue
import scala.collection.mutable

object PersistBuffer {
  private class Worker(store: PersistBuffer) extends Thread {
    var stopped: Boolean = false

    override def run(): Unit = {
      while (!stopped) {
        try {
          val logs = new ju.ArrayList[BusinessLogEvent]
          val e0 = store.queue.take()
          logs.add(e0)
          store.queue.drainTo(logs)
          store.persist(logs)
        } catch {
          case _: InterruptedException => stopped = true
        }
      }
    }
  }
}

class PersistBuffer(entityDao: EntityDao, sf: SessionFactory, queueSize: Int, appService: AppService) extends Disposable {
  private val queue = new ArrayBlockingQueue[BusinessLogEvent](queueSize)
  private val worker = new Worker(this)
  private val appName2Id = new mutable.HashMap[String, Int]
  worker.setDaemon(true)
  worker.setName("beangle-business-log-persister")
  worker.start()

  def push(entry: BusinessLogEvent): Unit = {
    queue.put(entry)
  }

  private def persist(events: ju.ArrayList[BusinessLogEvent]): Unit = {
    SessionHelper.openSession(sf)
    try {
      val logs = new mutable.ArrayBuffer[BusinessLog]
      val iter = events.iterator()
      while (iter.hasNext) {
        val event = iter.next()
        appName2Id.get(event.getAppName) match {
          case Some(id) =>
            val app = new App
            app.id = id
            logs += convert(app, event)
          case None =>
            appService.getApp(event.getAppName) foreach { app =>
              appName2Id.put(event.getAppName, app.id)
              logs += convert(app, event)
            }
        }
      }
      entityDao.saveOrUpdate(logs)
    } finally {
      SessionHelper.closeSession(sf)
    }
  }

  private def convert(app: App, e: BusinessLogEvent): BusinessLog = {
    val l = new BusinessLog()
    l.app = app
    l.operator = e.getOperator
    l.operateAt = Instant.ofEpochMilli(e.getOperateAt)
    l.summary = e.getSummary
    l.details = e.getDetails
    l.resources = e.getResources
    l.ip = e.getIp
    l.agent = e.getAgent
    l.entry = e.getEntry
    l.logLevel = Level.fromOrdinal(e.getLevel - 1) //level is 1-based
    l
  }

  override def destroy(): Unit = {
    worker.interrupt()
  }
}
