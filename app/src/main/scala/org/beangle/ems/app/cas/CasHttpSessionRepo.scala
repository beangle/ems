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

package org.beangle.ems.app.cas

import org.beangle.commons.cache.CacheManager
import org.beangle.security.session.http.HttpSessionRepo
import org.beangle.security.realm.cas.CasConfig
import org.beangle.commons.io.BinarySerializer
import org.beangle.commons.lang.reflect.BeanInfos
import org.beangle.security.session.jdbc.DBSessionRegistry

class CasHttpSessionRepo(casConfig: CasConfig, cacheManager: CacheManager, serializer: BinarySerializer)
    extends HttpSessionRepo(cacheManager, serializer) {
  this.geturl = casConfig.casServer + "/session/{id}?format=" + serializer.mediaTypes.head.toString
  this.accessUrl = casConfig.casServer + "/session/{id}/access?time={time}"
  this.findUrl =  casConfig.casServer + "/session/{principal}/ids"
  this.expireUrl =  casConfig.casServer + "/session/{id}/expire"
}

object CasHttpSessionRepo {
  def main(args: Array[String]): Unit = {
    println("dd")
    println(BeanInfos.of(classOf[DBSessionRegistry]))
  }
}
