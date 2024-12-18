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

import org.beangle.commons.lang.Strings
import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.core.config.model.TextBundle
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.ems.portal.action.admin.DomainSupport
import org.beangle.event.bus.{DataEvent, DataEventBus}
import org.beangle.webmvc.view.View
import org.beangle.webmvc.support.action.{ExportSupport, RestfulAction}

import java.util.Locale

class TextBundleAction extends RestfulAction[TextBundle], ExportSupport[TextBundle],DomainSupport {

  override protected def indexSetting(): Unit = {
    put("apps", appService.getWebapps)
  }

  override protected def getQueryBuilder: OqlBuilder[TextBundle] = {
    val query = super.getQueryBuilder
    val domain = domainService.getDomain
    query.where("bundle.app.domain=:domain", domain)
  }

  override protected def editSetting(entity: TextBundle): Unit = {
    put("apps", appService.getWebapps)
    put("locales", Map(Locale.SIMPLIFIED_CHINESE -> "中文", Locale.US -> "英文"))
    super.editSetting(entity)
  }

  override protected def saveAndRedirect(bundle: TextBundle): View = {
    bundle.texts = Strings.replace(bundle.texts, "\r", "")
    saveOrUpdate(bundle)
    publishUpdate(bundle)
    super.saveAndRedirect(bundle)
  }

  override protected def simpleEntityName: String = "bundle"

}
