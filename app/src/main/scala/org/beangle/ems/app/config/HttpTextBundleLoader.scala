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

package org.beangle.ems.app.config

import org.beangle.commons.bean.Initializing
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.{Charsets, Strings}
import org.beangle.commons.net.http.HttpUtils
import org.beangle.commons.text.i18n.DefaultTextBundleLoader
import org.beangle.ems.app.{Ems, EmsApp}

import java.io.{ByteArrayInputStream, InputStream}
import java.util.Locale

class HttpTextBundleLoader extends DefaultTextBundleLoader, Initializing {

  private var bundles: Set[String] = Set.empty

  override def init(): Unit = {
    val url = s"${Ems.api}/platform/config/text-bundles/${EmsApp.name}/ls"
    val res = HttpUtils.getText(url)
    if (res.isOk) {
      bundles = Strings.split(res.getText).toSet
    }
  }

  override protected def findExtra(locale: Locale, bundleName: String): collection.Seq[(String, InputStream)] = {
    val path = s"${bundleName.replace('.', '/')}.${locale.toString}"
    if (bundles.contains(path)) {
      val url = s"${Ems.api}/platform/config/text-bundles/${EmsApp.name}/${path}"
      val res = HttpUtils.getText(url)
      if (res.isOk) {
        List((bundleName + "@db", new ByteArrayInputStream(res.getText.getBytes(Charsets.UTF_8))))
      } else {
        List.empty
      }
    } else {
      List.empty
    }
  }

}
