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

package org.beangle.ems.core.log.service

import org.beangle.commons.bean.Disposable
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.EntityDao
import org.beangle.ems.app.log.{BusinessLogEvent, ErrorLogEvent, Level, Proto}
import org.beangle.ems.core.config.model.App
import org.beangle.ems.core.config.service.AppService
import org.beangle.ems.core.log.model.{AppLogEntry, BusinessLog, ErrorLog}
import org.beangle.ems.core.log.service.LogPersistBuffer.Worker

import java.time.Instant
import java.util as ju
import java.util.concurrent.ArrayBlockingQueue
import scala.collection.mutable

object LogPersistBuffer {
  private class Worker(store: LogPersistBuffer) extends Thread {
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

class LogPersistBuffer(entityDao: EntityDao, appService: AppService, queueSize: Int) extends Disposable {
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
    val session = entityDao.openSession()
    try {
      val logs = new mutable.ArrayBuffer[AppLogEntry]
      val iter = events.iterator()
      while (iter.hasNext) {
        val event = iter.next()
        event match {
          case be: Proto.BusinessLogEvent => findAppByName(be.getAppName) foreach (app => logs += convert(app, be))
          case ee: Proto.ErrorLogEvent => findAppByName(ee.getAppName) foreach (app => logs += convert(app, ee))
          case be: BusinessLogEvent => findAppByName(be.appName) foreach (app => logs += convert(app, be))
          case ee: ErrorLogEvent => findAppByName(ee.appName) foreach (app => logs += convert(app, ee))
        }
      }
      entityDao.saveOrUpdate(logs)
    } finally {
      entityDao.closeSession(session)
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

  private def convert(app: App, e: BusinessLogEvent): AppLogEntry = {
    val l = new BusinessLog()
    l.app = app
    l.operator = e.operator
    l.operateAt = e.operateAt
    l.summary = e.summary
    l.details = e.details
    l.resources = e.resources
    l.ip = e.ip
    l.agent = e.agent
    l.entry = e.entry
    l.logLevel = e.level
    l
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

  private def convert(app: App, e: ErrorLogEvent): AppLogEntry = {
    val l = new ErrorLog
    l.app = app
    l.stackTrace = Strings.abbreviate(e.stackTrace, 4000)
    l.exceptionName = e.exceptionName
    l.requestUrl = e.requestUrl
    l.message = Strings.abbreviate(e.message, 400)
    l.occurredAt = e.occurredAt
    l.username = e.username
    l.params = e.params.map(x => Strings.abbreviate(x, 4000))
    l
  }

  private def convert(app: App, e: Proto.ErrorLogEvent): AppLogEntry = {
    val l = new ErrorLog
    l.app = app
    l.stackTrace = Strings.abbreviate(e.getStackTrace, 4000)
    l.exceptionName = e.getExceptionName
    l.requestUrl = e.getRequestUrl
    l.message = Strings.abbreviate(e.getMessage, 400)
    l.occurredAt = Instant.ofEpochMilli(e.getOccurredAt)
    l.username = Option(e.getUsername)
    l.params = Option(Strings.abbreviate(e.getParams, 4000))
    l
  }

  override def destroy(): Unit = {
    worker.interrupt()
  }
}
