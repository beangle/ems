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

package org.beangle.ems.core.job

import org.beangle.data.model.LongId
import org.beangle.data.model.pojo.Named
import org.beangle.ems.core.config.model.Domain

import java.time.Instant
import java.time.Duration
import org.beangle.data.model.pojo.Updated

/** 计划任务
 *
 */
class CronJob extends LongId, Named,Updated {
  /** 所属域 */
  var domain: Domain = _
  /** 目标（执行目标类或地址） */
  var target: String = _
  /** 描述 */
  var description: String = _
  /** 任务内容 */
  var contents: String = _
  /** Cron 表达式 */
  var expression: String = _
  /** 上次执行时间 */
  var lastExecuteAt: Option[Instant] = None
  /** 上次执行耗时（毫秒） */
  var duration: Option[Duration] = None
  /** 状态码 */
  var statusCode: Option[Int] = None
  /** 是否启用 */
  var enabled: Boolean = _
}
