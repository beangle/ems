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

package org.beangle.ems.core.oa.service.impl

import org.beangle.commons.collection.Collections
import org.beangle.commons.json.{Json, JsonObject}
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.app.oa.Flows
import org.beangle.ems.app.oa.Flows.Payload
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.oa.model.*
import org.beangle.ems.core.oa.service.FlowService
import org.beangle.ems.core.user.model.User

import java.time.{Instant, LocalDate}

/** 流程服务的实现
 */
class FlowServiceImpl extends FlowService {
  var domainService: DomainService = _
  var entityDao: EntityDao = _

  override def getFlows(businessCode: String, profileId: String): Seq[Flow] = {
    val query = OqlBuilder.from(classOf[Flow], "flow")
    query.where("flow.domain=:domain", domainService.getDomain)
    query.where("flow.business.code=:businessCode", businessCode)
    query.where("flow.profileId=:profileId", profileId)
    query.cacheable()
    entityDao.search(query)
  }

  override def getFlow(code: String): Flow = {
    val query = OqlBuilder.from(classOf[Flow], "flow")
    query.where("flow.domain=:domain", domainService.getDomain)
    query.where("flow.code=:flowCode", code)
    query.cacheable()
    entityDao.search(query).head
  }

  /** 开始一个流程
   *
   * @param flow
   * @param businessKey
   * @param data
   * @return
   */
  override def start(flow: Flow, businessKey: String, env: JsonObject): Flows.Process = {
    if (flow.checkMatch(env)) {
      val ap = new FlowActiveProcess(flow, businessKey)
      entityDao.saveOrUpdate(ap)
      val p = new FlowProcess(ap, env)
      entityDao.saveOrUpdate(p)
      val at = startTask(ap, p, flow.firstActivity)

      val pt = new Flows.Task(ap.id.toString, at.name, at.startAt,
        at.assignee.map(u => Flows.User(u.code, u.name)), at.candidates.map(u => Flows.User(u.code, u.name)))
      Flows.Process(ap.id.toString, flow.code, List(pt), null)
    } else {
      Flows.Process(null, "", List.empty, "不满足流程启动条件")
    }
  }

  /** 完成一个任务
   *
   * @param activeTask
   * @param payload
   */
  override def complete(activeTask: FlowActiveTask, payload: Payload): Flows.Process = {
    val task = entityDao.get(classOf[FlowTask], activeTask.id)
    val assignee = entityDao.findBy(classOf[User], "code" -> payload.assignee.code, "org" -> domainService.getOrg).head
    task.complete(assignee, payload)
    entityDao.saveOrUpdate(task, task.process)

    entityDao.remove(activeTask)
    val tasks = Collections.newBuffer[FlowActiveTask]
    findNext(task) foreach { next =>
      val at = startTask(activeTask.process, task.process, next)
      tasks.addOne(at)
    }
    val ap = activeTask.process
    if (tasks.isEmpty) {
      Flows.Process(ap.id.toString, ap.flow.code, List.empty, null)
    } else {
      val at = tasks.head
      val pt = new Flows.Task(ap.id.toString, at.name, at.startAt,
        at.assignee.map(u => Flows.User(u.code, u.name)), at.candidates.map(u => Flows.User(u.code, u.name)))
      Flows.Process(ap.id.toString, ap.flow.code, List(pt), null)
    }
  }

  /** 取消一个流程
   *
   * @param process
   */
  override def cancel(process: FlowActiveProcess): Unit = {
    val p = entityDao.get(classOf[FlowProcess], process.id)
    p.endAt = Some(Instant.now)
    p.status = FlowStatus.Canceled
    p.tasks foreach { t =>
      if t.endAt.isEmpty then
        t.endAt = Some(Instant.now)
        t.status = FlowStatus.Canceled
    }
    entityDao.saveOrUpdate(p)
    entityDao.remove(process)
  }

  override def remove(process: FlowProcess): Unit = {
    val ap = entityDao.get(classOf[FlowActiveProcess], process.id)
    entityDao.remove(ap, process)
  }

  /** 查找下一个任务
   * FIXME 这里过于简化
   *
   * @param task
   * @return
   */
  private def findNext(task: FlowTask): Option[FlowActivity] = {
    val activities = task.process.flow.activities.sortBy(_.idx)
    activities.find(a => a.idx > task.idx)
  }

  /** 开始一个任务
   *
   * @param ap
   * @param p
   * @param activity
   * @return
   */
  private def startTask(ap: FlowActiveProcess, p: FlowProcess, activity: FlowActivity): FlowActiveTask = {
    val at = new FlowActiveTask(ap, activity)
    val env = Json.parseObject(p.envJson)

    //解析活动的受理人，候选人，部门
    activity.assignee foreach { assignee =>
      val userCode = resolveVar(env, assignee)
      if (Strings.isNotEmpty(userCode)) {
        val user = entityDao.findBy(classOf[User], "code" -> userCode, "org" -> domainService.getOrg).head
        at.assignee = Some(user)
      }
    }
    activity.candidates foreach { candidates =>
      Strings.split(candidates, ",") foreach { c =>
        val code = resolveVar(env, c)
        at.candidates.addAll(entityDao.findBy(classOf[User], "code" -> code, "org" -> domainService.getOrg))
      }
    }
    if (activity.candidates.isEmpty && activity.assignee.isEmpty && activity.groups.nonEmpty) {
      val depart = Option(resolveVar(env, activity.depart.get))
      val q = OqlBuilder.from(classOf[User], "u")
      q.join("u.groups", "g").where("g in (:groups)", activity.groups)
      depart foreach { dcode =>
        q.where("u.depart.code=:dcode", dcode)
      }
      q.where("u.org=:org", domainService.getOrg)
      q.where("u.enabled = true")
      q.where("u.endOn is null or u.endOn>:today", LocalDate.now)
      q.select("distinct u")
      q.limit(1, 50) //最多取50个
      val users = entityDao.search(q)

      at.candidates.addAll(users)
    }

    entityDao.saveOrUpdate(at)
    //create task synchronized
    val t = new FlowTask(p, at)
    entityDao.saveOrUpdate(t)
    at
  }


  private def resolveVar(data: JsonObject, exp: String): String = {
    val varname = Strings.substringBetween(exp, "{", "}")
    if (Strings.isEmpty(varname)) {
      exp
    } else {
      data.getString(varname)
    }
  }
}
