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

package org.beangle.ems.web

import org.beangle.cdi.bind.ReconfigModule
import org.beangle.commons.io.{Dirs, Files}
import org.beangle.commons.text.i18n.TextBundleLoader
import org.beangle.ems.app.{Ems, EmsApp}
import org.beangle.web.action.view.Static

import java.io.File

class WebReconfigModule extends ReconfigModule {
  override protected def config(): Unit = {
    val file = new File(Ems.home + EmsApp.path)

    //1.模板个性化 当本地项目文件存在，并且包含模板文件时才启用本地加载
    if (file.exists()) {
      var templateCount = 0
      Files.travel(file, f => {
        if (f.getName.endsWith(".ftl")) templateCount += 1
      })
      if (templateCount > 0) {
        //support load local freemarker template files
        update("mvc.FreemarkerConfigurer.default")
          .set("templatePath", s"file://${Ems.home + EmsApp.path}/,class://")
      }
    }

    //2.国际化词条个性化
    update("mvc.TextBundleLoader.db").primaryOf(classOf[TextBundleLoader])

    //3.spring配置个性化
    this.configUrl = s"file://${Ems.home + EmsApp.path}/spring-config.xml"

    //4. 静态文件配置
    Static.Default.base = Ems.static
  }

}
