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

import org.beangle.commons.codec.digest.Digests
import org.beangle.commons.logging.Logging
import org.beangle.commons.net.Networks
import org.beangle.commons.net.http.{HttpUtils, Request}

import java.io.*
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RemoteRepository(val base: String, val dir: String, user: String, key: String) extends Repository with Logging {

  private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
  require(!dir.endsWith("/"))

  override def remove(path: String): Boolean = {
    require(path.startsWith("/"))
    val load = Request.asJson("{}").auth(user, key)
    val response = HttpUtils.delete(Networks.url(s"$base$dir${path}"), load)
    if (response.status >= 300) {
      throw new Exception("Remove Failed,Response is " + response.getText)
    } else {
      response.status == 200
    }
  }

  override def path(p: String): Option[String] = {
    require(p.startsWith("/"))
    Some(s"$base$dir${p}")
  }

  override def url(path: String): Option[URL] = {
    require(path.startsWith("/"))
    val now = LocalDateTime.now.format(formatter)
    val token = Digests.sha1Hex(s"$dir${path}$user$key$now")
    Some(Networks.url(s"$base$dir${path}?token=$token&u=$user&t=$now"))
  }

  override def upload(folder: String, is: InputStream, fileName: String, owner: String): BlobMeta = {
    require(folder.startsWith("/"))
    val folderUrl = if (folder.endsWith("/")) folder else folder + "/"
    val params = Map("owner" -> owner, "file" -> (fileName, is))
    val response = HttpUtils.post(Networks.url(s"$base$dir$folderUrl"), Request.asForm(params).auth(user, key))
    if (response.status == 200) {
      BlobMeta.fromJson(response.getText)
    } else {
      throw new RuntimeException("Upload failed,response code is " + response.status + " and response body:" + response.getText)
    }
  }

}
