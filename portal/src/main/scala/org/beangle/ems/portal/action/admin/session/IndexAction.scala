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

package org.beangle.ems.portal.action.admin.session

import java.sql.Timestamp

import javax.sql.DataSource
import org.beangle.commons.collection.page.SinglePage
import org.beangle.commons.collection.{Collections, Order}
import org.beangle.jdbc.query.JdbcExecutor
import org.beangle.web.action.support.ActionSupport
import org.beangle.web.action.view.View
import org.beangle.webmvc.support.helper.QueryHelper
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.user.model.Category

class IndexAction(ds: DataSource) extends ActionSupport {
  val jdbcExecutor = new JdbcExecutor(ds)

  var domainService: DomainService = _

  def index(): View = {
    var sql = "select id,name from ems.usr_categories"
    val categories = Collections.newMap[Int, Category]
    jdbcExecutor.query(sql) foreach { d =>
      val category = new Category
      category.id = d(0).asInstanceOf[Number].intValue
      category.name = d(1).asInstanceOf[String]
      categories.put(category.id, category)
    }

    sql = "select id,principal,description,ip,agent,os,login_at,last_access_at,category_id from ems.se_session_infoes s"
    sql += " where s.domain_id=" + domainService.getDomain.id
    sql += (" order by " + get(Order.OrderStr, "login_at desc"))
    val limit = QueryHelper.pageLimit
    val list = jdbcExecutor.fetch(sql, limit)
    val datas = list.map { d =>
      val info = new SessionInfo()
      info.id = d(0).asInstanceOf[String]
      info.principal = d(1).asInstanceOf[String]
      info.description = Option(d(2).asInstanceOf[String])
      info.ip = Option(d(3).asInstanceOf[String])
      info.agent = Option(d(4).asInstanceOf[String])
      info.os = Option(d(5).asInstanceOf[String])
      info.loginAt = d(6).asInstanceOf[Timestamp].toInstant
      info.lastAccessAt = d(7).asInstanceOf[Timestamp].toInstant
      val categoryId = d(8).asInstanceOf[Number].intValue
      info.category = categories.get(categoryId) match {
        case None =>
          val c = new Category()
          c.id = categoryId
          c.name = categoryId.toString
          c
        case Some(c) => c
      }
      info
    }
    val total = jdbcExecutor.queryForInt("select count(*) from ems.se_session_infoes where domain_id=" + domainService.getDomain.id)
    val page = new SinglePage[SessionInfo](limit.pageIndex, limit.pageSize, total.getOrElse(0), datas)
    put("sessionInfoes", page)
    forward()
  }

}
