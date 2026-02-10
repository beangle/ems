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

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings
import org.beangle.commons.net.http.{HttpUtils, Request}
import org.beangle.ems.app.{Ems, EmsApp}

object Sms {

  def send(name: String, mobile: String, template: String): String = {
    val url = Ems.innerApi + s"/platform/oa/sms/send/${mobile}"
    val params = Collections.newMap[String, String]
    params.put("name", name)
    if (Strings.isNotBlank(template)) {
      params.put("template", template)
    }
    params.put("appName", EmsApp.name)
    params.put("secret", EmsApp.secret)

    val res = HttpUtils.post(url, Request.asForm(params))
    res.getText
  }

  def verify(mobile: String, code: String): Boolean = {
    val url = Ems.innerApi + s"/platform/oa/sms/verify/${mobile}/${code}"
    HttpUtils.get(url).getText == "true"
  }
}
