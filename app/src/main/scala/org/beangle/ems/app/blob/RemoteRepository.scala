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
package org.beangle.ems.app.blob

import java.io._
import java.lang.reflect.Type
import java.net.{HttpURLConnection, URL}
import java.nio.charset.StandardCharsets
import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneId}
import java.util.Base64

import com.google.gson.{JsonDeserializationContext, JsonDeserializer}
import org.beangle.commons.codec.digest.Digests
import org.beangle.commons.conversion.Converter
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.Strings.isEmpty
import org.beangle.commons.net.http.{HttpMethods, Https}

class RemoteRepository(val base: String, val dir: String, user: String, key: String) extends Repository {

  private var formater = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
  require(!dir.endsWith("/"))

  override def remove(path: String): Boolean = {
    require(path.startsWith("/"))
    val url = new URL(s"$base$dir${path}")
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    Https.noverify(conn)
    conn.setDoOutput(true)
    conn.setRequestMethod(HttpMethods.DELETE)
    val encoded = Base64.getEncoder.encodeToString((s"${user}:${key}").getBytes(StandardCharsets.UTF_8))
    conn.setRequestProperty("Authorization", s"Basic $encoded")
    if (conn.getResponseCode >= 300) {
      throw new Exception("Remove Failed,Response code is " + conn.getResponseCode)
    } else {
      conn.getResponseCode == 200
    }
  }

  override def path(p: String): Option[String] = {
    require(p.startsWith("/"))
    Some(s"$base$dir${p}")
  }

  override def url(path: String): Option[URL] = {
    require(path.startsWith("/"))
    val now = LocalDateTime.now.format(formater)
    val token = Digests.sha1Hex(s"$dir${path}$user$key$now")
    Some(new URL(s"$base$dir${path}?token=$token&u=$user&t=$now"))
  }

  override def upload(folder: String, is: InputStream, fileName: String, owner: String): BlobMeta = {
    require(folder.startsWith("/"))
    val folderUrl = if (folder.endsWith("/")) folder else folder + "/"
    val target = new URL(s"$base$dir$folderUrl")
    val params = Map("owner" -> owner)
    doUpload(target, is, fileName, params, Some(s"$user:$key"))
  }

  import java.nio.charset.StandardCharsets
  import java.util.Base64

  private def doUpload(url: URL, is: InputStream, fileName: String,
                       params: Map[String, String], basicAuth: Option[String]): BlobMeta = {
    val end = "\r\n"
    val twoHyphens = "--"
    val boundary = "*****";
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    Https.noverify(conn)
    conn.setDoOutput(true)
    conn.setDoInput(true)
    conn.setRequestMethod(HttpMethods.POST)
    conn.setRequestProperty("Content-Type", s"multipart/form-data;boundary=$boundary")
    conn.setRequestProperty("Connection", "Keep-Alive")
    basicAuth foreach { auth =>
      val encoded = Base64.getEncoder.encodeToString((auth).getBytes(StandardCharsets.UTF_8))
      conn.setRequestProperty("Authorization", s"Basic $encoded")
    }
    val os = conn.getOutputStream
    val ds = new DataOutputStream(os)
    val text = new StringBuilder
    params foreach { case (k, v) =>
      text.append(twoHyphens).append(boundary).append(end)
      text.append(s"""Content-Disposition: form-data; name="${k}"""")
      text.append(end).append(end)
      text.append(v).append(end);
    }
    ds.write(text.toString().getBytes())

    val strBuf = new StringBuilder
    strBuf.append(twoHyphens).append(boundary).append(end)
    strBuf.append(s"""Content-Disposition: form-data; name="file"; filename="${fileName}"""")
    strBuf.append(end).append(end)
    ds.write(strBuf.toString().getBytes())
    IOs.copy(is, ds)
    ds.writeBytes(end)
    ds.writeBytes(twoHyphens + boundary + twoHyphens + end)
    ds.flush()
    ds.close()
    os.close() //don't forget to close the OutputStream
    if (conn.getResponseCode != 200) {
      println(IOs.readString(conn.getInputStream))
      throw new Exception("Upload Failed,Response code is " + conn.getResponseCode)
    } else {
      val response = IOs.readString(conn.getInputStream)
      import com.google.gson.GsonBuilder
      val gson = new GsonBuilder().registerTypeAdapter(classOf[Instant], new InstantAdapter).create
      gson.fromJson(response, classOf[BlobMeta])
    }
  }

  import com.google.gson.JsonElement

  class InstantAdapter extends JsonDeserializer[Instant] {
    def deserialize(elem: JsonElement, `type`: Type, context: JsonDeserializationContext): Instant = {
      InstantConverter(elem.getAsString)
    }
  }

  object InstantConverter extends Converter[String, Instant] {
    override def apply(value: String): Instant = {
      if (isEmpty(value)) return null
      if (!value.endsWith("Z")) {
        LocalDateTime.parse(normalize(value)).atZone(ZoneId.systemDefault).toInstant
      } else {
        Instant.parse(value)
      }
    }
  }

  /**
   * Change DateTime Format
   * 1. YYYY-MM-DD HH:mm into YYYY-MM-DDTHH:mm:00
   * 2. YYYY-MM-DD HH:mm:ss into YYYY-MM-DDTHH:mm:ss
   */
  def normalize(value: String): String = {
    val v = if (value.length == 16) value + ":00" else value
    Strings.replace(v, " ", "T")
  }
}
