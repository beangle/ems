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

import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.security.Securities
import org.beangle.web.action.support.{ActionSupport, EntitySupport, ParamSupport}
import org.beangle.web.action.annotation.{mapping, param}
import org.beangle.web.action.context.Params
import org.beangle.web.action.view.View
import org.beangle.webmvc.support.helper.QueryHelper
import org.beangle.ems.core.bulletin.model.{Notice, NoticeStatus}
import org.beangle.ems.core.user.model.User

class NoticeAction extends ActionSupport {

  var entityDao: EntityDao = _

  def index(): View = {
    val me: User = entityDao.findBy(classOf[User], "code", List(Securities.user)).head
    val query = getOqlBuilder(me.category.id)
    query.limit(QueryHelper.pageLimit)
    put("notices", entityDao.search(query))
    forward()
  }

  @mapping("pannel/{category}")
  def pannel(@param("category") category: String): View = {
    val query = getOqlBuilder(category.toInt)
    put("notices", entityDao.search(query.limit(1, 10)))
    forward()
  }

  private def getOqlBuilder(categoryId: Int): OqlBuilder[Notice] = {
    val query = OqlBuilder.from(classOf[Notice], "notice")
    query.where("notice.archived=false")
    query.where("notice.status=:status", NoticeStatus.Passed)
    query.join("notice.userCategories", "uc")
    query.where("uc.id=:category", categoryId)
    query.orderBy("notice.publishedAt desc")
    query.cacheable(true)
    query
  }

  @mapping("{id}")
  def info(@param("id") id: String): View = {
    val notice = entityDao.get(classOf[Notice], id.toLong)
    put("notice", notice)
    forward()
  }
}
