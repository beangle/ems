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

import org.beangle.commons.json.{Json, JsonObject}
import org.beangle.commons.net.Networks
import org.beangle.commons.net.http.HttpUtils.getText
import org.beangle.commons.net.http.{HttpUtils, Response}
import org.beangle.data.json.JsonAPI
import org.beangle.ems.app.Ems
import org.beangle.serializer.json.JsonSerializer

import java.time.Instant

object Flows {

  case class Flow(code: String, name: String, env: JsonObject, activities: Seq[Activity])

  case class Activity(idx: Int, name: String)

  case class User(code: String, name: String)

  case class Group(code: String, name: String)

  case class Task(id: String, name: String, startAt: Instant, assignee: Option[User], candidates: Iterable[User],
                  comments: Iterable[Comment],
                  attachments: Iterable[Attachment], data: JsonObject) {
    def allCandidates: Seq[User] = {
      assignee.toBuffer.addAll(candidates).toSeq
    }

    def this(id: String, name: String, startAt: Instant, assignee: Option[User], candidates: Iterable[User]) = {
      this(id, name, startAt, assignee, candidates, Seq.empty, Seq.empty, new JsonObject)
    }
  }

  case class Process(id: String, flowCode: String, tasks: Iterable[Task], remark: String)

  case class Comment(messages: String, updatedAt: Instant)

  case class Attachment(name: String, fileSize: Long, var filePath: String)

  object Payload {
    def fromJson(json: JsonObject): Payload = {
      val u = json.getObject("assignee")
      val assignee = User(u.getString("code"), u.getString("name"))
      val comments = json.getArray("comments").map(_.toString).toSeq
      val attachments = json.getArray("attachments").map {
        case f: JsonObject => Attachment(f.getString("name"), f.getLong("fileSize"), f.getString("filePath"))
      }.toSeq
      val env = json.getObject("env")
      val data = json.getObject("data")
      val complete = json.getBoolean("complete", false)
      Payload(assignee, comments, attachments, env, data, complete)
    }
  }

  case class Payload(assignee: User, comments: Seq[String], attachments: Seq[Attachment], env: JsonObject, data: JsonObject, complete: Boolean) {
    def toJson: String = {
      JsonSerializer.Default.serialize(this)
    }
  }

  def getFlows(businessCode: String, profileId: Any): Iterable[Flow] = {
    val url = Ems.api + s"/platform/oa/flows/${businessCode}/${profileId}.json"
    convertFlow(getText(url).getOrElse(null))
  }

  def getFlow(flowCode: String): Flow = {
    val url = Ems.api + s"/platform/oa/flows/${flowCode}.json"
    convertFlow(getText(url).getOrElse(null)).head
  }

  def getProcess(processId: String): Process = {
    val url = Ems.api + s"/platform/oa/flows/processes/${processId}.json"
    convertProcess(getText(url))
  }

  def cancel(processId: String): Unit = {
    val url = Ems.api + s"/platform/oa/flows/processes/${processId}/cancel.json"
    HttpUtils.invoke(Networks.url(url), "", "application/json")
  }

  def start(flowCode: String, businessKey: Any, ctx: JsonObject): Process = {
    val url = Ems.api + s"/platform/oa/flows/${flowCode}/start/${businessKey}.json"
    val res = HttpUtils.invoke(Networks.url(url), ctx.toJson, "application/json")
    convertProcess(res)
  }

  def complete(processId: String, taskId: String, payload: Payload): Process = {
    val url = Ems.api + s"/platform/oa/flows/processes/${processId}/tasks/${taskId}/complete.json"
    val res = HttpUtils.invoke(Networks.url(url), payload.toJson, "application/json")
    convertProcess(res)
  }

  private def convertProcess(res: Response): Process = {
    if (res.isOk) {
      val jo = Json.parseObject(res.getText)
      val tasks = jo.getArray("tasks").map {
        case jo: JsonObject =>
          val id = jo.getString("id")
          val name = jo.getString("name")
          val startAt = jo.getInstant("startAt")
          var assignee: Option[User] = None
          if (jo.contains("assignee")) {
            val a = jo.getObject("assignee")
            assignee = Some(User(a.getString("code"), a.getString("name")))
          }
          val candidates = jo.getArray("candidates").map {
            case o: JsonObject => User(o.getString("code"), o.getString("name"))
          }
          val comments = jo.getArray("comments").map {
            case o: JsonObject => Comment(o.getString("messages"), o.getInstant("updatedAt"))
          }
          val attachments = jo.getArray("attachments").map {
            case o: JsonObject => Attachment(o.getString("name"), o.getLong("fileSize"), o.getString("filePath"))
          }
          val data = Json.parseObject(jo.getString("data", "{}"))
          //FIXME attachments,comments,data
          Task(id, name, startAt, assignee, candidates, comments, attachments, data)
      }.toSeq.sortBy(_.startAt)

      Process(jo.getString("processId"), jo.getString("flowCode"), tasks, null)
    } else {
      Process("", "", List(), res.getText)
    }
  }

  protected[oa] def convertFlow(content: String): collection.Seq[Flow] = {
    val r = JsonAPI.parse(content)
    r.map { f =>
      val activities = f.getArray("activities").map {
        case jo: JsonObject => Activity(jo.getInt("idx"), jo.getString("name"))
      }.toSeq.sortBy(_.idx)

      Flow(f.getString("code"), f.getString("name"), Json.parseObject(f.getString("envJson", "{}")), activities)
    }
  }

}
