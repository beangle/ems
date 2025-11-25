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

package org.beangle.ems.app.log

import org.beangle.commons.lang.Strings

import scala.collection.mutable

trait LogEvent {

  def appName: String
}

object LogEvent {

  def toDetailString(datas: Any): String = {
    datas match {
      case null => "--"
      case m: collection.Map[_, _] =>
        val sb = new mutable.ArrayBuffer[String]
        m foreach { case (k, v) =>
          val key = k.toString
          if !key.startsWith("_") then
            val stringValue = v match {
              case Some(n) => n.toString
              case None => "None"
              case vs: Array[_] => "[" + vs.map(String.valueOf(_)).mkString(",") + "]"
              case vs: Seq[_] => "[" + vs.map(String.valueOf(_)).mkString(",") + "]"
              case _ => String.valueOf(v)
            }
            val value = if k.toString.contains("password") then "*****" else stringValue

            if (Strings.isNotBlank(value)) {
              sb += s"$k = $value"
            }
        }
        val mapString = sb.sorted.mkString("\n")
        Strings.abbreviate(mapString, 4000)
      case e: Any => e.toString
    }
  }
}
