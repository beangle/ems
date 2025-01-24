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

package org.beangle.ems.core.oa.service

import org.beangle.commons.json.JsonObject
import org.beangle.ems.app.oa.Flows
import org.beangle.ems.app.oa.Flows.Payload
import org.beangle.ems.core.oa.model.{Flow, FlowActiveProcess, FlowActiveTask, FlowProcess}

trait FlowService {

  def getFlows(businessCode: String, profileId: String): Seq[Flow]

  def getFlow(code: String): Flow

  def start(flow: Flow, businessKey: String, data: JsonObject): Flows.Process

  def complete(task: FlowActiveTask, payload: Payload): Flows.Process

  def cancel(process: FlowActiveProcess): Unit

  def remove(process: FlowProcess): Unit
}
