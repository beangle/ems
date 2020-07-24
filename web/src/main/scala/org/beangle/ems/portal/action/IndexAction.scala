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
package org.beangle.ems.portal.action

import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.app.Ems
import org.beangle.ems.app.web.NavContext
import org.beangle.security.Securities
import org.beangle.webmvc.api.action.{ActionSupport, ServletSupport}
import org.beangle.webmvc.api.view.View
import org.beangle.ems.core.bulletin.model.{Doc, Notice}
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.user.model.User

class IndexAction extends ActionSupport with ServletSupport {
  var entityDao: EntityDao = _

  var domainService: DomainService = _

  def index(): View = {
    val ctx = NavContext.get(request)
    put("nav", ctx)

    put("domain", domainService.getDomain)
    put("ems", Ems)
    forward()
  }

  def welcome(): View = {
    val me: User = entityDao.findBy(classOf[User], "code", List(Securities.user)).head
    val noticeQuery = OqlBuilder.from(classOf[Notice], "notice")
    noticeQuery.join("notice.userCategories", "uc")
    noticeQuery.where("uc.id=:category", me.category.id)
    noticeQuery.where("notice.archived=false")
    noticeQuery.where("notice.app.domain=:domain", domainService.getDomain)
    noticeQuery.limit(1, 10)
    noticeQuery.orderBy("notice.publishedAt desc")
    val notices = entityDao.search(noticeQuery)

    val docQuery = OqlBuilder.from(classOf[Doc], "doc")
    docQuery.join("doc.userCategories", "uc")
    docQuery.where("uc.id=:category", me.category.id)
    docQuery.where("doc.archived=false")
    docQuery.where("doc.app.domain=:domain", domainService.getDomain)
    docQuery.limit(1, 10)
    docQuery.orderBy("doc.updatedAt desc")
    val docs = entityDao.search(docQuery)

    put("notices", notices)
    put("docs", docs)
    put("user", me)
    put("webappBase", Ems.webapp)
    forward()
  }

  def logout(): View = {
    redirect(to(Ems.cas + "/logout"), null)
  }
}
