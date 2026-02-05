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

package org.beangle.ems.app.cache

import org.beangle.commons.collection.Collections
import org.beangle.commons.net.http.HttpUtils
import org.beangle.commons.xml.{Document, Node}
import org.beangle.ems.app.{EmsApi, EmsApp}

import java.io.FileInputStream

object Redis {

  private var properties: Map[String, String] = _

  def available(): Boolean = {
    conf.nonEmpty
  }

  def conf: Map[String, String] = {
    if null == properties then properties = loadConf()
    properties
  }

  /** Load redis conf from local file,then remote url
   *
   * @return
   */
  private def loadConf(): Map[String, String] = {
    var elem: Node = null
    EmsApp.getAppFile foreach { file =>
      val is = new FileInputStream(file)
      (Document.parse(is) \\ "redis") foreach { e => elem = e }
    }
    if (null == elem) {
      val res = HttpUtils.get(EmsApi.getRedisUrl)
      if (res.isOk) {
        (Document.parse(res.getText) \\ "redis") foreach { e => elem = e }
      }
    }

    if (null != elem) {
      val props = Collections.newMap[String, String]
      elem \ "_" foreach { n =>
        props.put(n.label, n.text.trim())
      }
      props.toMap
    } else {
      Map.empty
    }
  }

}
