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

package org.beangle.ems.ws.oa

import org.beangle.commons.json.{Json, JsonObject}
import org.beangle.commons.lang.Charsets
import org.beangle.data.dao.EntityDao
import org.beangle.data.json.JsonAPI
import org.beangle.data.json.JsonAPI.Context
import org.beangle.ems.app.oa.Flows
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.oa.model.*
import org.beangle.ems.core.oa.service.FlowService
import org.beangle.ems.core.user.model.User
import org.beangle.webmvc.annotation.{mapping, param, response}
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.support.{ActionSupport, ServletSupport}

class FlowWS(entityDao: EntityDao) extends ActionSupport, ServletSupport {
  var domainService: DomainService = _
  var flowService: FlowService = _

  @mapping("{businessCode}/{profileId}")
  @response
  def profile(businessCode: String, profileId: String): AnyRef = {
    val flows = flowService.getFlows(businessCode, profileId)
    convert(flows, true)
  }

  @mapping("{flowCode}")
  @response
  def info(@param("flowCode") flowCode: String): AnyRef = {
    convert(List(flowService.getFlow(flowCode)), true)
  }

  /** 开始一个流程
   */
  @mapping("{flowCode}/start/{businessKey}", method = "post")
  @response
  def start(flowCode: String, businessKey: String): Flows.Process = {
    val flow = flowService.getFlow(flowCode)
    val data = Json.parseObject(new String(request.getInputStream.readAllBytes(), Charsets.UTF_8))
    flowService.start(flow, businessKey, data)
  }

  @mapping("processes/${processId}/tasks/${taskId}/complete", method = "post")
  @response
  def complete(processId: String, taskId: String): Flows.Process = {
    val payload = Json.parseObject(new String(request.getInputStream.readAllBytes(), Charsets.UTF_8))
    val task = entityDao.get(classOf[FlowActiveTask], taskId.toLong)
    flowService.complete(task, Flows.Payload.fromJson(payload))
  }

  @mapping("processes/${processId}/cancel", method = "post")
  @response
  def cancel(processId: String): String = {
    val process = entityDao.get(classOf[FlowActiveProcess], processId.toLong)
    flowService.cancel(process)
    "OK"
  }

  @mapping("processes/${processId}")
  @response
  def process(processId: String): JsonObject = {
    val process = entityDao.get(classOf[FlowProcess], processId.toLong)

    given context: Context = JsonAPI.context(ActionContext.current.params)

    context.filters.include(classOf[FlowProcess], "id", "businessKey", "startAt", "endAt", "status", "tasks")
    context.filters.include(classOf[FlowTask], "id", "name", "idx", "assignee", "startAt", "endAt", "comments", "attachments", "dataJson", "status")
    context.filters.include(classOf[FlowAttachment], "name", "fileSize", "filePath")
    context.filters.include(classOf[FlowComment], "id", "messages", "updatedAt")
    context.filters.include(classOf[User], "id", "code", "name")
    JsonAPI.newJson(JsonAPI.create(process, ""))
  }

  private def convert(docs: Iterable[Flow], hasDetail: Boolean): JsonObject = {
    given context: Context = JsonAPI.context(ActionContext.current.params)

    if (hasDetail) {
      context.filters.include(classOf[Flow], "id", "name", "code", "activities", "envJson")
      context.filters.include(classOf[FlowActivity], "id", "name", "idx")
    } else {
      context.filters.include(classOf[Flow], "id", "name", "code")
    }
    val resources = docs.map { g => JsonAPI.create(g, "") }
    JsonAPI.newJson(resources)
  }
}
