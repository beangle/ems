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
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.ems.core.oa.model.*
import org.beangle.ems.core.oa.service.DocService
import org.beangle.ems.core.user.model.{Category, User}
import org.beangle.ems.core.user.service.UserService
import org.beangle.security.Securities
import org.beangle.web.action.annotation.ignore
import org.beangle.web.action.view.View
import org.beangle.webmvc.support.action.RestfulAction

import java.time.{Instant, LocalDate}

class NoticeAction extends RestfulAction[Notice] {

  var docService: DocService = _
  var userService: UserService = _
  var domainService: DomainService = _
  var appService: AppService = _

  override protected def indexSetting(): Unit = {
    put("categories", userService.getCategories())
    put("apps", appService.getWebapps)
  }

  override protected def editSetting(entity: Notice): Unit = {
    put("categories", userService.getCategories())
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
    getInt("category.id") foreach { categoryId =>
      builder.join("notice.categories", "uc")
      builder.where("uc.id=:categoryId", categoryId)
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
    notice.categories.clear()
    notice.categories ++= entityDao.find(classOf[Category], getIntIds("category"))
    val allowExts = Set("doc", "docx", "xls", "xlsx", "pdf", "zip", "rar","jpg","png")
    var disallowed = false
    getAll("notice_doc", classOf[Part]) foreach { docFile =>
      val doc = new Doc
      doc.app = notice.app
      doc.uploadBy = notice.operator
      doc.categories ++= notice.categories
      doc.updatedAt = Instant.now
      if (allowExts.contains(Strings.substringAfterLast(docFile.getSubmittedFileName, "."))) {
        docService.save(doc, docFile.getSubmittedFileName, docFile.getInputStream)
        notice.docs += doc
      } else {
        disallowed = true
      }
    }
    if (disallowed) {
      addFlashMessage("非法文件类型，附件仅允许doc,docx,xls,xlsx,pdf,zip,jpg,png后缀的文件")
      throw new RuntimeException("非法文件类型，附件仅允许doc,docx,xls,xlsx,pdf,zip,jpg,png后缀的文件")
    } else {
      notice.status = NoticeStatus.Submited
      super.saveAndRedirect(notice)
    }
  }
}
