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
import org.beangle.ems.core.job.CronJob
import org.beangle.she.webmvc.RestfulAction
import org.beangle.webmvc.view.View

/** 计划任务维护
 */
class CronJobAction extends RestfulAction[CronJob] {

  var domainService: DomainService = _

  override def getQueryBuilder: OqlBuilder[CronJob] = {
    val query = super.getQueryBuilder
    query.where("cronJob.domain=:domain", domainService.getDomain)
    query
  }

  override protected def editSetting(cronJob: CronJob): Unit = {
    if (!cronJob.persisted) {
      cronJob.enabled = true
    }
    super.editSetting(cronJob)
  }

  override def saveAndRedirect(cronJob: CronJob): View = {
    cronJob.domain = domainService.getDomain
    super.saveAndRedirect(cronJob)
  }

  override protected def simpleEntityName: String = "cronJob"
}
