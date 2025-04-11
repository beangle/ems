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
import org.beangle.data.dao.EntityDao
import org.beangle.ems.app.Ems
import org.beangle.ems.core.oa.model.{Flow, FlowProcess, Message}
import org.beangle.ems.core.oa.service.MessageService
import org.beangle.ems.core.user.model.User
import org.beangle.template.freemarker.DefaultTemplateInterpreter

class MessageServiceImpl extends MessageService {

  var entityDao: EntityDao = _

  override def newMessage(recipient: User, flow: Flow, process: FlowProcess): Option[Message] = {
    flow.resultMessage.map { template =>
      val ctx = Collections.newMap[String, Any]
      ctx.put("flow", flow)
      ctx.put("process", process)
      ctx.put("base", Ems.base)
      val title = DefaultTemplateInterpreter.process(template.title, ctx)
      val contents = DefaultTemplateInterpreter.process(template.contents, ctx)
      val message = new Message(recipient, title, contents)
      message.sendFrom = "系统"
      if (template.delayMinutes > 0) {
        message.sentAt = message.sentAt.plusSeconds(template.delayMinutes * 60)
      }
      entityDao.saveOrUpdate(message)
      message
    }
  }
}
