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

package org.beangle.ems.ws.config

import org.beangle.commons.collection.Properties
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.core.config.model.Theme
import org.beangle.ems.core.config.service.DomainService
import org.beangle.web.action.annotation.response
import org.beangle.web.action.support.ActionSupport

class ThemeWS extends ActionSupport {

  var domainService: DomainService = _
  var entityDao: EntityDao = _

  @response(cacheable = true)
  def index(): Properties = {
    val domain = domainService.getDomain
    val query = OqlBuilder.from(classOf[Theme], "theme")
    query.where("theme.domain =:domain and theme.enabled=true", domain)
    query.cacheable()
    val theme = entityDao.search(query).headOption.getOrElse(Theme.Default)
    new Properties(theme, "primaryColor","navbarBgColor", "searchBgColor", "gridbarBgColor", "gridBorderColor")
  }

}
