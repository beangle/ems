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

package org.beangle.ems.portal.admin.action.bulletin

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings

import scala.collection.mutable

object SensitiveFilter {

  def apply(wordstr: String): SensitiveFilter = {
    apply(Strings.split(wordstr).toSet)
  }

  def apply(words: collection.Set[String]): SensitiveFilter = {
    val dfa = Collections.newMap[Any, Any]
    words foreach { key =>
      var nowMap: mutable.Map[Any, Any] = dfa
      (0 until key.length) foreach { i =>
        val keyChar = key.charAt(i)
        nowMap.get(keyChar) match {
          case None =>
            val newWorMap = Collections.newMap[Any, Any]
            newWorMap.put("isEnd", false)
            nowMap.put(keyChar, newWorMap)
            nowMap = newWorMap
          case Some(m) =>
            nowMap = m.asInstanceOf[mutable.Map[Any, Any]]
        }
        if (i == key.length() - 1) {
          nowMap.put("isEnd", true)
        }
      }
    }
    new SensitiveFilter(dfa)
  }
}

class SensitiveFilter(val dfa: mutable.Map[Any, Any], val minMatch: Boolean = true) {

  def matched(txt: String): Boolean = {
    val chars = txt.toCharArray
    chars.indices.exists(i => find(chars, i) > 0)
  }

  def matchedWords(txt: String): Set[String] = {
    val result = Collections.newSet[String]
    val chars = txt.toCharArray
    var i = 0
    while (i < chars.length) {
      val len = find(chars, i)
      if (len > 0) {
        result += txt.substring(i, i + len)
        i = i + len
      } else {
        i += 1
      }
    }
    result.toSet
  }

  /** 从指定位置查找匹配的长度
   * @param txt        目标字符串
   * @param beginIndex 起始位置
   * @return length if exists
   */
  private[this] def find(txt: Array[Char], beginIndex: Int): Int = {
    var matched = false
    var len = 0
    var nowMap = dfa

    var breaked = false
    for (i <- beginIndex until txt.length if !breaked) {
      nowMap.get(txt(i)) match {
        case None => breaked = true
        case Some(m) =>
          nowMap = m.asInstanceOf[mutable.Map[Any, Any]]
          len += 1
          if (true == nowMap("isEnd")) {
            matched = true
            if (minMatch) {
              breaked = true
            }
          }
      }
    }

    if (len < 2 || !matched) {
      len = 0
    }
    len
  }
}
