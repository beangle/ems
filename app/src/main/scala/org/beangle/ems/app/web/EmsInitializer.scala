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

package org.beangle.ems.app.web

import jakarta.servlet.ServletContext
import org.beangle.ems.app.{Ems, EmsApp}
import org.beangle.web.servlet.init.Initializer

class EmsInitializer extends Initializer {
  override def onStartup(servletContext: ServletContext): Unit = {
  }

  override def onConfig(sc: ServletContext): Unit = {
    System.setProperty("beangle.webmvc.static_base", Ems.static)
    //针对平台应用，不要使用网络配置化，防止自我循环依赖
    if (!Ems.isPlatform(sc.getContextPath)) {
      System.setProperty("beangle.cdi.reconfig_url", s"${Ems.api}/platform/config/files/${EmsApp.name}/spring-config.xml")
    }
  }
}
