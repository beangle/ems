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
import org.beangle.commons.io.Dirs
import org.beangle.commons.lang.{Charsets, Strings}
import org.beangle.commons.net.Networks
import org.beangle.commons.net.http.{HttpMethods, HttpUtils, Https, Response}

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
    builder.setLevel(event.level.ordinal + 1) //level is 1 based
    builder.setAppName(event.appName)
    val os = new ByteArrayOutputStream()
    builder.build().writeTo(os)
    val upload = Networks.url(url.replace("{level}", Strings.uncapitalize(event.level.toString)))
    HttpUtils.invoke(upload, os.toByteArray, "application/x-protobuf", None)
  }
}
