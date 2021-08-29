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

package org.beangle.ems.core.user.service.impl

import org.beangle.commons.bean.Properties
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings
import org.beangle.ems.core.user.model.Dimension
import org.beangle.ems.core.user.service.DataResolver

object CsvDataResolver extends DataResolver {

  def marshal(field: Dimension, items: Seq[Any]): String = {
    if (null == items || items.isEmpty) return ""
    val properties = new collection.mutable.ListBuffer[String]
    field.keyName foreach (properties += _)
    field.properties foreach (x => properties ++= Strings.split(x, ","))
    val sb = new StringBuilder()
    if (properties.isEmpty) {
      for (obj <- items) if (null != obj) sb.append(String.valueOf(obj)).append(',')
    } else {
      for (prop <- properties) sb.append(prop).append(';')
      sb.deleteCharAt(sb.length() - 1).append(',')

      for (obj <- items) {
        for (prop <- properties) {
          try {
            val value: Any = Properties.get(obj, prop)
            sb.append(String.valueOf(value)).append(';')
          } catch {
            case e: Exception => e.printStackTrace()
          }
        }
        sb.deleteCharAt(sb.length() - 1)
        sb.append(',')
      }
    }
    if (sb.nonEmpty) sb.deleteCharAt(sb.length() - 1)
    sb.toString()
  }

  def unmarshal(field: Dimension, source: String): collection.Seq[Map[String, String]] = {
    if (Strings.isEmpty(source)) return List.empty

    val properties = new collection.mutable.ListBuffer[String]
    field.keyName foreach (properties += _)
    field.properties foreach (x => properties ++= Strings.split(x, ","))
    val rs = new collection.mutable.ListBuffer[Map[String, String]]
    if (properties.isEmpty) {
      val datas = Strings.split(source, ",")
      for (data <- datas) rs += Map(field.keyName.get -> data)
    } else {
      properties.clear()
      var startIndex = 1
      val datas = Strings.split(source, ",")
      var names = Array(field.keyName.get)
      names = Strings.split(datas(0), ",")
      properties ++= names
      (startIndex until datas.length) foreach { i =>
        val obj = Collections.newMap[String, String]
        val dataItems = Strings.split(datas(i), ";")
        properties.indices foreach { j =>
          obj.put(properties(j), dataItems(j))
        }
        rs += obj.toMap
      }
    }
    rs
  }
}
