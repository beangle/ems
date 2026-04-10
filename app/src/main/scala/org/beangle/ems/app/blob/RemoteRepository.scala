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
import org.beangle.commons.json.{Json, JsonObject}
import org.beangle.commons.lang.Charsets
import org.beangle.commons.net.Networks
import org.beangle.commons.net.http.{HttpUtils, Request}
import org.beangle.ems.app.Ems

import java.io.*
import java.net.URI
import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}
import scala.concurrent.Future

class RemoteRepository(val base: String, val dir: String, user: String, key: String) extends Repository {

  private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
  require(!dir.endsWith("/"))

  override def remove(path: String): Boolean = {
    require(path.startsWith("/"))
    val load = Request.asJson("{}").bearer(Ems.key)
    val response = HttpUtils.delete(url(path), load)
    if (response.status >= 500) {
      throw new Exception("Remove Failed,Response is " + response.getText)
    } else {
      val removed = response.status == 200
      import scala.concurrent.ExecutionContext.Implicits.global
      Future {
        val jb = new JsonObject
        jb.add("profile", dir)
        jb.add("appName", user)
        jb.add("filePath", path)
        val body = jb.toString
        val digests = Digests.md5Hex(key + body)
        val notifyUrl = Ems.innerApi + "/platform/blob/files/unregister?digest=" + digests
        HttpUtils.post(notifyUrl, Request.asJson(body))
      }
      removed
    }
  }

  override def uri(path: String): URI = {
    require(path.startsWith("/"))
    val now = LocalDateTime.now(ZoneId.of("UTC")).format(formatter)
    val token = Digests.sha1Hex(s"/${Ems.profile}$dir$path$key$now")
    Networks.uri(url(s"${path}?token=$token&t=$now"))
  }

  override def upload(folder: String, is: InputStream, fileName: String, owner: String): BlobMeta = {
    require(folder.startsWith("/"))
    val folderUrl = if (folder.endsWith("/")) folder else folder + "/"
    val params = Map("owner" -> owner, "file" -> (fileName, is))
    val response = HttpUtils.post(url(folderUrl), Request.asForm(params).bearer(Ems.key))
    if (response.status == 200) {
      val res = response.getText
      val meta = BlobMeta.fromJson(res)
      meta.filePath = subdir(meta.filePath)
      import scala.concurrent.ExecutionContext.Implicits.global
      Future {
        val jb = Json.parseObject(res)
        jb.add("owner", owner)
        jb.add("profile", dir)
        jb.add("appName", user)
        jb.add("filePath", meta.filePath)
        val body = jb.toString
        val digests = Digests.md5Hex(key + body)
        val notifyUrl = Ems.innerApi + "/platform/blob/files/register?digest=" + digests
        HttpUtils.post(notifyUrl, Request.asJson(body))
      }
      meta
    } else {
      throw new RuntimeException("Upload failed,response code is " + response.status + " and response body:" + response.getText)
    }
  }

  private def url(path: String): String = {
    s"${base}/${Ems.profile}${dir}${path}"
  }

  private def subdir(fullPath: String): String = {
    val bucketAndDir = s"/${Ems.profile}${dir}"
    if (fullPath.startsWith(bucketAndDir)) {
      fullPath.substring(bucketAndDir.length)
    } else if(fullPath.startsWith (dir)) {
      fullPath.substring(dir.length)
    }else {
      fullPath
    }
  }

  private def digest(bytes: Array[Byte], secret: String): String = {
    Digests.md5Hex(Array.concat(secret.getBytes(Charsets.UTF_8), bytes))
  }
}
