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

import org.beangle.commons.lang.{Chars, Strings}
import org.beangle.ems.app.EmsApp

import java.time.format.DateTimeFormatter
import scala.collection.mutable

trait Layout {
  def mkString(entry: BusinessLogEntry): String
}

case class Part(content: String, isVariable: Boolean)

class PatternLayout(pattern: String) extends Layout {
  private val parts = parse(pattern)

  def parse(pattern: String): List[Part] = {
    val keys = Set("app", "operator", "summary", "details", "resources", "ip", "agent", "entry", "operateAt")
    var index = 0
    var prevIndex = 0
    var colonIndex = pattern.indexOf('%', index)
    val parts = new mutable.ArrayBuffer[Part]
    while (index < pattern.length && colonIndex > -1) {
      index = colonIndex + 1
      while (index < pattern.length && Chars.isAsciiAlpha(pattern.charAt(index))) {
        index += 1
      }
      if (colonIndex - 1 >= prevIndex) {
        parts.addOne(Part(pattern.substring(prevIndex, colonIndex), false))
      }
      val paramName = pattern.substring(colonIndex + 1, index)
      if !keys.contains(paramName) then parts.addOne(Part(paramName, false))
      else parts.addOne(Part(paramName, true))
      prevIndex = index
      colonIndex = pattern.indexOf('%', index)
    }
    parts.toList
  }

  override def mkString(entry: BusinessLogEntry): String = {
    val result = new mutable.StringBuilder
    parts foreach { p =>
      if p.isVariable then
        val v = p.content match {
          case "app" => EmsApp.name
          case "operator" => entry.operator
          case "summary" => entry.summary
          case "details" => entry.details
          case "resources" => entry.resources
          case "ip" => entry.ip
          case "agent" => entry.agent
          case "entry" => entry.entry
          case "operateAt" => DateTimeFormatter.ISO_INSTANT.format(entry.operateAt)
        }
        result.append(v)
      else result.append(p.content)
    }
    result.mkString
  }
}
