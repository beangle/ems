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

package org.beangle.ems.portal.action

import org.beangle.commons.collection.Collections
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.app.Ems
import org.beangle.ems.app.web.NavContext
import org.beangle.ems.core.config.model.Portalet
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.oa.model.{Doc, Notice, NoticeStatus}
import org.beangle.ems.core.user.model.User
import org.beangle.security.Securities
import org.beangle.web.action.annotation.mapping
import org.beangle.web.action.context.ActionContext
import org.beangle.web.action.support.{ActionSupport, ServletSupport}
import org.beangle.web.action.view.View

import scala.collection.mutable

class IndexAction extends ActionSupport with ServletSupport {
  var entityDao: EntityDao = _

  var domainService: DomainService = _

  @mapping("")
  def index(): View = {
    val ctx = NavContext.get(request)
    put("nav", ctx)
    put("locale", ActionContext.current.locale)
    forward()
  }

  def noticePortalet(): View = {
    val me: User = entityDao.findBy(classOf[User], "code", List(Securities.user)).head
    val noticeQuery = OqlBuilder.from(classOf[Notice], "notice")
    noticeQuery.join("notice.userCategories", "uc")
    noticeQuery.where("uc.id=:category", me.category.id)
    noticeQuery.where("notice.archived=false and notice.status=:status", NoticeStatus.Passed)
    noticeQuery.where("notice.app.domain=:domain", domainService.getDomain)
    noticeQuery.limit(1, 10)
    noticeQuery.orderBy("notice.publishedAt desc")
    val notices = entityDao.search(noticeQuery)
    put("notices", notices)
    put("user", me)
    put("webappBase", Ems.portal)
    forward()
  }

  def docPortalet(): View = {
    val me: User = entityDao.findBy(classOf[User], "code", List(Securities.user)).head

    val docQuery = OqlBuilder.from(classOf[Doc], "doc")
    docQuery.join("doc.userCategories", "uc")
    docQuery.where("uc.id=:category", me.category.id)
    docQuery.where("doc.archived=false")
    docQuery.where("doc.app.domain=:domain", domainService.getDomain)
    docQuery.limit(1, 10)
    docQuery.orderBy("doc.updatedAt desc")
    val docs = entityDao.search(docQuery)

    put("docs", docs)
    put("user", me)
    put("webappBase", Ems.portal)
    forward()
  }

  def welcome(): View = {
    val me: User = entityDao.findBy(classOf[User], "code", List(Securities.user)).head
    val query = OqlBuilder.from(classOf[Portalet], "p")
    query.where(":category in elements(p.categories)", me.category)
    query.orderBy("p.idx")
    query.cacheable()
    val portalets = entityDao.search(query)

    val rows = portalets.groupBy(p => p.rowIndex)
    val rowPortalets = Collections.newMap[Int, collection.Seq[collection.Seq[Portalet]]]
    rows.foreach { case (i, ps) =>
      val cols = ps.groupBy(_.colspan)
      if (cols.size == 1) {
        val divided = 12 / cols.head._1
        val col1 = cols.head._2.toList.sortBy(_.idx)
        rowPortalets.put(i, split(col1, divided))
      } else {
        rowPortalets.put(i, cols.values.toSeq.sortBy(x => 0 - x.head.colspan).map(_.sortBy(_.idx)))
      }
    }
    put("rowPortalets", rowPortalets)
    put("user", me)
    forward()
  }

  private def split[T](list: List[T], count: Int): collection.Seq[collection.Seq[T]] = {
    val subLists = new mutable.ArrayBuffer[mutable.Buffer[T]]
    (0 until count) foreach (i => subLists.addOne(new mutable.ArrayBuffer[T]))
    var idx = 0
    list foreach { l =>
      val i = idx % count
      idx += 1
      subLists(i).addOne(l)
    }
    subLists
  }

  def logout(): View = {
    redirect(to(Ems.cas + "/logout"), null)
  }
}
