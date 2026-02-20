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

package org.beangle.ems.portal.action.admin.job

import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.job.{CronJob, CronJobLog}
import org.beangle.she.webmvc.RestfulAction
import org.beangle.she.webmvc.QueryHelper
import org.beangle.webmvc.view.View

import java.time.Duration

/** 计划任务执行日志维护
 */
class CronJobLogAction extends RestfulAction[CronJobLog] {

  var domainService: DomainService = _

  override def indexSetting(): Unit = {
    super.indexSetting()
    put("cronJobs", entityDao.findBy(classOf[CronJob], "domain", domainService.getDomain))
  }

  override def getQueryBuilder: OqlBuilder[CronJobLog] = {
    val query = super.getQueryBuilder
    query.where("cronJobLog.job.domain=:domain", domainService.getDomain)
    QueryHelper.dateBetween(query, null, "executeAt", "beginOn", "endOn")
    query.orderBy("cronJobLog.executeAt desc")
    query
  }

  override protected def editSetting(log: CronJobLog): Unit = {
    put("cronJobs", entityDao.findBy(classOf[CronJob], "domain", domainService.getDomain))
    super.editSetting(log)
  }

  override def saveAndRedirect(log: CronJobLog): View = {
    getLong("durationMillis").filter(_ > 0).foreach { ms =>
      log.duration = Some(Duration.ofMillis(ms))
    }
    log.resultFilePath = get("cronJobLog.resultFilePath").filter(_.nonEmpty)
    super.saveAndRedirect(log)
  }

  override protected def simpleEntityName: String = "cronJobLog"
}
