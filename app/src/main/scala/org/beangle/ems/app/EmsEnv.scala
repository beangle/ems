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

import org.beangle.commons.io.IOs
import org.beangle.commons.lang.SystemInfo
import org.beangle.commons.logging.Logging

import java.io.File

object EmsEnv extends Logging {

  def findHome(): String = {
    SystemInfo.properties.get("ems.base") match {
      case Some(base) => base
      case None =>
        val home = SystemInfo.properties.getOrElse("ems.home", SystemInfo.user.home + s"/.ems")
        SystemInfo.properties.get("ems.profile") match {
          case Some(p) => home + "/" + p
          case None => home
        }
    }
  }

  def readConfig(location: String): Map[String, String] = {
    try {
      val configFile = new File(location)
      if (!configFile.exists) {
        Map.empty
      } else {
        IOs.readJavaProperties(configFile.toURI.toURL)
      }
    } catch {
      case e: Throwable => logger.error("Read config error", e); Map.empty
    }
  }
}

final class EmsEnv(val home: String, val properties: Map[String, String]) extends Logging {

  val base = readBase("base", null)

  val cas = readBase("cas", "{base}/cas")

  val portal = readBase("portal", "{base}/portal")

  val api = readBase("api", "{base}/api")

  var blob = readBase("blob", "{base}/blob")

  val webapp = readBase("webapp", "{base}")

  val static = readBase("static", "{base}/static")

  val key = readKey()

  private def readBase(property: String, defaults: String): String = {
    var result =
      properties.get(property) match {
        case Some(v) => v
        case None =>
          if ("base" == property) {
            logger.warn("Cannot find base,using localhost/base")
            "localhost/base"
          } else {
            defaults.replace("{base}", this.base)
          }
      }
    if (result.endsWith("/")) result = result.substring(0, result.length - 1)
    if (result.startsWith("http")) result else "http://" + result
  }

  private def readKey(): String = {
    properties.getOrElse("key", "ems")
  }

}
