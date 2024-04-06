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

package org.beangle.ems.app.datasource

import org.beangle.commons.io.IOs
import org.beangle.jdbc.ds.DataSourceFactory
import org.beangle.ems.app.util.AesEncryptor
import org.beangle.ems.app.{Ems, EmsApi, EmsApp}

import java.io.FileInputStream

class AppDataSourceFactory extends DataSourceFactory {

  override def init(): Unit = {
    if (null == name) name = "default"
    if (EmsApp.getAppFile exists (file => IOs.readString(new FileInputStream(file)).contains("</datasource>"))) {
      this.url = EmsApp.getAppFile.get.getCanonicalPath
    } else {
      this.url = EmsApi.getDatasourceUrl(name)
    }
    super.init()
  }

  protected override def postInit(): Unit = {
    if (password != null && password.startsWith("?")) {
      this.password = new AesEncryptor(Ems.key).decrypt(password.substring(1))
    }
  }

}
