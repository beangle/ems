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

package org.beangle.ems.app.oa

import org.beangle.commons.json.JsonObject
import org.beangle.commons.net.http.HttpUtils.getText
import org.beangle.data.json.JsonAPI
import org.beangle.ems.app.Ems

object Flows {

  case class Flow(code: String, name: String, tasks: Seq[Task])

  case class Task(idx: Int, name: String, groupName: String)

  def getFlows(businessCode: String): Iterable[Flow] = {
    val url = Ems.api + "/platform/oa/flows/business/" + businessCode + ".json"
    convert(getText(url).getOrElse(null))
  }

  def convert(content: String): collection.Seq[Flow] = {
    val r = JsonAPI.parse(content)
    r.map { f =>
      val tasks = f.getArray("tasks").map {
        case jo: JsonObject => Task(jo.getInt("idx"), jo.getString("name"), jo.query("group.name").get.toString)
      }.toSeq.sortBy(_.idx)
      Flow(f.getString("code"), f.getString("name"), tasks)
    }
  }

  def getFlow(flowCode: String): Flow = {
    val url = Ems.api + "/platform/oa/flows/" + flowCode + ".json"
    convert(getText(url).getOrElse(null)).head
  }
}
