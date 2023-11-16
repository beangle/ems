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
import org.beangle.ems.app.{Ems, EmsApp}

import java.io.File

class WebReconfigModule extends ReconfigModule {
  override protected def config(): Unit = {
    val file = new File(Ems.home + EmsApp.path + "/pages")
    if (file.exists()) {
      //support load local freemarker template files
      update("mvc.FreemarkerConfigurer.default")
        .set("templatePath", s"file://${Ems.home + EmsApp.path}/pages/,class://")
    }
  }
}
