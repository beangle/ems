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

package org.beangle.ems.core.oa.model

import org.beangle.commons.collection.Collections
import org.beangle.data.model.LongId
import org.beangle.data.model.pojo.{Named, Remark}
import org.beangle.ems.core.user.model.Group

import scala.collection.mutable

/** 工作流定义中的活动
 */
class FlowActivity extends LongId, Named, Remark {
  /** 流程定义 */
  var flow: Flow = _
  /** 顺序号 */
  var idx: Int = _
  /** 先决条件 */
  var guard: Option[String] = None
  /** 受理人 工号 或  求值为代码的表达式 */
  var assignees: Option[String] = None
  /** 受理用户组 */
  var groups: mutable.Buffer[Group] = Collections.newBuffer[Group]
  /** 受理人部门，具体部门代码 或 求值为代码的表达式 */
  var depart: Option[String] = None
  /** 先决条件说明 */
  var guardComment: Option[String] = None
}
