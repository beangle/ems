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

package org.beangle.ems.portal.action.admin.session

import org.beangle.commons.collection.Order
import org.beangle.ems.core.security.service.SessionInfoService
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.view.View
import org.beangle.webmvc.support.helper.QueryHelper

class IndexAction extends ActionSupport {

  var sessionInfoService: SessionInfoService = _

  def index(): View = {
    val infoes = sessionInfoService.find(None, QueryHelper.pageLimit, get(Order.OrderStr))
    put("sessionInfoes", infoes)
    forward()
  }

}
