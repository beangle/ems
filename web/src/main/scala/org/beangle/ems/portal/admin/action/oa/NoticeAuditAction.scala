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

package org.beangle.ems.portal.admin.action.oa

import org.beangle.data.dao.EntityDao
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.ems.core.oa.model.{Notice, NoticeStatus}
import org.beangle.ems.core.user.model.User
import org.beangle.ems.core.user.service.UserService
import org.beangle.event.bus.{DataEvent, DataEventBus}
import org.beangle.security.Securities
import org.beangle.web.action.annotation.{mapping, param}
import org.beangle.web.action.support.ActionSupport
import org.beangle.web.action.view.View
import org.beangle.webmvc.support.action.EntityAction

import java.time.{Instant, LocalDate}

class NoticeAuditAction extends ActionSupport with EntityAction[Notice] {
  var userService: UserService = _
  var domainService: DomainService = _
  var appService: AppService = _
  var entityDao: EntityDao = _
  var databus: DataEventBus = _

  def index(): View = {
    put("categories", userService.getCategories())
    put("apps", appService.getWebapps)
    forward()
  }

  def archive(): View = {
    val notices = entityDao.find(classOf[Notice], getLongIds("notice"))
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
    getBoolean("attached") foreach { attached =>
      builder.where(if (attached) "size(notice.docs)>0" else "size(notice.docs)=0")
    }
    builder.where("notice.operator.code != :me", Securities.user)
    put("notices", entityDao.search(builder))
    forward()
  }

  def audit(): View = {
    val notices = entityDao.find(classOf[Notice], getLongIds("notice"))
    val passed = getBoolean("passed", defaultValue = false)
    val me = entityDao.findBy(classOf[User], "code", List(Securities.user)).head
    notices foreach { notice =>
      if (notice.operator != me) {
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
    }
    entityDao.saveOrUpdate(notices)
    databus.publish(DataEvent.update(notices))
    redirect("search", "info.save.success")
  }

  @mapping(value = "{id}")
  def info(@param("id") id: String): View = {
    val notice: Notice = getModel(id.toLong)
    put(simpleEntityName, notice)
    forward()
  }
}
