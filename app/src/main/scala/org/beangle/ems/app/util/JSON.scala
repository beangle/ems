/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2020, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.ems.app.util

import java.{util => ju}

import javax.script.ScriptEngineManager
import org.beangle.commons.collection.Properties
import org.beangle.commons.lang.Strings

import scala.jdk.javaapi.CollectionConverters.asScala

//FIXME generalize it.
object JSON {
  def parse(string: String): Any = {
    if (Strings.isBlank(string) || "{}".equals(string)) {
      return Map.empty[String, Any]
    }
    val sem = new ScriptEngineManager
    val engine = sem.getEngineByName("javascript")
    engine.eval("result =" + string) match {
      case d: String => d
      case n: Number => n
      case b: ju.Map[_, _] =>
        if (isArray(string)) {
          asScala(b.values).map { x => convert(x.asInstanceOf[Object]) }
        } else {
          val iter = b.entrySet().iterator()
          val result = new Properties
          while (iter.hasNext) {
            val one = iter.next
            result.put(one.getKey.toString, convert(one.getValue.asInstanceOf[Object]))
          }
          result.toMap
        }
      case l: ju.Collection[_] => asScala(l)
    }
  }

  def convert(value: Object): Object = {
    value match {
      case d: String => d
      case n: Number => n
      case b: ju.Map[_, _] =>
        val iter = b.entrySet().iterator()
        val signature = b.toString
        if (signature.contains("Array")) {
          val result = new collection.mutable.ArrayBuffer[Any]
          while (iter.hasNext) {
            val one = iter.next
            result += convert(one.getValue.asInstanceOf[Object])
          }
          result
        } else {
          val result = new Properties
          while (iter.hasNext) {
            val one = iter.next
            result.put(one.getKey.toString, convert(one.getValue.asInstanceOf[Object]))
          }
          result.toMap
        }
      case l: ju.Collection[_] => asScala(l)
      case _ => value
    }
  }

  def isArray(str: String): Boolean = {
    var i = 0
    while (i < str.length) {
      val c = str.charAt(i)
      if (!Character.isWhitespace(c)) {
        return c == '['
      }
      i += 1
    }
    false
  }
}
