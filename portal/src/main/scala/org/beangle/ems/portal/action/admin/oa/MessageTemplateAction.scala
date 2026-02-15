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

import org.beangle.commons.lang.Strings
import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.core.config.model.Business
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.oa.model.MessageTemplate
import org.beangle.she.webmvc.RestfulAction
import org.beangle.webmvc.view.View

/** 消息模板维护
 */
class MessageTemplateAction extends RestfulAction[MessageTemplate] {

  var domainService: DomainService = _

  override def indexSetting(): Unit = {
    super.indexSetting()
    put("businesses", getBusinesses())
  }

  override protected def editSetting(entity: MessageTemplate): Unit = {
    put("businesses", getBusinesses())
    super.editSetting(entity)
  }

  private def getBusinesses(): Seq[Business] = {
    entityDao.findBy(classOf[Business], "domain", domainService.getDomain)
  }

  override protected def getQueryBuilder: OqlBuilder[MessageTemplate] = {
    val query = super.getQueryBuilder
    query.where("template.business.domain=:domain", domainService.getDomain)
    query
  }

  override def saveAndRedirect(template: MessageTemplate): View = {
    template.title = Strings.replace(template.title, "\n", "")
    template.title = Strings.replace(template.title, "\t", "")

    template.variables foreach { v =>
      var vb = v
      vb = Strings.replace(vb, "\n", "")
      vb = Strings.replace(vb, "\t", "")
      vb = Strings.replace(vb, "；", ";")
      vb = Strings.replace(vb, "，", ";")
      vb = Strings.replace(vb, ",", ";")
      template.variables = Some(vb)
    }
    super.saveAndRedirect(template)
  }

  override protected def simpleEntityName: String = {
    "template"
  }
}
