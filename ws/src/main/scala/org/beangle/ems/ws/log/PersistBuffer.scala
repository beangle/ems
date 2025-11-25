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
import org.beangle.ems.app.log.{ErrorLogEvent, Level, LogEvent, Proto}
import org.beangle.ems.core.config.model.App
import org.beangle.ems.core.config.service.AppService
import org.beangle.ems.core.log.model.{AppLogEntry, BusinessLog, ErrorLog}
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
          val logs = new ju.ArrayList[AnyRef]
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
  private val queue = new ArrayBlockingQueue[AnyRef](queueSize)
  private val worker = new Worker(this)
  private val appName2Id = new mutable.HashMap[String, Int]
  worker.setDaemon(true)
  worker.setName("beangle-log-persister")
  worker.start()

  def push(entry: AnyRef): Unit = {
    queue.put(entry)
  }

  private def persist(events: ju.ArrayList[AnyRef]): Unit = {
    SessionHelper.openSession(sf)
    try {
      val logs = new mutable.ArrayBuffer[AppLogEntry]
      val iter = events.iterator()
      while (iter.hasNext) {
        val event = iter.next()
        event match {
          case be: Proto.BusinessLogEvent =>
            findAppByName(be.getAppName) foreach { app =>
              logs += convert(app, be)
            }
          case ee: Proto.ErrorLogEvent =>
            findAppByName(ee.getAppName) foreach { app =>
              logs += convert(app, ee)
            }
        }
      }
      entityDao.saveOrUpdate(logs)
    } finally {
      SessionHelper.closeSession(sf)
    }
  }

  def findAppByName(appName: String): Option[App] = {
    appName2Id.get(appName) match {
      case Some(id) =>
        val app = new App
        app.id = id
        Some(app)
      case None =>
        appService.getApp(appName).map { app =>
          appName2Id.put(appName, app.id)
          app
        }
    }
  }

  private def convert(app: App, e: Proto.BusinessLogEvent): AppLogEntry = {
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

  private def convert(app: App, e: Proto.ErrorLogEvent): AppLogEntry = {
    val l = new ErrorLog
    l.app = app
    l.stackTrace = e.getStackTrace
    l.exceptionName = e.getExceptionName
    l.requestUrl = e.getRequestUrl
    l.message = e.getMessage
    l.occurredAt = Instant.ofEpochMilli(e.getOccurredAt)
    l.username = Option(e.getUsername)
    l.params = Option(e.getParams)
    l
  }

  override def destroy(): Unit = {
    worker.interrupt()
  }
}
