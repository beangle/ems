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

import org.beangle.commons.collection.Collections
import org.beangle.ems.core.config.model.{Business, RuleMeta, RuleParamMeta}
import org.beangle.ems.core.config.service.DomainService
import org.beangle.event.bus.{DataEvent, DataEventBus}
import org.beangle.webmvc.context.Params
import org.beangle.webmvc.support.action.RestfulAction
import org.beangle.webmvc.support.helper.PopulateHelper
import org.beangle.webmvc.view.View

class RuleMetaAction extends RestfulAction[RuleMeta] {

  var databus: DataEventBus = _
  var domainService: DomainService = _

  override protected def editSetting(entity: RuleMeta): Unit = {
    put("businesses", entityDao.getAll(classOf[Business]))
    super.editSetting(entity)
  }

  override protected def saveAndRedirect(meta: RuleMeta): View = {
    val params = meta.params.toBuffer.map(x => (x.name, x)).toMap
    val paramNames = Collections.newSet[String]
    (0 to 10) foreach { i =>
      val p = Params.sub(i.toString)
      val paramMeta = PopulateHelper.populate(new RuleParamMeta, p)
      if (null != paramMeta.name && null != paramMeta.title) {
        paramNames += paramMeta.name
        params.get(paramMeta.name) match {
          case Some(x) =>
            x.title = paramMeta.title
            x.description = paramMeta.description
          case None =>
            paramMeta.ruleMeta = meta
            meta.params += paramMeta
        }
      }
    }
    meta.params.subtractAll(meta.params.filter(x => !paramNames.contains(x.name)))
    meta.domain = domainService.getDomain
    entityDao.saveOrUpdate(meta)
    databus.publish(DataEvent.update(meta))
    super.saveAndRedirect(meta)
  }
}
