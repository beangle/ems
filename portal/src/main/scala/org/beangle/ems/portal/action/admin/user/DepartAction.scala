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

import org.beangle.commons.collection.Order
import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.user.model.Depart
import org.beangle.event.bus.{DataEvent, DataEventBus}
import org.beangle.webmvc.annotation.ignore
import org.beangle.she.webmvc.RestfulAction
import org.beangle.she.webmvc.QueryHelper
import org.beangle.webmvc.view.View

import java.time.LocalDate

class DepartAction extends RestfulAction[Depart] {
  var databus: DataEventBus = _

  var domainService: DomainService = _

  override protected def getQueryBuilder: OqlBuilder[Depart] = {
    val builder = OqlBuilder.from(classOf[Depart], "depart")
    builder.where("depart.org=:org", domainService.getOrg)
    populateConditions(builder)
    QueryHelper.addActive(builder, getBoolean("active"))
    builder.orderBy(get(Order.OrderStr).orNull).limit(getPageLimit)
  }

  override def editSetting(depart: Depart): Unit = {
    val org = domainService.getOrg
    super.editSetting(depart)
    if !depart.persisted then depart.beginOn = LocalDate.now

    val parents = entityDao.findBy(classOf[Depart], "org", org).toBuffer
    if (depart.persisted) parents.subtractOne(depart)
    put("parents", parents)
  }

  @ignore
  override protected def saveAndRedirect(depart: Depart): View = {
    depart.org = domainService.getOrg
    entityDao.saveOrUpdate(depart)
    databus.publish(DataEvent.update(depart))
    super.saveAndRedirect(depart)
  }

}
