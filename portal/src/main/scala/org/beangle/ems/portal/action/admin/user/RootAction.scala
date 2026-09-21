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

package org.beangle.ems.portal.action.admin.user

import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.core.user.model.Root
import org.beangle.ems.core.user.service.UserService
import org.beangle.ems.portal.action.admin.DomainSupport
import org.beangle.event.bus.DataEvent
import org.beangle.security.context.SecurityContext
import org.beangle.she.webmvc.{QueryHelper, RestfulAction}
import org.beangle.webmvc.view.View

import java.time.LocalDate
import scala.compiletime.uninitialized

/**
 * 超级用户管理
 *
 * @author chaostone
 */
class RootAction extends RestfulAction[Root], DomainSupport {

  var userService: UserService = uninitialized

  override protected def indexSetting(): Unit = {
    put("isRoot", SecurityContext.get.isRoot)
    super.indexSetting()
  }

  override protected def getQueryBuilder: OqlBuilder[Root] = {
    val builder = super.getQueryBuilder
    builder.where("root.domain=:domain", domainService.getDomain)
    QueryHelper.addActive(builder, getBoolean("active"))
    builder
  }

  override def editSetting(root: Root): Unit = {
    if (!root.persisted) root.beginOn = LocalDate.now
    super.editSetting(root)
  }

  override protected def saveAndRedirect(root: Root): View = {
    get("user") foreach { u =>
      userService.get(u) foreach { user =>
        root.user = user
      }
    }
    root.domain = domainService.getDomain
    if (root.user == null) {
      addError("查不到账户")
      put(simpleEntityName, root)
      editSetting(root)
      forward("form")
    } else if (root.endOn.exists(_.isBefore(root.beginOn))) {
      addError("失效日期不能早于生效日期")
      put(simpleEntityName, root)
      editSetting(root)
      forward("form")
    } else {
      entityDao.saveOrUpdate(root)
      publishUpdate(root)
      super.saveAndRedirect(root)
    }
  }

  override protected def removeAndRedirect(roots: Seq[Root]): View = {
    databus.publish(DataEvent.remove(roots))
    super.removeAndRedirect(roots)
  }
}
