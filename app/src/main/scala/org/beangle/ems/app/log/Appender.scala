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

package org.beangle.ems.app.log

import org.beangle.commons.bean.{Disposable, Initializing}
import org.beangle.commons.io.{Dirs, IOs}
import org.beangle.commons.lang.{Charsets, Strings}
import org.beangle.commons.net.http.HttpUtils.report
import org.beangle.commons.net.http.{HttpMethods, Https, Response}

import java.io.*
import java.net.HttpURLConnection.HTTP_NOT_FOUND
import java.net.{HttpURLConnection, URL, URLConnection}

trait Appender {

  def append(entry: BusinessLogEvent): Unit
}

class ConsoleAppender(layout: Layout) extends Appender {
  def append(entry: BusinessLogEvent): Unit = {
    println(layout.mkString(entry))
  }
}

class FileAppender(val fileName: String, layout: Layout) extends Appender with Initializing with Disposable {

  private var fos: FileOutputStream = _

  override def init(): Unit = {
    val file = new File(fileName)
    Dirs.on(file.getParentFile).mkdirs()
    fos = new FileOutputStream(file)
  }

  override def destroy(): Unit = {
    if null != fos then fos.close()
  }

  def append(entry: BusinessLogEvent): Unit = {
    fos.write(layout.mkString(entry).getBytes(Charsets.UTF_8))
  }
}

class RemoteAppender(val url: String) extends Appender {
  def append(event: BusinessLogEvent): Unit = {
    val builder = BusinessLogProto.BusinessLogEvent.newBuilder()
    builder.setOperator(event.operator)
    builder.setOperateAt(event.operateAt.toEpochMilli)
    builder.setSummary(event.summary)
    builder.setDetails(event.details)
    builder.setResources(event.resources)
    builder.setIp(event.ip)
    builder.setAgent(event.agent)
    builder.setEntry(event.entry)
    val os = new ByteArrayOutputStream()
    builder.build().writeTo(os)
    val upload = new URL(url.replace("{level}", Strings.uncapitalize(event.level.toString)))
    invoke(upload, os.toByteArray, "application/x-protobuf", None)
  }

  private def invoke(url: URL, body: Array[Byte], contentType: String, f: Option[(URLConnection) => Unit]): Response = {
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    Https.noverify(conn)
    conn.setDoOutput(true)
    conn.setRequestMethod(HttpMethods.POST)
    conn.setRequestProperty("Content-Type", contentType)
    f foreach (x => x(conn))
    val os = conn.getOutputStream
    os.write(body)
    os.flush()
    os.close() //don't forget to close the OutputStream
    try {
      conn.connect()
      //read the inputstream and print it
      //val lines = IOs.readString(conn.getInputStream)
      Response(conn.getResponseCode, "")
    } catch {
      case e: Exception =>
        e.printStackTrace()
        Response(HTTP_NOT_FOUND, conn.getResponseMessage)
    } finally
      if (null != conn) conn.disconnect()
  }

}
