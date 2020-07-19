/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2020, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.ems.core.user.service.impl

import java.util.Date

import org.beangle.data.dao.{ EntityDao, OqlBuilder }
import org.beangle.ems.core.user.model.{ Role, User }
import org.beangle.ems.core.user.service.RoleService
import org.beangle.data.model.util.Hierarchicals
import java.time.ZonedDateTime

class RoleServiceImpl extends RoleService {

  var entityDao: EntityDao = _
  override def isManagedBy(manager: User, role: Role): Boolean = {
    true
  }

  override def create(creator: User, role: Role): Unit = {
    role.creator = creator
    role.updatedAt = ZonedDateTime.now.toInstant
    entityDao.saveOrUpdate(role)
  }

  override def move(role: Role, parent: Role, indexno: Int): Unit = {
    val nodes =
      if (null != parent) {
        Hierarchicals.move(role, parent, indexno)
      } else {
        val builder = OqlBuilder.from(classOf[Role], "r")
          .where("r.parent is null")
          .orderBy("r.indexno")
        Hierarchicals.move(role, entityDao.search(builder).toBuffer, indexno)
      }
    entityDao.saveOrUpdate(nodes)
  }

  override def remove(manager: User, roles: Seq[Role]): Unit = {
    entityDao.remove(roles)
  }

  def get(id: Int): Role = {
    entityDao.get(classOf[Role], id)
  }

}
