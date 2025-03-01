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

package org.beangle.ems.app

import org.beangle.security.Securities
import org.beangle.webmvc.context.ActionContext

object EmsApi {

  def getDatasourceUrl(resourceKey: String): String = {
    Ems.api + "/platform/config/datasources/" + EmsApp.name + "/" + resourceKey + ".xml?secret=" + EmsApp.secret
  }

  def getRedisUrl: String = {
    Ems.api + "/platform/config/rediss/" + EmsApp.name + ".xml?secret=" + EmsApp.secret
  }

  /** 构造一个完整url
   *
   * @param uri
   * @return
   */
  def url(uri: String): String = {
    val base = Ems.base + ActionContext.current.request.getContextPath + uri
    val sidParam = s"${Ems.sid.name}=" + Securities.session.map(_.id).getOrElse("")
    if base.contains("?") then s"${base}&$sidParam" else s"${base}?$sidParam"
  }

  def url(contextPath: String, uri: String): String = {
    val base = Ems.base + contextPath + uri
    val sidParam = s"${Ems.sid.name}=" + Securities.session.map(_.id).getOrElse("")
    if base.contains("?") then s"${base}&$sidParam" else s"${base}?$sidParam"
  }
}
