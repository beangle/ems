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

package org.beangle.ems.app.web

import org.beangle.commons.lang.Strings
import org.beangle.ems.app.log.{BusinessLogStore, Level}
import org.beangle.web.action.context.ActionContext
import org.beangle.web.servlet.util.RequestUtils

import scala.collection.mutable

trait BusinessLogSupport {
  var businessLogStore: BusinessLogStore = _

  def info(summary: String, resources: Any, details: Any): Unit = {
    log(Level.Info, summary, resources, details)
  }

  def warn(summary: String, resources: Any, details: Any): Unit = {
    log(Level.Warn, summary, resources, details)
  }

  def error(summary: String, resources: Any, details: Any): Unit = {
    log(Level.Error, summary, resources, details)
  }

  def log(level: Level, summary: String, resources: Any, details: Any): Unit = {
    val log = BusinessLogStore.newEntry(summary)
    log.level = level
    val context = ActionContext.current
    val detailStr = details match {
      case null => "--"
      case m: collection.Map[_, _] =>
        val sb = new mutable.ArrayBuffer[String]
        m foreach { case (k, v) =>
          val key = k.toString
          if !key.startsWith("_") then
            val value = if k.toString.contains("password") then "*****" else v.toString
            sb += s"$k = $value"
        }
        val mapString = sb.sorted.mkString("\n")
        Strings.abbreviate(mapString, 4000)
      case e: Any => e.toString
    }
    log.from(RequestUtils.getIpAddr(context.request)).operateOn(resources.toString, detailStr)
    businessLogStore.publish(log)
  }
}
