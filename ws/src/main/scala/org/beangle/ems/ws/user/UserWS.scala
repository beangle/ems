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
import org.beangle.commons.lang.{Charsets, Strings}
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.core.user.model.User
import org.beangle.webmvc.annotation.{mapping, param, response}
import org.beangle.webmvc.support.ActionSupport

import java.net.URLDecoder

class UserWS extends ActionSupport {

  var entityDao: EntityDao = _

  @response
  @mapping("")
  def index(@param("q") q: String): Iterable[Properties] = {
    val query = OqlBuilder.from(classOf[User], "u")
    val p = "%" + URLDecoder.decode(q, Charsets.UTF_8) + "%"
    query.where("u.code like :q or u.name like :q ", p)
    query.limit(1, 20)
    val users = entityDao.search(query)
    users.map(x => new Properties(x, "id", "code", "name", "description"))
  }

  @response
  @mapping("openid/{openid}")
  def openid(@param("openid") openid: String): String = {
    if (Strings.isBlank(openid)) {
      ""
    } else {
      val q = OqlBuilder.from[String](classOf[User].getName, "u")
      q.where("u.openid=:openid", openid)
      q.select("u.code")
      entityDao.first(q).getOrElse("")
    }
  }

  @response
  def check(@param("username") username: String, @param("openid") openid: String): Boolean = {
    if (Strings.isBlank(username) || Strings.isBlank(openid)) {
      false
    } else {
      val q = OqlBuilder.from(classOf[User], "u")
      q.where("u.code=:code and u.openid=:openid", username, openid)
      q.select("u.id")
      entityDao.first(q).nonEmpty
    }
  }
}
