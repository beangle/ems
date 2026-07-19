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

package org.beangle.ems.core.security.service.impl

import org.beangle.commons.collection.Collections
import org.beangle.data.dao.{EntityDao, Operation, OqlBuilder}
import org.beangle.ems.core.config.model.{App, Env}
import org.beangle.ems.core.security.model.{FuncPermission, FuncResource, Menu, RoleAppEnv}
import org.beangle.ems.core.security.service.FuncPermissionService
import org.beangle.ems.core.user.model.{Role, User}

class FuncPermissionServiceImpl(val entityDao: EntityDao) extends FuncPermissionService {

  def getResource(app: App, name: String): Option[FuncResource] = {
    val query = OqlBuilder.from(classOf[FuncResource], "r")
    query.where("r.name=:name and r.app=:app", name, app).cacheable()
    val rs = entityDao.search(query)
    rs.headOption
  }

  def getResourceIdsByRole(roleId: Int): Set[Int] = {
    val hql = "select a.resource.id from " + classOf[FuncPermission].getName() +
      " as a where a.role.id= :roleId and a.resource.enabled = true"
    val query = OqlBuilder.oql[Int](hql).param("roleId", roleId).cacheable()
    entityDao.search(query).toSet
  }

  def getResources(user: User): Seq[FuncResource] = {
    null
  }

  def getResources(app: App): Seq[FuncResource] = {
    val query = OqlBuilder.from(classOf[FuncResource], "r")
    query.where("r.app = :app", app)
    entityDao.search(query)
  }

  def getPermissions(app: App, role: Role): Seq[FuncPermission] = {
    entityDao.search(OqlBuilder.from(classOf[FuncPermission], "fp").where("fp.resource.app=:app and fp.role=:role", app, role))
  }

  override def getRoleAppEnvs(app: App, role: Role): Seq[RoleAppEnv] = {
    entityDao.search(OqlBuilder.from(classOf[RoleAppEnv], "rae")
      .where("rae.app=:app and rae.role=:role", app, role)
      .cacheable())
  }

  def activate(ids: Iterable[Int], active: Boolean): Unit = {
    val resources = entityDao.find(classOf[FuncResource], ids)
    resources.foreach { f => f.enabled = active }
    entityDao.saveOrUpdate(resources)
  }

  def authorize(app: App, role: Role, resources: Set[FuncResource], envIds: Iterable[Long] = Nil): Unit = {
    val resourceSet = Collections.newSet[FuncResource] ++ resources
    val permissions = getPermissions(app, role).toBuffer
    val builder = new Operation.Builder()
    for (au <- permissions) {
      if (resources.contains(au.resource)) {
        resourceSet.remove(au.resource)
      } else {
        builder.remove(au)
      }
    }

    for (resource <- resourceSet) {
      builder.saveOrUpdate(new FuncPermission(role, resource))
    }

    // 场景：与目标 envIds 对比后增量增删；目标为空表示全部场景（不落库）
    val exists = getRoleAppEnvs(app, role)
    val targetIds = if (resources.isEmpty) Set.empty[Long] else envIds.toSet
    val existByEnvId = exists.map(e => e.env.id -> e).toMap
    (existByEnvId.keySet -- targetIds).foreach { id => builder.remove(existByEnvId(id)) }
    val toAdd = targetIds -- existByEnvId.keySet
    if (toAdd.nonEmpty) {
      entityDao.find(classOf[Env], toAdd).foreach { env =>
        builder.saveOrUpdate(new RoleAppEnv(role, app, env))
      }
    }

    entityDao.execute(builder)
  }

  override def removeResources(resources: Iterable[FuncResource]): Unit = {
    val menuBuilder2 = OqlBuilder.from(classOf[Menu], "m").join("m.resources", "r")
    val menus2 = entityDao.search(menuBuilder2)
    menus2 foreach (m => m.resources --= resources)
    entityDao.saveOrUpdate(menus2)

    val menuBuilder = OqlBuilder.from(classOf[Menu], "m").
      where("m.entry in(:entries)", resources)
    val menus = entityDao.search(menuBuilder)
    menus foreach (m => m.entry = None)

    val query = OqlBuilder.from(classOf[FuncPermission], "fp")
    query.where("fp.resource in(:res)", resources)
    val fps = entityDao.search(query)
    entityDao.remove(fps)
    entityDao.remove(resources)
  }
}
