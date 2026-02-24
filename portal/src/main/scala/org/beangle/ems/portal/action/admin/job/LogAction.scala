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

import org.beangle.commons.net.http.HttpUtils
import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.job.model.{CronTask, CronTaskLog}
import org.beangle.she.webmvc.RestfulAction
import org.beangle.she.webmvc.QueryHelper
import org.beangle.webmvc.view.View

import java.time.Duration

/** 计划任务执行日志维护
 */
class LogAction extends RestfulAction[CronTaskLog] {

  var domainService: DomainService = _

  override def indexSetting(): Unit = {
    super.indexSetting()
    put("cronTasks", entityDao.findBy(classOf[CronTask], "domain", domainService.getDomain))
  }

  override def getQueryBuilder: OqlBuilder[CronTaskLog] = {
    val query = super.getQueryBuilder
    query.where("log.task.domain=:domain", domainService.getDomain)
    QueryHelper.dateBetween(query, null, "executeAt", "beginOn", "endOn")
    query.orderBy("log.executeAt desc")
    query
  }

  override protected def editSetting(log: CronTaskLog): Unit = {
    put("cronTasks", entityDao.findBy(classOf[CronTask], "domain", domainService.getDomain))
    super.editSetting(log)
  }

  override def saveAndRedirect(log: CronTaskLog): View = {
    getLong("durationMillis").filter(_ > 0).foreach { ms =>
      log.duration = Duration.ofMillis(ms)
    }
    log.resultFilePath = get("log.resultFilePath").filter(_.nonEmpty).orNull
    super.saveAndRedirect(log)
  }

  override def info(id: String): View = {
    val log = entityDao.get(classOf[CronTaskLog],id.toLong)
    if(log.resultFilePath.startsWith("/")){
      val blob = EmsApp.getBlobRepository()
      val result = HttpUtils.get(blob.uri(log.resultFilePath).toString).getText
      put("result",result.split("\n"))
    }else{
      put("result",List.empty)
    }
    super.info(id)
  }
  override protected def simpleEntityName: String = "log"
}
