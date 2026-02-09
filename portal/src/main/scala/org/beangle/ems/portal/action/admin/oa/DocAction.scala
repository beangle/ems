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

package org.beangle.ems.portal.action.admin.oa

import jakarta.servlet.http.Part
import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.oa.model.Doc
import org.beangle.ems.core.oa.service.DocService
import org.beangle.ems.core.user.model.{Category, User}
import org.beangle.ems.core.user.service.UserService
import org.beangle.ems.portal.action.admin.DomainSupport
import org.beangle.event.bus.DataEvent
import org.beangle.security.Securities
import org.beangle.webmvc.annotation.{ignore, param}
import org.beangle.webmvc.support.ServletSupport
import org.beangle.webmvc.support.action.RestfulAction
import org.beangle.webmvc.view.{Status, Stream, View}

import java.io.File
import java.time.Instant

class DocAction extends RestfulAction[Doc], ServletSupport, DomainSupport {

  var docService: DocService = _
  var userService: UserService = _

  override protected def indexSetting(): Unit = {
    put("categories", userService.getCategories())
  }

  override protected def getQueryBuilder: OqlBuilder[Doc] = {
    val builder = super.getQueryBuilder
    builder.where("doc.app.domain=:domain", domainService.getDomain)
    getInt("category.id") foreach { categoryId =>
      builder.join("doc.categories", "uc")
      builder.where("uc.id=:categoryId", categoryId)
    }
    builder
  }

  override protected def editSetting(entity: Doc): Unit = {
    put("categories", entityDao.getAll(classOf[Category]))
    put("apps", appService.getWebapps)
  }

  def download(@param("id") id: String): View = {
    val doc = entityDao.get(classOf[Doc], id.toLong)
    val p = EmsApp.getBlobRepository().path(doc.filePath)
    if p.startsWith("http") then
      response.sendRedirect(p)
      null
    else Stream(new File(p), doc.name)
  }

  @ignore
  override protected def removeAndRedirect(entities: Seq[Doc]): View = {
    try {
      entities.foreach { doc =>
        docService.remove(doc)
      }
      databus.publish(DataEvent.update(entities))
      redirect("search", "info.remove.success")
    } catch {
      case e: Exception => redirect("search", "info.delete.failure")
    }
  }

  @ignore
  override protected def saveAndRedirect(doc: Doc): View = {
    doc.updatedAt = Instant.now
    doc.uploadBy = entityDao.findBy(classOf[User], "code", List(Securities.user)).head
    doc.categories.clear()
    doc.categories ++= entityDao.find(classOf[Category], getIntIds("category"))
    getAll("docfile", classOf[Part]) foreach { docFile =>
      docService.save(doc, docFile.getSubmittedFileName, docFile.getInputStream)
    }
    entityDao.saveOrUpdate(doc)
    databus.publish(DataEvent.update(doc))
    super.saveAndRedirect(doc)
  }
}
