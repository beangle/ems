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

package org.beangle.ems.portal.aot

import org.beangle.commons.aot.{AotHintRegistrar, AotPolicy}

/** org.beangle.ems.portal.* 包的 GraalVM 反射提示。
 *
 * 随 portal 模块发布（AotPlugin 按模块独立收集，native 构建时各 jar 配置自动
 * 合并）。目前只需补 portal 辅助类 DomainSupport（Spring 依赖注入反射）。
 */
class EmsPortalAotHints extends AotHintRegistrar {

  override def registering(): Unit = {
    hints.registerType(classOf[org.beangle.ems.portal.action.admin.DomainSupport],AotPolicy.bean)
  }
}
