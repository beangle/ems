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

package org.beangle.ems.portal.action.admin.oa

import org.beangle.commons.lang.Strings
import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.oa.model.{FlowActiveProcess, FlowActivity, FlowProcess}
import org.beangle.ems.core.oa.service.FlowService
import org.beangle.webmvc.support.action.RestfulAction
import org.beangle.webmvc.view.View

class FlowActiveProcessAction extends RestfulAction[FlowActiveProcess] {

  var domainService: DomainService = _

  var flowService: FlowService = _

  override protected def getQueryBuilder: OqlBuilder[FlowActiveProcess] = {
    val query = super.getQueryBuilder
    query.where("process.flow.domain=:domain", domainService.getDomain)
    get("initiator") foreach { i =>
      if (Strings.isNotBlank(i)) {
        query.where("process.initiator.code like :initiator or process.initiator.name like :initiator", s"%$i%")
      }
    }
    query
  }

  /** 设置回退
   *
   * @return
   */
  def backForm(): View = {
    val processId = getLongId("process")
    val ap = entityDao.get(classOf[FlowActiveProcess], processId)
    val p = entityDao.get(classOf[FlowProcess], processId)
    put("process", ap)
    val passedTasks = p.tasks.map(_.name).toSet
    val activities = p.flow.activities.filter(x => passedTasks.contains(x.name)).sortBy(_.idx)
    put("activities", activities)
    forward()
  }

  def back(): View = {
    val processId = getLongId("process")
    val activity = entityDao.get(classOf[FlowActivity], getLongId("activity"))
    flowService.back(entityDao.get(classOf[FlowActiveProcess], processId), activity)
    redirect("search", "设置成功")
  }

  override protected def simpleEntityName: String = {
    "process"
  }

}
