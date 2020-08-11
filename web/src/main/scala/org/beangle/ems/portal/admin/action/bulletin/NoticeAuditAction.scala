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

import org.beangle.security.Securities
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.annotation.{mapping, param}
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.entity.action.EntityAction
import org.beangle.ems.core.bulletin.model.{Notice, NoticeStatus}
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.ems.core.user.model.User
import org.beangle.ems.core.user.service.UserService

class NoticeAuditAction extends ActionSupport with EntityAction[Notice] {
  var userService: UserService = _
  var domainService: DomainService = _
  var appService: AppService = _

  def index(): View = {
    put("userCategories", userService.getCategories())
    put("apps", appService.getWebapps)
    forward()
  }

  def archive(): View = {
    val notices = entityDao.find(classOf[Notice], longIds("notice"))
    val archived = getBoolean("archived", true)
    notices foreach { notice =>
      if (notice.status == NoticeStatus.Passed) {
        notice.archived = archived
      }
    }
    entityDao.saveOrUpdate(notices)
    redirect("search", "info.save.success")
  }

  def search(): View = {
    val builder = getQueryBuilder
    builder.where("notice.status != :status", NoticeStatus.Draft)
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
    getBoolean("attached") foreach { attached =>
      builder.where(if (attached) "size(notice.docs)>0" else "size(notice.docs)=0")
    }
    put("notices", entityDao.search(builder))
    forward()
  }

  def audit(): View = {
    val notices = entityDao.find(classOf[Notice], longIds("notice"))
    val passed = getBoolean("passed", defaultValue = false)
    val me = entityDao.findBy(classOf[User], "code", List(Securities.user)).head
    notices foreach { notice =>
      notice.auditor = Some(me)
      notice.updatedAt = Instant.now
      if (passed) {
        notice.publishedAt = Some(Instant.now)
        notice.status = NoticeStatus.Passed
      } else {
        notice.publishedAt = None
        notice.status = NoticeStatus.Unpassed
      }
    }
    entityDao.saveOrUpdate(notices)
    redirect("search", "info.save.success")
  }

  @mapping(value = "{id}")
  def info(@param("id") id: String): View = {
    put(simpleEntityName, getModel[Notice](entityName, convertId(id)))
    forward()
  }
}
