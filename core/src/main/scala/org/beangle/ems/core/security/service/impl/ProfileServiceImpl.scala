/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2020, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.ems.core.security.service.impl

import org.beangle.commons.bean.Properties
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.app.util.JSON
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.security.model.FuncResource
import org.beangle.ems.core.security.service.ProfileService
import org.beangle.ems.core.user.model.{Dimension, Profile, User, UserProfile}

class ProfileServiceImpl extends ProfileService {

  var domainService: DomainService = _
  var entityDao: EntityDao = _

  override def getProfiles(user: User, resource: FuncResource): collection.Seq[Profile] = {
    Seq.empty
  }

  override def getProfiles(usercode: String): Seq[UserProfile] = {
    val builder = OqlBuilder.from(classOf[UserProfile], "up")
    builder.where("up.user.code=:code and up.user.org=:org and up.domain=:domain",
      usercode, domainService.getOrg, domainService.getDomain)
    entityDao.search(builder)
  }

  def getDimensionValues(field: Dimension, keys: String*): collection.Seq[Any] = {
    val source = field.source
    val keyname = field.keyName.getOrElse("")

    if (source.startsWith("json:")) {
      val json = source.substring(5)
      JSON.parseSeq(json).filter { x => Properties.get(x, keyname) }
    } else if (source.startsWith("csv:")) {
      val csv = source.substring(4)
      val lines = Strings.split(Strings.replace(csv, "\r", ""), "\n")
      val start = lines.indices find (x => Strings.isNotBlank(lines(x)))
      val heads = Strings.split(lines(start.get), ",")
      val data = Collections.newBuffer[org.beangle.commons.collection.Properties]
      var i = start.get + 1
      while (i < lines.length) {
        if (!Strings.isBlank(lines(i))) {
          val datas = Strings.split(lines(i), ",")
          val p = new org.beangle.commons.collection.Properties
          for (j <- heads.indices) {
            p.put(heads(j), datas(j))
          }
          p.get(keyname) foreach { id =>
            if (keys.isEmpty || keys.contains("*") || keys.contains(id)) {
              data += p
            }
          }
        }
        i += 1
      }
      data
    } else {
      Seq.empty
    }
  }

  def getDimension(fieldName: String): Dimension = {
    null
  }

  def get(id: java.lang.Long): Profile = {
    null
  }
}
