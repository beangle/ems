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
import org.beangle.ems.core.job.model.CronTask
import org.beangle.she.webmvc.RestfulAction
import org.beangle.webmvc.view.View

/** 计划任务维护
 */
class TaskAction extends RestfulAction[CronTask] {

  var domainService: DomainService = _

  override def getQueryBuilder: OqlBuilder[CronTask] = {
    val query = super.getQueryBuilder
    query.where("task.domain=:domain", domainService.getDomain)
    query
  }

  override protected def editSetting(cronTask: CronTask): Unit = {
    if (!cronTask.persisted) {
      cronTask.enabled = true
    }
    super.editSetting(cronTask)
  }

  override def saveAndRedirect(cronTask: CronTask): View = {
    cronTask.domain = domainService.getDomain
    super.saveAndRedirect(cronTask)
  }

  override protected def simpleEntityName: String = "task"
}
