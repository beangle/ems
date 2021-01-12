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

import com.google.gson.Gson
import org.beangle.commons.lang.Strings

import scala.collection.mutable
import scala.jdk.javaapi.CollectionConverters.asScala

object JSON {
  private val gson = new Gson()

  def parseValue[T](json: String, clazz: Class[T]): T = {
    gson.fromJson(json, clazz)
  }

  def parseObj(json: String): collection.Map[String, Any] = {
    if (Strings.isEmpty(json)) {
      Map.empty
    } else {
      val map = gson.fromJson(json, classOf[java.util.Map[_, _]])
      convert(map).asInstanceOf[collection.Map[String, Any]]
    }
  }

  def parseSeq(json: String): collection.Seq[Any] = {
    if (Strings.isEmpty(json)) {
      List.empty
    } else {
      val list = gson.fromJson(json, classOf[java.util.List[_]])
      convert(list).asInstanceOf[collection.Seq[Any]]
    }
  }

  def convert(value: Any): Any = {
    value match {
      case b: ju.Map[_, _] =>
        val iter = b.entrySet().iterator
        val result = new mutable.HashMap[Any, Any]
        while (iter.hasNext) {
          val one = iter.next
          result.put(one.getKey, convert(one.getValue))
        }
        result
      case l: ju.Collection[_] => asScala(l).map(convert)
      case null => null
      case _ =>
        if (value.getClass.isArray) {
          value.asInstanceOf[Array[_]].map(convert).toList
        } else {
          value
        }
    }
  }
}
