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

package org.beangle.ems.portal.user.action

import org.beangle.commons.codec.digest.Digests
import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.app.Ems
import org.beangle.ems.core.oa.model.Message
import org.beangle.ems.core.user.model.User
import org.beangle.security.Securities
import org.beangle.web.action.annotation.{ignore, mapping, param}
import org.beangle.web.action.view.View
import org.beangle.webmvc.support.action.RestfulAction

import java.time.Instant

class MessageAction extends RestfulAction[Message] {

  override protected def indexSetting(): Unit = {
    val me = Securities.user
    val query = OqlBuilder.from[Array[_]](classOf[Message].getName, "msg")
    query.where("msg.recipient.code=:me", me)
    query.select("msg.status,count(*)").groupBy("msg.status")
    val rs = entityDao.search(query).map(x => x(0).toString -> x(1)).toMap
    put("stats", rs)
  }

  @mapping(value = "{id}")
  override def info(@param("id") id: String): View = {
    val msg: Message = getModel(id.toLong)
    val me = Securities.user
    if (msg.status == Message.Newly && msg.recipient.code == me) {
      msg.status = Message.Readed
      entityDao.saveOrUpdate(msg)
    }
    put("isMe", msg.recipient.code == me)
    put(simpleEntityName, msg)
    forward()
  }

  def sentList(): View = {
    val builder = OqlBuilder.from(classOf[Message], "message")
    builder.where("message.sender.code=:me", Securities.user)
    populateConditions(builder)
    builder.limit(getPageLimit)
    builder.orderBy("message.sentAt desc")
    put("messages", entityDao.search(builder))
    forward()
  }

  def newly(): View = {
    val builder = getQueryBuilder
    builder.limit(1, 5)
    builder.where("message.status=" + Message.Newly)
    val messages = entityDao.search(builder)
    put("messages", messages)
    val avatarUrls = messages map { m =>
      (m.sender.code, Ems.api + "/platform/user/avatars/" + Digests.md5Hex(m.sender.code))
    }
    put("avatarUrls", avatarUrls.toMap)
    forward()
  }

  override protected def getQueryBuilder: OqlBuilder[Message] = {
    val builder = super.getQueryBuilder
    builder.where("message.recipient.code = :me", Securities.user)
    builder
  }

  @ignore
  override protected def saveAndRedirect(msg: Message): View = {
    get("recipient.code") foreach { recipientCode =>
      val users = entityDao.findBy(classOf[User], "code", List(recipientCode))
      if (users.size == 1) {
        msg.recipient = users.head
      }
    }

    if (msg.recipient != null) {
      val users = entityDao.findBy(classOf[User], "code", List(Securities.user))
      if (users.size == 1) {
        msg.sender = users.head
      }
      msg.sentAt = Instant.now
    }
    msg.status = Message.Newly
    saveOrUpdate(msg)
    redirect("sentList", "info.save.success")
  }

  def moveToTrash(): View = {
    val ids = longIds("message")
    val msgs = entityDao.find(classOf[Message], ids)
    val me = Securities.user
    msgs foreach { msg =>
      if (msg.recipient.code == me) {
        msg.status = Message.InTrash
      }
    }
    entityDao.saveOrUpdate(msgs)
    redirect("search", "info.save.success")
  }
}
