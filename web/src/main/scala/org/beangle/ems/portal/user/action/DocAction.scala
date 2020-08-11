/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2020, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.ems.portal.user.action

import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.security.Securities
import org.beangle.webmvc.api.action.{ActionSupport, ServletSupport}
import org.beangle.webmvc.api.annotation.{mapping, param}
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.entity.helper.QueryHelper
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.bulletin.model.Doc
import org.beangle.ems.core.user.model.User

class DocAction extends ActionSupport with ServletSupport {

  var entityDao: EntityDao = _

  def index(): View = {
    val me: User = entityDao.findBy(classOf[User], "code", List(Securities.user)).head
    val query = getOqlBuilder(me.category.id)
    query.limit(QueryHelper.pageLimit)
    val docs = entityDao.search(query)
    put("docs", docs)
    forward()
  }

  def getOqlBuilder(categoryId: Int): OqlBuilder[Doc] = {
    val builder = OqlBuilder.from(classOf[Doc], "doc")
    builder.join("doc.userCategories", "uc")
    builder.where("uc.id=:categoryId", categoryId)
    val orderBy = get("orderBy").getOrElse("doc.updatedAt desc")
    builder.orderBy(orderBy)
    builder.cacheable(true)
    builder
  }

  @mapping("pannel/{category}")
  def pannel(@param("category") category: String): View = {
    val query = getOqlBuilder(category.toInt)
    put("docs", entityDao.search(query.limit(1, 10)))
    forward()
  }

  private def decideContentType(fileName: String): String = {
    MediaTypes.get(Strings.substringAfterLast(fileName, "."), MediaTypes.ApplicationOctetStream).toString
  }

  @mapping("{id}")
  def info(@param("id") id: String): View = {
    val doc = entityDao.get(classOf[Doc], id.toLong)
    EmsApp.getBlobRepository(true).path(doc.path) match {
      case Some(p) => response.sendRedirect(p)
      case None => response.setStatus(404)
    }
    null
  }

}
