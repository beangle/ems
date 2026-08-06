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

import org.beangle.data.model.LongId
import org.beangle.data.model.pojo.{Enabled, Named}

object Theme {
  val Default = new Theme()
  Default.primaryColor = "#007bff"
  Default.navbarBgColor = "#3c8dbc"
  Default.searchBgColor = "#f8fafc"
  Default.gridbarBgColor = "#f8fafc"
  Default.gridHeaderBgColor = "#e1ecff"
  Default.gridBorderColor = "hsla(0,0%,0%,0.3)"
}

/** 主题 */
class Theme extends LongId, Named, Enabled {
  /** 域 */
  var domain: Domain = _

  var primaryColor: String = _

  /** 导航栏背景颜色 */
  var navbarBgColor: String = _

  /** 查询框背景颜色 */
  var searchBgColor: String = _

  /** 表格工具栏背景颜色 */
  var gridbarBgColor: String = _

  /** 表格表头背景颜色 */
  var gridHeaderBgColor: String = _

  /** 表格边框颜色 */
  var gridBorderColor: String = _

}
