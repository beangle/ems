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

package org.beangle.ems.portal.action.user

import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.core.config.model.Business
import org.beangle.ems.core.oa.model.{DoneTodo, Todo}
import org.beangle.ems.core.oa.service.TodoService
import org.beangle.ems.core.user.service.UserService
import org.beangle.security.Securities
import org.beangle.she.webmvc.RestfulAction
import org.beangle.she.webmvc.QueryHelper
import org.beangle.webmvc.view.View

class TodoAction extends RestfulAction[Todo] {

  var userService: UserService = _

  var todoService: TodoService = _

  override def indexSetting(): Unit = {
    val user = userService.get(Securities.user)
    val query = OqlBuilder.from[Array[_]](classOf[Todo].getName, "todo")
    query.where("todo.user = :me", user)
    query.select("todo.business.id,count(*)").groupBy("todo.business.id")
    val stat = entityDao.search(query).map(x => entityDao.get(classOf[Business], x(0).asInstanceOf[Long]) -> x(1)).toMap
    put("stat", stat)

    val doneCount = entityDao.count(classOf[DoneTodo], "user" -> user)
    put("doneCount", doneCount)
  }

  def newly(): View = {
    val builder = getQueryBuilder
    builder.limit(1, 5)
    val todoes = entityDao.search(builder)
    put("todoes", todoes)
    forward()
  }

  def complete(): View = {
    val todoes = entityDao.find(classOf[Todo], getLongIds("todo"))
    todoes foreach { todo =>
      todoService.complete(todo)
    }
    val msg = if todoes.size > 1 then s"已完成${todoes.size}条代办" else "已完成"
    redirect("search", msg)
  }

  /** 已办结的任务
   *
   * @return
   */
  def done(): View = {
    val user = userService.get(Securities.user)
    val query = OqlBuilder.from(classOf[DoneTodo], "todo")
    query.where("todo.user = :me", user)
    populateConditions(query)
    QueryHelper.sort(query)
    query.tailOrder("todo.id")
    query.limit(getPageLimit)
    put("todoes", entityDao.search(query))
    forward()
  }

  override protected def getQueryBuilder: OqlBuilder[Todo] = {
    getLong("todo.business.id") foreach { businessId =>
      put("business", entityDao.get(classOf[Business], businessId))
    }
    val user = userService.get(Securities.user)
    val query = super.getQueryBuilder
    query.where("todo.user = :me", user)
    query
  }
}
