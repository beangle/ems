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

package org.beangle.ems.portal.action.admin.config

import jakarta.servlet.http.Part
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.config.model.{App, File}
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.ems.core.user.model.User
import org.beangle.security.Securities
import org.beangle.webmvc.annotation.mapping
import org.beangle.webmvc.context.ActionContext
import org.beangle.she.webmvc.{ExportSupport, RestfulAction}
import org.beangle.webmvc.view.View

class FileAction extends RestfulAction[File], ExportSupport[File] {

  var domainService: DomainService = _
  var appService: AppService = _

  override protected def indexSetting(): Unit = {
    put("apps", appService.getWebapps)
  }

  override protected def getQueryBuilder: OqlBuilder[File] = {
    val query = super.getQueryBuilder
    val domain = domainService.getDomain
    query.where("file.app.domain=:domain", domain)
  }

  override protected def editSetting(entity: File): Unit = {
    put("apps", appService.getWebapps)
    super.editSetting(entity)
  }

  override protected def saveAndRedirect(f: File): View = {
    val repo = EmsApp.getBlobRepository()
    val user = entityDao.findBy(classOf[User], "code", List(Securities.user)).head
    var filename = Strings.substringAfterLast(f.name, "/")
    val part = get("attachment", classOf[Part]).get
    if (!filename.contains(".")) {
      filename = part.getSubmittedFileName
      if (!f.name.endsWith("/")) {
        f.name += "/"
      }
      f.name += filename
    }
    if (f.name.startsWith("/")) {
      f.name = f.name.substring(1)
    }
    val is = part.getInputStream
    if (f.persisted) {
      repo.remove(f.filePath)
    }
    val app = entityDao.get(classOf[App], f.app.id)
    var fname = f.name
    if (fname.charAt(0) == '/') fname = fname.substring(1)
    fname = Strings.replace(fname, "/", "_")
    val meta = repo.upload(s"/file/${app.name}", is, fname, user.code + " " + user.name)
    f.updatedAt = meta.updatedAt
    f.filePath = meta.filePath
    f.fileSize = meta.fileSize
    f.mediaType = meta.mediaType

    entityDao.saveOrUpdate(f)
    redirect("search", "info.save.success")
  }

  override protected def removeAndRedirect(entities: Seq[File]): View = {
    val repo = EmsApp.getBlobRepository()
    entities foreach { t =>
      repo.remove(t.filePath)
    }
    super.removeAndRedirect(entities)
  }

  @mapping(value = "{id}")
  override def info(id: String): View = {
    val response = ActionContext.current.response
    val template = entityDao.get(classOf[File], id.toLong)
    val repo = EmsApp.getBlobRepository()
    redirect(to(repo.path(template.filePath)), "")
  }
}
