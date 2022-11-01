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

package org.beangle.ems.core.user.service.impl

import java.time.Instant

import org.beangle.commons.collection.Collections
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.data.model.Entity
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.user.model.MemberShip
import org.beangle.ems.core.user.model.MemberShip.{Granter, Manager, Member}
import org.beangle.ems.core.user.model._
import org.beangle.ems.core.user.service.UserService

class UserServiceImpl(val entityDao: EntityDao) extends UserService {

  var domainService: DomainService = _

  def get(code: String): Option[User] = {
    val query = OqlBuilder.from(classOf[User], "u")
    query.where("u.org=:org", domainService.getOrg)
    query.where("u.code=:code", code)
    entityDao.search(query).headOption
  }

  def get(id: Long): User = {
    entityDao.get(classOf[User], id)
  }

  def getUsers(ids: Long*): collection.Seq[User] = {
    entityDao.find(classOf[User], ids.toList)
  }

  def getRoles(user: User, ship: MemberShip): collection.Seq[RoleMember] = {
    val domain = domainService.getDomain
    ship match {
      case Manager => user.roles.filter(m => m.manager && m.role.domain == domain)
      case Granter => user.roles.filter(m => m.granter && m.role.domain == domain)
      case Member => user.roles.filter(m => m.member && m.role.domain == domain)
    }
  }

  //FIXME
  def isManagedBy(manager: User, user: User): Boolean = {
    true
  }

  override def isRoot(user: User, appName: String): Boolean = {
    val rootQuery = OqlBuilder.from(classOf[Root], "r")
    rootQuery.where("r.user=:user and r.app.name=:appName", user, appName)
    rootQuery.where("r.app.domain=:domain", domainService.getDomain)
    entityDao.search(rootQuery).nonEmpty
  }

  def create(creator: User, user: User): Unit = {
    user.updatedAt = Instant.now
    user.org = domainService.getOrg
    entityDao.saveOrUpdate(user)
  }

  def remove(manager: User, user: User): Unit = {
    if (isManagedBy(manager, user)) {
      val removed = Collections.newBuffer[Entity[_]]
      removed ++= entityDao.findBy(classOf[Account], "user", List(user))
      removed ++= entityDao.findBy(classOf[Profile], "user", List(user))
      entityDao.remove(removed, user)
    }
  }

  override def getCategories(): Seq[Category] = {
    val query = OqlBuilder.from(classOf[Category], "uc")
    query.where("uc.org=:org", domainService.getOrg).cacheable()
    entityDao.search(query)
  }
}
