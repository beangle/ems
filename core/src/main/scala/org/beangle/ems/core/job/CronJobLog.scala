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

import java.time.Instant
import java.time.Duration

/** 计划任务执行日志
 */
class CronJobLog extends LongId {
  /** 关联任务 */
  var job: CronJob = _
  /** 执行时间 */
  var executeAt: Instant = _
  /** 执行耗时 */
  var duration: Duration = _
  /** 状态码（0-成功 1-失败 2-运行中） */
  var statusCode: Int = _
  /** 结果文件路径 */
  var resultFilePath: String  = _
}
