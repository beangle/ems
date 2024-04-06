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

package org.beangle.ems.ws.user

import org.beangle.commons.collection.Properties
import org.beangle.commons.lang.Charsets
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.core.user.model.User
import org.beangle.web.action.annotation.{mapping, param, response}
import org.beangle.web.action.support.ActionSupport

import java.net.URLDecoder

class UserWS extends ActionSupport {

  var entityDao: EntityDao = _

  @response
  @mapping("{q}")
  def index(@param("q") q: String): Iterable[Properties] = {
    val query = OqlBuilder.from(classOf[User], "u")
    query.where("u.name like :q ", "%" + URLDecoder.decode(q,Charsets.UTF_8) + "%")
    query.limit(1, 20)
    val users = entityDao.search(query)
    users.map(x => new Properties(x, "id", "name"))
  }
}
