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

package org.beangle.ems.cas.action

import org.beangle.commons.io.IOs
import org.beangle.commons.lang.ClassLoaders
import org.beangle.webmvc.support.{ActionSupport, ServletSupport}
import org.beangle.webmvc.view.View

/** 隐私安全政策页（/cas/safety.html）。
 *
 * 页面本体是 classpath 上的静态 HTML（cas/safety.html）：native 下没有 war 根目录，
 * 容器默认 servlet 不可用，由本路由从 classpath 读出后直接输出（JVM 下同）。
 */
class SafetyAction extends ActionSupport, ServletSupport {

  private val ResourceName = "cas/safety.html"

  def index(): View = {
    val stream = ClassLoaders.getResourceAsStream(ResourceName) match {
      case Some(in) => in
      case None => throw new RuntimeException(s"Cannot find $ResourceName in classpath")
    }
    response.setContentType("text/html;charset=UTF-8")
    raw(IOs.readBytes(stream))
  }
}
