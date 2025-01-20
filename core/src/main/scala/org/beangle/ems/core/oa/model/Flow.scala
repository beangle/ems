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
import org.beangle.data.json.JsonValue
import org.beangle.data.model.LongId
import org.beangle.data.model.pojo.{Named, Remark, Updated}
import org.beangle.ems.core.config.model.{Business, Domain}

import scala.collection.mutable

/** 工作流定义
 */
class Flow extends LongId, Updated, Named, Remark {
  var domain: Domain = _
  var business: Business = _
  var tasks: mutable.Buffer[FlowTask] = Collections.newBuffer[FlowTask]
  var dataJson: JsonValue = JsonValue.Empty
}
