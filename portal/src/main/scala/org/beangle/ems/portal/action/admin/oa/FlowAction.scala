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
import org.beangle.commons.lang.Strings
import org.beangle.ems.core.config.model.Business
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.oa.model.{Flow, FlowActivity, MessageTemplate}
import org.beangle.ems.core.user.model.Group
import org.beangle.event.bus.{DataEvent, DataEventBus}
import org.beangle.webmvc.context.Params
import org.beangle.she.webmvc.RestfulAction
import org.beangle.webmvc.view.View

import java.time.Instant

class FlowAction extends RestfulAction[Flow] {
  var databus: DataEventBus = _

  var domainService: DomainService = _

  override def indexSetting(): Unit = {
    super.indexSetting()
    put("businesses", getBusinesses())
  }

  private def getBusinesses(): Seq[Business] = {
    entityDao.findBy(classOf[Business], "domain", domainService.getDomain)
  }

  private def getTemplates(): Seq[MessageTemplate] = {
    entityDao.findBy(classOf[MessageTemplate], "business.domain", domainService.getDomain)
  }

  override protected def editSetting(entity: Flow): Unit = {
    val groups = entityDao.getAll(classOf[Group])
    put("groups", groups)
    put("businesses", getBusinesses())
    val templates = getTemplates()
    put("todoTemplates", templates.filter(_.todo))
    put("resultTemplates", templates.filter { x => !x.todo })
    super.editSetting(entity)
  }

  override protected def saveAndRedirect(flow: Flow): View = {
    val tasks = flow.activities.map(x => (x.name, x)).toMap
    val taskNames = Collections.newSet[String]
    //max 11 steps
    (0 to 10) foreach { i =>
      val p = Params.sub(i.toString)
      val paramId = getLong(i.toString + ".id").getOrElse(0L)
      val act = if (paramId > 0) entityDao.get(classOf[FlowActivity], paramId) else new FlowActivity
      populate(act, i.toString)
      if (null != act.name) {
        taskNames += act.name
        val groups = getAll(s"${i}_group.id", classOf[Int]) map { groupId => entityDao.get(classOf[Group], groupId) }
        tasks.get(act.name) match {
          case Some(x) =>
            x.assignees = act.assignees
            x.depart = act.depart
            x.groups.clear()
            x.groups.addAll(groups)
            x.idx = act.idx
            x.guard = act.guard
            x.guardComment = act.guardComment
          case None =>
            act.flow = flow
            flow.activities += act
            act.groups.addAll(groups)
        }
      }
    }
    flow.activities.subtractAll(flow.activities.filter(x => !taskNames.contains(x.name)))
    flow.domain = domainService.getDomain
    if (Strings.isBlank(flow.guardJson)) flow.guardJson = "{}"
    if (Strings.isBlank(flow.envJson)) flow.envJson = "{}"
    if (Strings.isBlank(flow.flowJson)) flow.flowJson = "{}"

    var idx = 0
    flow.activities.sortBy(_.idx) foreach { a =>
      a.idx = idx
      idx += 1
    }
    entityDao.saveOrUpdate(flow)
    entityDao.refresh(flow)
    databus.publish(DataEvent.update(flow))
    super.saveAndRedirect(flow)
  }

  def copy(): View = {
    val flow = entityDao.get(classOf[Flow], getLongId("flow"))
    val nf = new Flow()
    nf.name = flow.name + "副本"
    nf.code = flow.code + "(copy)"

    nf.business = flow.business
    nf.domain = flow.domain
    nf.activities = flow.activities.map(x => {
      val nx = new FlowActivity()
      nx.idx = x.idx
      nx.name = x.name
      nx.flow = nf
      nx.assignees = x.assignees
      nx.depart = x.depart
      nx.groups.addAll(x.groups)
      nx
    })
    nf.flowJson = flow.flowJson
    nf.guardJson = flow.guardJson
    nf.envJson = flow.envJson
    nf.profileId = flow.profileId
    nf.updatedAt = Instant.now

    entityDao.saveOrUpdate(nf)
    redirect("search", "复制成功")
  }
}
