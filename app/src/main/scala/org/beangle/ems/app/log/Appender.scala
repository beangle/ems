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
import org.beangle.commons.codec.digest.Digests
import org.beangle.commons.io.Dirs
import org.beangle.commons.lang.Charsets
import org.beangle.commons.net.Networks
import org.beangle.commons.net.http.HttpUtils
import org.beangle.ems.app.EmsApp

import java.io.*

trait Appender {

  def append(entry: LogEvent): Unit
}

class ConsoleAppender(layout: Layout) extends Appender {
  def append(event: LogEvent): Unit = {
    println(layout.mkString(event))
  }
}

class FileAppender(val fileName: String, layout: Layout) extends Appender, Initializing, Disposable {

  private var fos: FileOutputStream = _

  override def init(): Unit = {
    val file = new File(fileName)
    Dirs.on(file.getParentFile).mkdirs()
    fos = new FileOutputStream(file)
  }

  override def destroy(): Unit = {
    if null != fos then fos.close()
  }

  def append(event: LogEvent): Unit = {
    fos.write(layout.mkString(event).getBytes(Charsets.UTF_8))
  }
}

class RemoteAppender(val url: String) extends Appender {
  def append(event: LogEvent): Unit = {
    event match {
      case be: BusinessLogEvent =>
        val b = Proto.BusinessLogEvent.newBuilder()
        b.setOperator(be.operator)
        b.setOperateAt(be.operateAt.toEpochMilli)
        b.setSummary(be.summary)
        b.setDetails(be.details)
        b.setResources(be.resources)
        b.setIp(be.ip)
        b.setAgent(be.agent)
        b.setEntry(be.entry)
        b.setLevel(be.level.ordinal + 1) //level is 1 based
        b.setAppName(be.appName)
        val os = new ByteArrayOutputStream()
        b.build().writeTo(os)
        val bytes = os.toByteArray
        val upload = Networks.url(url + "?type=business&digest=" + digest(bytes, EmsApp.secret))
        HttpUtils.post(upload, bytes, "application/x-protobuf")
      case ee: ErrorLogEvent =>
        val b = Proto.ErrorLogEvent.newBuilder()
        b.setAppName(ee.appName)
        b.setExceptionName(ee.exceptionName)
        b.setMessage(ee.message)
        b.setOccurredAt(ee.occurredAt.toEpochMilli)
        b.setParams(ee.params.orNull)
        b.setRequestUrl(ee.requestUrl)
        b.setStackTrace(ee.stackTrace)
        b.setUsername(ee.username.orNull)
        val os = new ByteArrayOutputStream()
        b.build().writeTo(os)
        val bytes = os.toByteArray
        val upload = Networks.url(url + "?type=error&digest=" + digest(bytes, EmsApp.secret))
        HttpUtils.post(upload, bytes, "application/x-protobuf")
    }

  }

  private def digest(bytes: Array[Byte], secret: String): String = {
    Digests.md5Hex(Array.concat(secret.getBytes(Charsets.UTF_8), bytes))
  }
}
