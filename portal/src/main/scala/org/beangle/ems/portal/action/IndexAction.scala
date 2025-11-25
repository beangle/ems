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
import org.beangle.web.servlet.url.UrlBuilder
import org.beangle.webmvc.annotation.mapping
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.support.{ActionSupport, ServletSupport}
import org.beangle.webmvc.view.{Status, View}

import java.time.LocalDate
import scala.collection.mutable

class IndexAction extends ActionSupport, ServletSupport {
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
    val me = entityDao.findBy(classOf[User], "code", List(Securities.user)).head
    put("notices", topNotices(me.category.id))
    forward()
  }

  def docPortalet(): View = {
    val me = entityDao.findBy(classOf[User], "code", List(Securities.user)).head
    put("docs", topDocs(me.category.id))
    forward()
  }

  def appNotice(): View = {
    val me = entityDao.findBy(classOf[User], "code", List(Securities.user)).head
    put("docs", topDocs(me.category.id))
    put("notices", topNotices(me.category.id))
    put("user", me)
    forward()
  }

  def welcome(): View = {
    val me = entityDao.findBy(classOf[User], "code", List(Securities.user)).head
    val query = OqlBuilder.from(classOf[Portalet], "p")
    query.where(":category in elements(p.categories)", me.category)
    query.where("p.domain=:domain", domainService.getDomain)
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

  private def topNotices(categoryId: Int): Iterable[Notice] = {
    val q = OqlBuilder.from(classOf[Notice], "notice")
    get("app").foreach { app =>
      q.where("notice.app.name=:appName", app)
    }
    q.join("notice.categories", "uc")
    q.where("uc.id=:categoryId", categoryId)
    q.where("notice.endOn >= :now", LocalDate.now)
    q.where("notice.archived=false and notice.status=:status", NoticeStatus.Passed)
    q.where("notice.app.domain=:domain", domainService.getDomain)
    q.cacheable()
    q.orderBy("notice.publishedAt desc")
    entityDao.topN(10, q)
  }

  private def topDocs(categoryId: Int): Iterable[Doc] = {
    val q = OqlBuilder.from(classOf[Doc], "doc")
    q.join("doc.categories", "uc")
    get("app").foreach { app =>
      q.where("doc.app.name=:appName", app)
    }
    q.where("uc.id=:categoryId", categoryId)
    q.where("doc.archived=false")
    q.where("doc.app.domain=:domain", domainService.getDomain)
    q.orderBy("doc.updatedAt desc")
    q.cacheable()
    entityDao.topN(10, q)
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

  def redirect(): View = {
    get("url") match
      case Some(url) =>
        get("target") match
          case Some(target) =>
            put("url", url)
            put("target", target)
            forward()
          case _ =>
            val builder = UrlBuilder(ActionContext.current.request)
            builder.setRequestURI(url)
            builder.setContextPath("").setPathInfo(null).setQueryString(null)
            redirect(to(builder.buildUrl()), "")
      case None => Status.NotFound
  }
}
