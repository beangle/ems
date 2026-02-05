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

import org.beangle.commons.lang.Strings
import org.beangle.commons.logging.Logging

import java.io.File

object Ems {

  val home: String = EmsEnv.findHome()

  val env: EmsEnv = EmsEnv(home, EmsEnv.readConfig(home + "/conf.properties"))

  private val innerEnv = EmsEnv.inner(env)

  var sid: Sid = Sid(env.properties)

  def hostname: String = {
    val h = Strings.substringAfter(base, "://")
    if (h.contains("/")) {
      Strings.substringBefore(h, "/")
    } else {
      h
    }
  }

  def base: String = {
    env.base
  }

  def blob: String = {
    env.blob
  }

  def webapp: String = {
    env.webapp
  }

  def static: String = {
    env.static
  }

  def key: String = {
    env.key
  }

  def properties: Map[String, String] = {
    env.properties
  }

  def isPlatform(contextPath: String): Boolean = {
    cas.endsWith(contextPath) || (api + "/platform").endsWith(contextPath) || portal.endsWith(contextPath)
  }

  def cas: String = env.cas

  def portal: String = env.portal

  def index: String = env.index

  def api: String = {
    env.api
  }

  def innerApi: String = {
    innerEnv.api
  }

  def getResourceFile: Option[File] = {
    val resfile = new File(home + "/resources.xml")
    if (resfile.exists) Some(resfile) else None
  }

  class Org {
    var id: Int = _
    var code: String = _
    var name: String = _
    var shortName: String = _
    var logoUrl: String = _
    var wwwUrl: String = _
  }

  class Domain {
    var id: Int = _
    var name: String = _
    var title: String = _
    var logoUrl: String = _
    var org: Org = _
  }

  case class Theme(primaryColor: String, navbarBgColor: String, searchBgColor: String, gridbarBgColor: String, gridBorderColor: String)

  case class Sid(name: String, prefix: String)

  object Sid {
    def apply(properties: Map[String, String]): Sid = {
      val name = properties.getOrElse("session_id_name", "EMS_SID")
      val prefix = properties.getOrElse("session_id_prefix", "EMS-")
      Sid(name, prefix)
    }
  }
}
