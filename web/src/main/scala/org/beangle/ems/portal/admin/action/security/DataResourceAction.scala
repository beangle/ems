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

package org.beangle.ems.portal.admin.action.security

import org.beangle.data.model.Entity
import org.beangle.ems.core.config.model.Domain
import org.beangle.ems.core.config.service.DataSourceManager
import org.beangle.ems.core.security.model.DataResource
import org.beangle.web.action.view.View
import org.beangle.webmvc.support.action.RestfulAction

/**
 * 系统模块管理响应类
 *
 * @author chaostone 2005-10-9
 */
class DataResourceAction extends RestfulAction[DataResource] {

  var dataSourceManager: DataSourceManager = _

  /**
   * 禁用或激活一个或多个模块
   */
  def activate(): View = {
    val resourceIds = getIntIds("resource")
    val enabled = getBoolean("enabled", defaultValue = false)
    dataSourceManager.activate(resourceIds, enabled.booleanValue())
    redirect("search", "info.save.success")
  }

  protected override def editSetting(dataPermission: DataResource): Unit = {
    put("domains", entityDao.getAll(classOf[Domain]))
  }

  protected override def saveAndRedirect(resource: DataResource): View = {
    if (null != resource) {
      if (entityDao.duplicate(classOf[DataResource], resource.id, Map("name" -> resource.name))) {
        return redirect("edit", "error.notUnique")
      }
    }
    entityDao.saveOrUpdate(resource)
    redirect("search", "info.save.success")
  }

  override def info(id: String): View = {
    val entity: DataResource = getModel(id.toInt)
    put(simpleEntityName, entity)
    forward()
  }

  protected override def simpleEntityName: String = {
    "resource"
  }

}
