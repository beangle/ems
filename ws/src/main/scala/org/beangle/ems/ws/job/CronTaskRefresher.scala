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

package org.beangle.ems.ws.job

import org.beangle.commons.bean.Scheduled
import org.beangle.commons.codec.digest.Digests
import org.beangle.commons.collection.Collections
import org.beangle.commons.io.StringBuilderWriter
import org.beangle.cron.shell.{LocalShellRunner, RemoteShellRunner}
import org.beangle.cron.{CronExpr, Scheduler}
import org.beangle.data.dao.EntityDao
import org.beangle.data.orm.AbstractDaoTask
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.job.model.{CronTask, CronTaskLog}
import org.beangle.ems.ws.WsLogger
import org.beangle.ems.ws.job.CronTaskRefresher.CronTaskRunner

import java.io.{ByteArrayInputStream, PrintWriter}
import java.time.{Duration, Instant}

class CronTaskRefresher(val expression: String) extends AbstractDaoTask, Scheduled {
  var scheduler: Scheduler = _
  var domainService: DomainService = _

  //task footprint -> taskId
  private val runningTasks = Collections.newMap[String, String]

  private def digestTaskId(t: CronTask): String = {
    Digests.md5Hex(t.id.toString + t.expression + t.command)
  }

  override def execute(): Unit = {
    val tasks = entityDao.findBy(classOf[CronTask], "domain" -> domainService.getDomain, "enabled" -> true)
    val taskMap = tasks.map(t => (digestTaskId(t), t)).toMap

    val abandons = runningTasks.keySet.toSet -- taskMap.keySet
    abandons foreach { digestId =>
      scheduler.cancel(runningTasks(digestId))
      runningTasks.remove(digestId)
      WsLogger.debug(s"remove cron task ${digestId}")
    }
    tasks foreach { t =>
      val digestId = digestTaskId(t)
      if (!runningTasks.contains(digestId)) {
        newTask(digestId, t)
        WsLogger.info(s"new cron task ${t.name} at ${t.expression}")
      }
    }
  }

  def newTask(digestId: String, t: CronTask): Unit = {
    val expr = CronExpr.parse(t.expression)
    val taskId = scheduler.schedule(expr, new CronTaskRunner(entityDao, t, None))
    runningTasks.put(digestId, taskId)
  }

}

object CronTaskRefresher {
  class CronTaskRunner(entityDao: EntityDao, task: CronTask, sshKeyPassphrase: Option[String]) extends Runnable {

    override def run(): Unit = {
      val start = Instant.now
      val runner =
        if (task.target == "localhost") {
          new LocalShellRunner()
        } else {
          new RemoteShellRunner(task.target, sshKeyPassphrase)
        }

      var rs: (Int, String) = null
      var end: Instant = null
      try {
        rs = runner.execute(task.command)
        end = Instant.now
      } catch {
        case e: Throwable =>
          val sw = new StringBuilderWriter()
          e.printStackTrace(new PrintWriter(sw))
          end = Instant.now
          rs = (-1, sw.toString)
      }

      val blob = EmsApp.getBlobRepository()
      val meta = blob.upload(s"/job/${task.id}", new ByteArrayInputStream(rs._2.getBytes), s"${System.currentTimeMillis()}.txt", "system")

      val session = entityDao.openSession()
      try {
        val nt = entityDao.get(classOf[CronTask], task.id)
        nt.statusCode = Some(rs._1)
        nt.lastExecuteAt = Some(start)
        nt.duration = Some(Duration.between(start, end))
        val log = new CronTaskLog
        log.task = nt
        log.executeAt = start
        log.duration = nt.duration.get
        log.statusCode = rs._1
        log.resultFilePath = meta.filePath
        entityDao.saveOrUpdate(nt, log)
      } finally {
        entityDao.closeSession(session)
      }
    }
  }
}
