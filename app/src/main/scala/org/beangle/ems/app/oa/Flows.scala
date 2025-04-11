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

import jakarta.servlet.http.Part
import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.codec.binary.Base64
import org.beangle.commons.collection.Collections
import org.beangle.commons.io.IOs
import org.beangle.commons.json.{Json, JsonObject}
import org.beangle.commons.lang.Strings
import org.beangle.commons.net.Networks
import org.beangle.commons.net.http.HttpUtils.getText
import org.beangle.commons.net.http.{HttpUtils, Response}
import org.beangle.data.json.JsonAPI
import org.beangle.data.model.Entity
import org.beangle.ems.app.{Ems, EmsApp}
import org.beangle.serializer.json.JsonSerializer

import java.io.{ByteArrayInputStream, InputStream}
import java.time.Instant

object Flows {

  case class Flow(code: String, name: String, env: JsonObject, activities: Seq[Activity])

  case class Activity(idx: Int, name: String, guard: Option[String])

  case class User(code: String, name: String)

  case class Group(code: String, name: String)

  case class Task(id: String, name: String, startAt: Instant, endAt: Option[Instant], assignee: Option[User], assignees: Option[String],
                  comments: Iterable[Comment],
                  attachments: Iterable[Attachment], data: JsonObject) {

    def this(id: String, name: String, startAt: Instant, assignee: Option[User], assignees: Option[String]) = {
      this(id, name, startAt, None, assignee, assignees, Seq.empty, Seq.empty, new JsonObject)
    }
  }

  case class Process(id: String, flowCode: String, tasks: Iterable[Task], remark: String) extends Entity[String] {
    def activeTasks: Iterable[Task] = {
      val ordered = tasks.toSeq.sortBy(_.startAt)
      val uncomplete = ordered.filter(_.endAt.isEmpty)
      if (uncomplete.isEmpty) {
        uncomplete
      } else {
        ordered.lastOption
      }
    }
  }

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
      Payload(assignee, comments, attachments, env, data)
    }
  }

  case class Payload(assignee: User, comments: Iterable[String], attachments: Seq[Attachment], env: JsonObject, data: JsonObject) {
    def toJson: String = {
      JsonSerializer.Default.serialize(this)
    }
  }

  def payload(assignee: User, comments: Option[String], data: JsonObject): Payload = {
    Payload(assignee, comments, List.empty, new JsonObject, data)
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

  def user(code: String, name: String): User = {
    User(code, name)
  }

  /** 上传附件
   *
   * @param parts
   * @param dir
   * @param businessKey
   * @param owner
   * @return
   */
  def upload(parts: Iterable[Part], dir: String, businessKey: Any, owner: User): Seq[Attachment] = {
    val blob = EmsApp.getBlobRepository(true)
    val files = Collections.newBuffer[Flows.Attachment]

    parts foreach { part =>
      val f = blob.upload(s"${dir}/${businessKey}/files/",
        part.getInputStream, part.getSubmittedFileName, owner.code + " " + owner.name)
      files.addOne(Flows.Attachment(part.getSubmittedFileName, f.fileSize, f.filePath))
    }
    files.toSeq
  }

  /** 上传签名
   *
   * @param signature
   * @param dir
   * @param businessKey
   * @param owner
   * @param data
   * @param storePath
   */
  def uploadSignature(signature: Option[String], dir: String, businessKey: Any, owner: User, data: JsonObject,
                      storePath: String = "signaturePath"): Unit = {
    val blob = EmsApp.getBlobRepository(true)
    val signs = Collections.newBuffer[String]
    signature foreach { signature =>
      if (Strings.isNotBlank(signature)) {
        val bytes = Base64.decode(Strings.substringAfter(signature, ";base64,"))
        val sign = blob.upload(s"${dir}/${businessKey}/signatures/",
          new ByteArrayInputStream(bytes), owner.code + ".png", owner.code + " " + owner.name)
        data.add(storePath, sign.filePath)
      }
    }
  }

  def readSignature(path: String): String = {
    val blob = EmsApp.getBlobRepository(true)
    try {
      toBase64(blob.url(path).get.openStream(), path)
    } catch
      case e: Exception => null
  }

  private def toBase64(is: InputStream, fileName: String): String = {
    val contentType = MediaTypes.get(Strings.substringAfterLast(fileName, ".")).map(_.toString).getOrElse("image/png")
    val data = Base64.encode(IOs.readBytes(is))
    s"data:${contentType};base64,${data}"
  }

  private def convertProcess(res: Response): Process = {
    if (res.isOk) {
      val r = JsonAPI.parse(res.getText)
      if (r.resources.isEmpty) {
        Process("", "", List(), res.getText)
      } else {
        val jo = r.resource
        val tasks = jo.getArray("tasks").map {
          case jo: JsonObject =>
            val id = jo.getString("id")
            val name = jo.getString("name")
            val startAt = jo.getInstant("startAt")
            val endAt = Option(jo.getInstant("endAt"))
            var assignee: Option[User] = None
            if (jo.contains("assignee")) {
              val a = jo.getObject("assignee")
              assignee = Some(User(a.getString("code"), a.getString("name")))
            }
            val assignees = Option(jo.getString("assignees", null))
            val comments = jo.getArray("comments").map {
              case o: JsonObject => Comment(o.getString("messages"), o.getInstant("updatedAt"))
            }
            val attachments = jo.getArray("attachments").map {
              case o: JsonObject => Attachment(o.getString("name"), o.getLong("fileSize"), o.getString("filePath"))
            }
            val data = Json.parseObject(jo.getString("dataJson", "{}"))
            Task(id, name, startAt, endAt, assignee, assignees, comments, attachments, data)
        }.toSeq.sortBy(_.startAt)
        Process(jo.getString("id"), jo.query("flow.code").getOrElse("").asInstanceOf[String], tasks, null)
      }
    } else {
      Process("", "", List(), res.getText)
    }
  }

  protected[oa] def convertFlow(content: String): collection.Seq[Flow] = {
    val r = JsonAPI.parse(content).resources
    r.map { f =>
      val activities = f.getArray("activities").map {
        case jo: JsonObject => Activity(jo.getInt("idx"), jo.getString("name"), jo.get("guardComment").map(_.toString))
      }.toSeq.sortBy(_.idx)

      Flow(f.getString("code"), f.getString("name"), Json.parseObject(f.getString("envJson", "{}")), activities)
    }
  }

}
