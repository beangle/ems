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

trait ErrorLogger {
  def publish(entry: ErrorLogEvent): Unit
}

import java.time.Instant

class ErrorLogEvent extends LogEvent {
  /** 应用 */
  var appName: String = _
  /** 访问路径 */
  var requestUrl: String = _
  /** 发生时间 */
  var occurredAt: Instant = _
  /** 异常名称 */
  var exceptionName: String = _
  /** 操作内容摘要 */
  var message: String = _
  /** 操作内容 */
  var stackTrace: String = _
  /** 调用上下文 */
  var params: Option[String] = None
  /** 业务操作人 */
  var username: Option[String] = None
}
