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

import org.beangle.commons.io.Files./
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.SystemInfo
import org.beangle.commons.logging.Logging

import java.io.File

object EmsEnv extends Logging {

  def findHome(): String = {
    SystemInfo.properties.get("ems.base") match {
      case Some(base) => base
      case None =>
        val home = SystemInfo.properties.getOrElse("ems.home", SystemInfo.user.home + / + ".ems")
        SystemInfo.properties.get("ems.profile") match {
          case Some(p) => home + / + p
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

  def readEnv(home: String, base: String, properties: Map[String, String]): EmsEnv = {
    val reader = new PropertyReader(base, properties)
    val cas = reader.find("cas", "{base}/cas")
    val portal = reader.find("portal", "{base}/portal")
    val index = reader.find("index", "{base}/portal")
    val api = reader.find("api", "{base}/api")
    val blob = reader.find("blob", "{base}/blob")
    val webapp = reader.find("webapp", "{base}")
    val static = reader.find("static", "{base}/static")
    val key = reader.readKey()
    EmsEnv(home, base, cas, portal, index, api, blob, webapp, static, key, properties)
  }

  def apply(home: String, properties: Map[String, String]): EmsEnv = {
    readEnv(home, readBase(properties, "name"), properties)
  }

  def inner(env: EmsEnv): EmsEnv = {
    env.properties.get("inner_base") match {
      case None => env
      case Some(b) =>
        val base = normalize(b)
        readEnv(env.home, base, env.properties)
    }
  }

  private def readBase(properties: Map[String, String], name: String): String = {
    properties.get(name) match {
      case None =>
        if (name == "base") logger.warn("Cannot find base,using localhost:8080")
        "localhost:8080"
      case Some(base) => normalize(base)
    }
  }

  private def normalize(value: String): String = {
    var url = value
    if (url.endsWith("/")) url = url.substring(0, url.length - 1)
    if (url.startsWith("http")) url else "http://" + url
  }

}

class PropertyReader(base: String, properties: Map[String, String]) extends Logging {
  def find(property: String, defaults: String): String = {
    var result = properties.get(property) match {
      case Some(v) => v
      case None =>
        if ("base" == property) {
          logger.warn("Cannot find base,using localhost:8080")
          "localhost:8080"
        } else {
          defaults.replace("{base}", this.base)
        }
    }
    if (result.endsWith("/")) result = result.substring(0, result.length - 1)
    if (result.startsWith("http")) result else "http://" + result
  }

  def readKey(): String = {
    properties.getOrElse("key", "ems")
  }
}

case class EmsEnv(home: String, base: String, cas: String, portal: String,
                  index: String, api: String, blob: String, webapp: String,
                  static: String, key: String, properties: Map[String, String]) extends Logging {
}
