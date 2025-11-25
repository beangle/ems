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

import org.beangle.ems.app.EmsApp
import org.beangle.security.Securities
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.dispatch.DefaultExceptionHandler

import java.time.{LocalDateTime, ZoneId}

class LogExceptionHandler extends DefaultExceptionHandler {

  var errorLogger: ErrorLogger = _

  override protected def report(attrs: collection.Map[String, Any], ex: Exception): Unit = {
    val message = attrs.getOrElse("message", null).asInstanceOf[String] // 异常消息
    val path = attrs.getOrElse("path", null).asInstanceOf[String] // 请求路径
    val timestamp = attrs.get("timestamp").orNull.asInstanceOf[LocalDateTime].atZone(ZoneId.systemDefault()).toInstant // 时间戳
    val exception = attrs.getOrElse("exception", null).asInstanceOf[String] // 异常类名
    val trace = attrs.getOrElse("trace", null).asInstanceOf[Array[String]] // 堆栈信息（开发模式有）

    if (null != trace && trace.length > 0) {
      val e = new ErrorLogEvent
      e.appName = EmsApp.name
      e.requestUrl = path
      e.exceptionName = exception
      e.stackTrace = trace.mkString("\n")
      e.message = message
      e.occurredAt = timestamp
      e.username = Securities.session.map(_.principal.getName)
      e.params = Some(LogEvent.toDetailString(ActionContext.current.params))
      errorLogger.publish(e)
    }
  }
}
