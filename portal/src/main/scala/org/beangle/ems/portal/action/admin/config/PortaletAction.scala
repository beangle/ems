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

import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.core.config.model.Portalet
import org.beangle.ems.core.user.model.Category
import org.beangle.ems.portal.action.admin.DomainSupport
import org.beangle.webmvc.view.View
import org.beangle.webmvc.support.action.RestfulAction

class PortaletAction extends RestfulAction[Portalet], DomainSupport {

  override def indexSetting(): Unit = {
    put("categories", entityDao.getAll(classOf[Category]))
  }

  override protected def editSetting(entity: Portalet): Unit = {
    put("categories", entityDao.getAll(classOf[Category]))
  }

  override protected def getQueryBuilder: OqlBuilder[Portalet] = {
    val builder = super.getQueryBuilder
    builder.where("portalet.domain=:domain", domainService.getDomain)
    builder
  }

  override protected def saveAndRedirect(p: Portalet): View = {
    p.domain = domainService.getDomain
    val categories = entityDao.find(classOf[Category], getIntIds("category"))
    p.categories.clear()
    p.categories.addAll(categories)
    if (!p.url.startsWith("http") && !p.url.startsWith("/")) p.url = "http://" + p.url
    saveOrUpdate(p)
    publishUpdate(p)
    super.saveAndRedirect(p)
  }

  def preview(): View = {
    forward()
  }

}
