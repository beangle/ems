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
package org.beangle.ems.portal.admin.action.config

import jakarta.servlet.http.Part
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.config.model.{App, Template}
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.ems.core.user.model.User
import org.beangle.security.Securities
import org.beangle.webmvc.api.annotation.mapping
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.entity.action.RestfulAction

class TemplateAction extends RestfulAction[Template] {

  var domainService: DomainService = _
  var appService: AppService = _

  override protected def indexSetting(): Unit = {
    put("apps", appService.getWebapps)
  }

  override protected def getQueryBuilder: OqlBuilder[Template] = {
    val query = super.getQueryBuilder
    val domain = domainService.getDomain
    query.where("template.app.domain=:domain", domain)
  }

  override protected def editSetting(entity: Template): Unit = {
    put("apps", appService.getWebapps)
    super.editSetting(entity)
  }

  override protected def saveAndRedirect(template: Template): View = {
    val repo = EmsApp.getBlobRepository(true)
    val user = entityDao.findBy(classOf[User], "code", List(Securities.user)).head
    var filename = Strings.substringAfterLast(template.name, "/")
    val part = get("attachment", classOf[Part]).get
    if (!filename.contains(".")) {
      filename = part.getSubmittedFileName
      if (!template.name.endsWith("/")) {
        template.name += "/"
      }
      template.name += filename
    }
    if (template.name.startsWith("/")) {
      template.name = template.name.substring(1)
    }
    val is = part.getInputStream
    if (template.persisted) {
      repo.remove(template.filePath)
    }
    val app = entityDao.get(classOf[App], template.app.id)
    var storeFileName = template.name
    storeFileName = Strings.replace(storeFileName.substring(1), "/", "_")
    val meta = repo.upload(s"/template/${app.name}", is, storeFileName, user.code + " " + user.name)
    template.updatedAt = meta.updatedAt
    template.filePath = meta.filePath
    template.fileSize = meta.fileSize
    template.mediaType = meta.mediaType

    entityDao.saveOrUpdate(template)
    redirect("search", "info.save.success")
  }

  override protected def removeAndRedirect(entities: Seq[Template]): View = {
    val repo = EmsApp.getBlobRepository(true)
    entities foreach { t =>
      repo.remove(t.filePath)
    }
    super.removeAndRedirect(entities)
  }

  @mapping(value = "{id}")
  override def info(id: String): View = {
    val response = ActionContext.current.response
    val template = entityDao.get(classOf[Template], id.toLong)
    val repo = EmsApp.getBlobRepository(true)
    repo.path(template.filePath) match {
      case Some(p) => response.sendRedirect(p)
      case None => response.setStatus(404)
    }
    null
  }
}
