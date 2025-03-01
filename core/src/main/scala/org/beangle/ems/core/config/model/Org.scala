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

package org.beangle.ems.core.config.model

import org.beangle.data.model.IntId
import org.beangle.data.model.pojo.{Named, Remark}

/** 组织信息
 */
class Org extends IntId with Named with Remark {
  /** 组织编号 */
  var code: String = _
  /** 简称 */
  var shortName: String = _
  /** logo地址 */
  var logoUrl: String = _
  /** 网站地址 */
  var wwwUrl: String = _

}
