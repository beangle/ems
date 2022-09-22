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

package org.beangle.ems.portal.admin.action.config

import org.beangle.ems.core.config.model.Portalet
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.user.model.UserCategory
import org.beangle.web.action.support.ActionSupport
import org.beangle.web.action.view.View
import org.beangle.webmvc.support.action.{EntityAction, RestfulAction}

class PortaletAction extends RestfulAction[Portalet] {

  override def indexSetting(): Unit = {
    put("categories", entityDao.getAll(classOf[UserCategory]))
  }

  override protected def editSetting(entity: Portalet): Unit = {
    put("categories", entityDao.getAll(classOf[UserCategory]))
  }

  override protected def saveAndRedirect(p: Portalet): View = {
    val categories = entityDao.find(classOf[UserCategory], intIds("category"))
    p.categories.clear()
    p.categories.addAll(categories)
    if (!p.url.startsWith("http") && !p.url.startsWith("/")) p.url = "http://" + p.url
    super.saveAndRedirect(p)
  }

  def preview(): View = {
    forward()
  }

}