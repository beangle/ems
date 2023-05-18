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

import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.oa.model.Doc
import org.beangle.ems.core.user.model.User
import org.beangle.security.Securities
import org.beangle.web.action.annotation.{mapping, param}
import org.beangle.web.action.support.{ActionSupport, ServletSupport}
import org.beangle.web.action.view.{Status, Stream, View}
import org.beangle.webmvc.support.helper.QueryHelper

import java.io.File

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
    builder.join("doc.categories", "uc")
    builder.where("uc.id=:categoryId", categoryId)
    builder.where("doc.archived=false")
    val orderBy = get("orderBy").getOrElse("doc.updatedAt desc")
    builder.orderBy(orderBy)
    builder.cacheable(true)
    builder
  }

  private def decideContentType(fileName: String): String = {
    MediaTypes.get(Strings.substringAfterLast(fileName, "."), MediaTypes.ApplicationOctetStream).toString
  }

  @mapping("{id}")
  def info(@param("id") id: String): View = {
    val doc = entityDao.get(classOf[Doc], id.toLong)
    EmsApp.getBlobRepository().path(doc.filePath) match {
      case Some(p) =>
        if p.startsWith("http") then
          response.sendRedirect(p)
          null
        else Stream(new File(p), doc.name)
      case None => Status.NotFound
    }
  }

}
