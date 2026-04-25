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

package org.beangle.ems.app.oa

import org.beangle.commons.codec.digest.Digests
import org.beangle.commons.collection.Collections
import org.beangle.commons.net.http.{HttpUtils, Request}
import org.beangle.ems.app.{Ems, EmsApp}

object Sms {

  def send(userName: String, mobile: String, template: Option[String]): String = {
    val url = Ems.innerApi + s"/platform/oa/sms/send/${mobile}"
    val params = Collections.newMap[String, String]
    params.put("name", userName)
    val t = template.getOrElse("").trim
    params.put("appName", EmsApp.name)
    params.put("digest", Digests.md5Hex(Ems.key + s"&appName=${EmsApp.name}&name=${userName}&template=${t}"))

    val res = HttpUtils.post(url, Request.asForm(params))
    res.getText
  }

  def available: Boolean = {
    val url = Ems.innerApi + s"/platform/oa/sms/available"
    HttpUtils.get(url).getText.contains("true")
  }

  def verify(mobile: String, code: String): Boolean = {
    val url = Ems.innerApi + s"/platform/oa/sms/verify/${mobile}/${code}"
    HttpUtils.get(url).getText == "true"
  }
}
