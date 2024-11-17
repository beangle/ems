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

import org.beangle.commons.cdi.ReconfigModule
import org.beangle.commons.text.i18n.TextBundleLoader
import org.beangle.ems.app.{Ems, EmsApp}
import org.beangle.webmvc.view.Static

class WebReconfigModule extends ReconfigModule {
  override protected def config(): Unit = {
    //1.模板个性化
    //support load remote freemarker template files
    update("mvc.FreemarkerConfigurer.default")
      .set("templatePath", s"${Ems.api}/platform/config/files/${EmsApp.name}/{path},class://")

    //2.国际化词条个性化
    //using http text bundle loader
    update("mvc.TextBundleLoader.http").primaryOf(classOf[TextBundleLoader])

    //3.spring配置个性化
    this.configUrl = s"${Ems.api}/platform/config/files/${EmsApp.name}/spring-config.xml"

    //4. 静态文件配置
    Static.Default.base = Ems.static
  }
}
