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

package org.beangle.ems.core.log.model

import org.beangle.data.model.LongId
import org.beangle.data.model.annotation.log
import org.beangle.ems.app.log.Level
import org.beangle.ems.core.config.model.App

import java.time.Instant

/** 业务日志
 */
@log
class BusinessLog extends AppLogEntry {
  /** 操作人 */
  var operator: String = _
  /** 操作时间 */
  var operateAt: Instant = _
  /** 操作内容摘要 */
  var summary: String = _
  /** 操作内容 */
  var details: String = _
  /** 对应的资源 */
  var resources: String = _
  /** IP */
  var ip: String = _
  /** 操作客户端代理 */
  var agent: String = _
  /** 访问入口 */
  var entry: String = _
  /** 级别 */
  var logLevel: Level = Level.Info

  override def message: String = summary

  override def occurredAt: Instant = operateAt

  override def requestUrl: String = entry

}
