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

import org.beangle.commons.collection.{Collections, Properties}
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.security.service.ProfileService
import org.beangle.ems.core.user.model.{Dimension, EnvProfile}
import org.beangle.ems.core.user.service.UserService
import org.beangle.webmvc.annotation.{mapping, param, response}
import org.beangle.webmvc.support.ActionSupport

/**
 * @author chaostone
 */
class ProfileWS(entityDao: EntityDao) extends ActionSupport {

  var userService: UserService = _

  var profileService: ProfileService = _

  var domainService: DomainService = _

  @response(cacheable = true)
  @mapping("{userCode}")
  def index(@param("userCode") userCode: String): Any = {
    userService.get(userCode) match {
      case Some(user) =>
        val domain = domainService.getDomain
        val userProfileQuery = OqlBuilder.from(classOf[EnvProfile], "up")
          .where("up.user =:user", user)
          .where("up.domain=:domain", domain)
          .cacheable()

        val profiles = entityDao.search(userProfileQuery)
        val dquery = OqlBuilder.from(classOf[Dimension], "d")
        dquery.where("d.domain=:domain", domain)
        dquery.cacheable()
        val dimensions = entityDao.search(dquery).map(x => (x.name, x)).toMap

        val resolved = getBoolean("resolved", defaultValue = false)
        profiles map { profile =>
          val p = new Properties()
          p.put("id", profile.id)
          p.put("name", profile.env.name)
          if (resolved) {
            val properties = Collections.newBuffer[Properties]
            p.put("properties", properties)
            profile.properties foreach {
              case (dname, v) =>
                val entry = new Properties()
                val dimension = new Properties()
                val d = dimensions(dname)
                dimension.put("id", d.id)
                dimension.put("name", d.name)
                dimension.put("title", d.title)
                d.keyName foreach { kn =>
                  dimension.put("keyName", kn)
                }
                entry.put("dimension", dimension)
                entry.put("value", profileService.getDimensionValues(d, v.toString))
                properties += entry
            }
          } else {
            profile.properties foreach {
              case (d, v) => p.put(d, v)
            }
          }
          p
        }
      case None => List.empty
    }
  }
}
