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

import org.beangle.commons.collection.Collections
import org.beangle.ems.core.config.model.Business
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.oa.model.{Flow, FlowActivity}
import org.beangle.ems.core.user.model.Group
import org.beangle.event.bus.{DataEvent, DataEventBus}
import org.beangle.webmvc.context.Params
import org.beangle.webmvc.support.action.RestfulAction
import org.beangle.webmvc.view.View

class FlowAction extends RestfulAction[Flow] {
  var databus: DataEventBus = _

  var domainService: DomainService = _

  override def indexSetting(): Unit = {
    super.indexSetting()
    put("businesses", entityDao.getAll(classOf[Business]))
  }

  override protected def editSetting(entity: Flow): Unit = {
    val groups = entityDao.getAll(classOf[Group])
    put("groups", groups)
    put("businesses", entityDao.getAll(classOf[Business]))
    super.editSetting(entity)
  }

  override protected def saveAndRedirect(flow: Flow): View = {
    //flow.dataJson = new JsonValue(get("flow.dataJson", ""))
    val tasks = flow.activities.map(x => (x.name, x)).toMap
    val taskNames = Collections.newSet[String]
    (0 to 10) foreach { i =>
      val p = Params.sub(i.toString)
      val paramId = getLong(i.toString + ".id").getOrElse(0L)
      val task = if (paramId > 0) entityDao.get(classOf[FlowActivity], paramId) else new FlowActivity
      populate(task, i.toString)
      if (null != task.name) {
        taskNames += task.name
        val group = getInt(s"${i}groupId") map { groupId => entityDao.get(classOf[Group], groupId) }
        tasks.get(task.name) match {
          case Some(x) =>
            x.idx = i
            x.groups.addAll(group)
          case None =>
            task.flow = flow
            task.idx = i
            flow.activities += task
            task.groups.addAll(group)
        }
      }
    }
    flow.activities.subtractAll(flow.activities.filter(x => !taskNames.contains(x.name)))
    flow.domain = domainService.getDomain
    entityDao.saveOrUpdate(flow)
    entityDao.refresh(flow)
    databus.publish(DataEvent.update(flow))
    super.saveAndRedirect(flow)
  }
}
