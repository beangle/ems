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

import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.data.model.util.Hierarchicals
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.security.model.FuncPermission
import org.beangle.ems.core.user.model._
import org.beangle.ems.core.user.service.RoleService

import java.time.ZonedDateTime

class RoleServiceImpl extends RoleService {

  var entityDao: EntityDao = _
  var domainService: DomainService = _

  override def isManagedBy(manager: User, role: Role): Boolean = {
    if (manager.roles.exists(rm => rm.manager && rm.role == role)) {
      true
    } else {
      val rq = OqlBuilder.from(classOf[Root], "r")
      rq.where("r.user=:user and r.app.name=:appName", manager, EmsApp.name)
      rq.where("r.app.domain=:domain", domainService.getDomain)
      entityDao.search(rq).nonEmpty
    }
  }

  override def create(creator: User, role: Role): Unit = {
    role.creator = creator
    role.updatedAt = ZonedDateTime.now.toInstant
    val rm = new RoleMember(creator, role, MemberShip.Manager)
    rm.granter = true
    entityDao.saveOrUpdate(role, rm)
  }

  override def move(role: Role, parent: Role, indexno: Int): Unit = {
    role.parent foreach { p =>
      if (null == parent || p != parent) {
        role.parent = None
        entityDao.saveOrUpdate(role)
        entityDao.refresh(p)
      }
    }
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
    val query = OqlBuilder.from(classOf[FuncPermission], "fp")
    query.where("fp.role in(:roles)", roles)
    val fps = entityDao.search(query)
    entityDao.remove(fps :: roles.toList)
  }

  def get(id: Int): Role = {
    entityDao.get(classOf[Role], id)
  }

}
