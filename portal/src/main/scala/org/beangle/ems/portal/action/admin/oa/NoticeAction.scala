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
import org.beangle.commons.collection.Properties
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.oa.model.*
import org.beangle.ems.core.oa.service.DocService
import org.beangle.ems.core.user.model.{Category, User}
import org.beangle.ems.core.user.service.UserService
import org.beangle.ems.portal.action.admin.DomainSupport
import org.beangle.event.bus.DataEvent
import org.beangle.security.Securities
import org.beangle.she.webmvc.RestfulAction
import org.beangle.webmvc.annotation.{ignore, response}
import org.beangle.webmvc.view.View

import java.io.InputStream
import java.time.{Instant, LocalDate, ZoneId}

class NoticeAction extends RestfulAction[Notice], DomainSupport {

  var userService: UserService = _

  private val allowExts = Set("doc", "docx", "xls", "xlsx", "pdf", "zip", "rar", "jpg", "png")

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
    val attachments = notices.flatMap(_.attachments)
    entityDao.remove(notices, attachments)
    databus.publish(DataEvent.remove(notices))
    val blob = EmsApp.getBlobRepository(true)
    attachments foreach { a =>
      blob.remove(a.filePath)
    }
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

  def saveAttachment(notice: Notice, filename: String, is: InputStream): NoticeAttachment = {
    val repo = EmsApp.getBlobRepository()

    val meta = repo.upload(s"/notice/${notice.updatedAt.atZone(ZoneId.systemDefault()).getYear}", is, filename, notice.operator.code + " " + notice.operator.name)
    val attachment = new NoticeAttachment
    attachment.name = filename
    attachment.filePath = meta.filePath
    attachment.fileSize = meta.fileSize
    attachment.embedded = false
    attachment.notice = notice
    attachment
  }

  @response
  def uploadImage(): Properties = {
    val rs = new Properties()
    get("imgFile", classOf[Part]) match
      case Some(part) =>
        val blob = EmsApp.getBlobRepository(true)
        val notice = entityDao.find(classOf[Notice], getLongId("notice")).head
        val a = new NoticeAttachment
        if (allowExts.contains(Strings.substringAfterLast(part.getSubmittedFileName, "."))) {
          val attachment = saveAttachment(notice, part.getSubmittedFileName, part.getInputStream)
          attachment.embedded = true
          notice.addAttachment(attachment)
          entityDao.saveOrUpdate(notice)
          var url = blob.uri(attachment.filePath).toString
          if (url.contains("?")) {
            url = Strings.substringBefore(url, "?")
          }
          rs.put("error", 0)
          rs.put("url", url)
          rs.put("message", "上传成功d")
        } else {
          rs.put("error", 1)
          rs.put("message", "不支持的附件类型")
        }
      case None =>
        rs.put("error", 1)
        rs.put("message", "图片不能为空")

    rs
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
    var disallowed = false
    getAll("notice_doc", classOf[Part]) foreach { docFile =>
      if (allowExts.contains(Strings.substringAfterLast(docFile.getSubmittedFileName, "."))) {
        val a = saveAttachment(notice, docFile.getSubmittedFileName, docFile.getInputStream)
        notice.addAttachment(a)
      } else {
        disallowed = true
      }
    }
    if (disallowed) {
      addFlashMessage("非法文件类型，附件仅允许doc,docx,xls,xlsx,pdf,zip,jpg,png后缀的文件")
      throw new RuntimeException("非法文件类型，附件仅允许doc,docx,xls,xlsx,pdf,zip,jpg,png后缀的文件")
    } else {
      notice.status = NoticeStatus.Submited
      entityDao.saveOrUpdate(notice)
      databus.publish(DataEvent.update(notice))
      super.saveAndRedirect(notice)
    }
  }
}
