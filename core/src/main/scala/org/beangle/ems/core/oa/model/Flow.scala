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
import org.beangle.commons.json.{Json, JsonObject}
import org.beangle.data.model.LongId
import org.beangle.data.model.pojo.{Coded, Named, Remark, Updated}
import org.beangle.ems.core.config.model.{Business, Domain}

import scala.collection.mutable

/** 工作流定义
 */
class Flow extends LongId, Coded, Updated, Named, Remark {
  /** 业务系统 */
  var domain: Domain = _
  /** 环境配置 */
  var profileId: String = _
  /** 业务类型 */
  var business: Business = _
  /** 活动列表 */
  var activities: mutable.Buffer[FlowActivity] = Collections.newBuffer[FlowActivity]
  /** 节点流转关系 */
  var flowJson: String = "{}"
  /** 初始数据 */
  var envJson: String = "{}"
  /** 先决条件 */
  var guardJson: String = "{}"
  /** 审批表单地址 */
  var formUrl: String = _
  /** 审核消息模板 */
  var todoMessage: Option[MessageTemplate] = None
  /** 审核结果消息模板 */
  var resultMessage: Option[MessageTemplate] = None

  def firstActivity: FlowActivity = {
    activities.minBy(_.idx)
  }

  def checkMatch(data: JsonObject): Boolean = {
    data.isMatch(Json.parseObject(guardJson))
  }

}
