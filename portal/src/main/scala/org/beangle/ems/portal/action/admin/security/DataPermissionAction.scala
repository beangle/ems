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

package org.beangle.ems.portal.action.admin.security

import org.beangle.webmvc.annotation.ignore
import org.beangle.webmvc.view.View
import org.beangle.webmvc.support.action.RestfulAction
import org.beangle.ems.core.config.model.{App, Domain}
import org.beangle.ems.core.security.model.{DataPermission, DataResource, FuncResource}
import org.beangle.ems.core.user.model.Role

/**
 * 数据限制模式元信息配置类
 * @author chaostone
 */
class DataPermissionAction extends RestfulAction[DataPermission] {
  @ignore
  protected override def simpleEntityName: String = {
    "permission"
  }

  protected override def editSetting(dataPermission: DataPermission): Unit = {
    put("roles", entityDao.getAll(classOf[Role]))
    put("domains", entityDao.getAll(classOf[Domain]))
    put("apps", entityDao.getAll(classOf[App]))
    put("funcResources", entityDao.getAll(classOf[FuncResource]))
    put("dataResources", entityDao.getAll(classOf[DataResource]))
  }

  protected override def saveAndRedirect(dataPermission: DataPermission): View = {
    if (entityDao.duplicate(classOf[DataPermission], dataPermission.id, Map("remark"-> dataPermission.remark))) {
      addError("限制模式描述重复")
      forward(to(this, "edit"))
    } else {
      entityDao.saveOrUpdate(dataPermission)
      redirect("search", "info.save.success")
    }
  }
}
