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

package org.beangle.ems.app.blob

import java.io.{File, InputStream}
import java.net.{URI, URL}

import org.beangle.commons.lang.Strings

trait Repository {

  final def remove(folder: String, sha: String): Boolean = {
    remove(getPath(folder,sha))
  }

  def remove(p: String): Boolean

  def path(path: String): Option[String]

  def uri(path: String): Option[URI]

  def upload(folder: String, file: InputStream,fileName:String,owner:String): BlobMeta

  private[blob] def getPath(folder: String, name: String): String = {
    if (folder == "/") {
      s"/${name}"
    } else {
      s"$folder/${name}"
    }
  }

  private[blob] def getExt(fileName: String): String = {
    Strings.substringAfterLast(fileName, ".")
  }

}
