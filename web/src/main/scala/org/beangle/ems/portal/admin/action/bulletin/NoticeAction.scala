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
package org.beangle.ems.portal.admin.action.bulletin

import java.time.{Instant, LocalDate}

import jakarta.servlet.http.Part
import org.beangle.data.dao.OqlBuilder
import org.beangle.security.Securities
import org.beangle.webmvc.api.annotation.ignore
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.entity.action.RestfulAction
import org.beangle.ems.core.bulletin.model._
import org.beangle.ems.core.bulletin.service.DocService
import org.beangle.ems.core.config.model.{App, AppType}
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.ems.core.user.model.{User, UserCategory}
import org.beangle.ems.core.user.service.UserService

class NoticeAction extends RestfulAction[Notice] {

  var docService: DocService = _
  var userService: UserService = _
  var domainService: DomainService = _
  var appService: AppService = _

  override protected def indexSetting(): Unit = {
    put("userCategories", userService.getCategories())
    put("apps", appService.getWebapps)
  }


  override protected def editSetting(entity: Notice): Unit = {
    put("userCategories", userService.getCategories())
    put("apps", appService.getWebapps)
    if (null == entity.status) {
      entity.status = NoticeStatus.Draft
    }
  }


  override protected def removeAndRedirect(notices: Seq[Notice]): View = {
    val docs = notices.flatMap(_.docs)
    entityDao.remove(notices, docs)
    redirect("search", "info.remove.success")
  }

  override protected def getQueryBuilder: OqlBuilder[Notice] = {
    val builder = super.getQueryBuilder
    builder.where("notice.app.domain=:domain", domainService.getDomain)
    getInt("userCategory.id") foreach { categoryId =>
      builder.join("notice.userCategories", "uc")
      builder.where("uc.id=:userCategoryId", categoryId)
    }
    getBoolean("active") foreach { active =>
      if (active) {
        builder.where(":now between notice.beginOn and notice.endOn", LocalDate.now)
      } else {
        builder.where(" not(:now between notice.beginOn and notice.endOn)", LocalDate.now)
      }
    }
    builder
  }

  @ignore
  override protected def saveAndRedirect(notice: Notice): View = {
    notice.updatedAt = Instant.now
    if (null == notice.createdAt) {
      notice.createdAt = notice.updatedAt
    }
    val words = entityDao.getAll(classOf[SensitiveWord]).map(_.contents).toSet
    val results = SensitiveFilter(words).matchedWords(notice.contents)
    if (results.nonEmpty) {
      addFlashMessage("找到敏感词汇:" + results.mkString(","))
      throw new RuntimeException("找到敏感词汇:" + results.mkString(","))
    }
    notice.operator = entityDao.findBy(classOf[User], "code", List(Securities.user)).head
    notice.userCategories.clear()
    notice.userCategories ++= entityDao.find(classOf[UserCategory], intIds("userCategory"))
    getAll("notice_doc", classOf[Part]) foreach { docFile =>
      val doc = new Doc
      doc.app = notice.app
      doc.uploadBy = notice.operator
      doc.userCategories ++= notice.userCategories
      doc.updatedAt = Instant.now
      docService.save(doc, docFile.getSubmittedFileName, docFile.getInputStream)
      notice.docs += doc
    }
    notice.status = NoticeStatus.Submited
    super.saveAndRedirect(notice)
  }
}
