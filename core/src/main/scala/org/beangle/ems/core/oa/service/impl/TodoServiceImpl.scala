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
import org.beangle.ems.core.config.model.Business
import org.beangle.ems.core.oa.model.*
import org.beangle.ems.core.oa.service.TodoService
import org.beangle.ems.core.user.model.User
import org.beangle.template.api.TemplateInterpreter
import org.beangle.template.freemarker.{DefaultTemplateEngine, DefaultTemplateInterpreter}

class TodoServiceImpl extends TodoService {

  var entityDao: EntityDao = _

  override def complete(todo: Todo): DoneTodo = {
    val done = new DoneTodo(todo)
    entityDao.saveOrUpdate(done)
    entityDao.remove(todo)
    done
  }

  override def complete(user: User, business: Business, businessKey: String): Int = {
    val todoes = entityDao.findBy(classOf[Todo], "user" -> user, "business" -> business, "businessKey" -> businessKey)
    todoes foreach { todo =>
      complete(todo)
    }
    todoes.size
  }

  override def newTodo(user: User, task: FlowTask, flow: Flow, process: FlowProcess): Option[Todo] = {
    flow.todoMessage map { template =>
      val ctx = Collections.newMap[String, Any]
      ctx.put("flow", flow)
      ctx.put("process", process)
      ctx.put("base", Ems.base)
      ctx.put("task", task)
      val title = DefaultTemplateInterpreter.process(template.title, ctx)
      val contents = DefaultTemplateInterpreter.process(template.contents, ctx)
      //    val title = s"${flow.name}申请${task.name}"
      //    val contents = s"${process.initiator.get.name}发起的${flow.name}申请,需要您处理。"
      val todo = new Todo(user, title, contents, flow.business, process.businessKey)
      if (template.delayMinutes > 0) {
        todo.updatedAt = todo.updatedAt.plusSeconds(template.delayMinutes * 60)
      }
      todo.url = DefaultTemplateInterpreter.process(flow.formUrl, ctx)
      entityDao.saveOrUpdate(todo)
      todo
    }
  }

}
