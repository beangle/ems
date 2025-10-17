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

import org.beangle.commons.bean.Initializing
import org.beangle.commons.collection.Collections
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.data.model.Entity
import org.beangle.ems.core.config.model.Domain
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.user.model.*
import org.beangle.ems.core.user.model.MemberShip.{Granter, Manager, Member}
import org.beangle.ems.core.user.service.{PasswordConfigService, UserService}
import org.beangle.security.authc.{CredentialAge, DefaultAccount, Profile as ProfileData}

import java.time.{Instant, LocalDate, ZoneId}

class UserServiceImpl(val entityDao: EntityDao) extends UserService, Initializing {

  var domainService: DomainService = _
  var passwordConfigService: PasswordConfigService = _

  private var config: PasswordConfig = _

  override def init(): Unit = {
    config = passwordConfigService.get()
  }

  def get(code: String): Option[User] = {
    val query = OqlBuilder.from(classOf[User], "u")
    query.where("u.org=:org", domainService.getOrg)
    query.where("u.code=:code", code)
    entityDao.search(query).headOption
  }

  def getIgnoreCase(code: String): Option[User] = {
    val query = OqlBuilder.from(classOf[User], "u")
    query.where("u.org=:org", domainService.getOrg)
    query.where("lower(u.code)=:code", code.toLowerCase)
    entityDao.search(query).headOption
  }

  def get(id: Long): User = {
    entityDao.get(classOf[User], id)
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
    if (null == user.password) {
      user.password = "--EMPTY--"
    }
    val config = passwordConfigService.get()
    val maxdays = if (config.mindays > 10000) 10000 else config.maxdays
    user.passwdExpiredOn = LocalDate.ofInstant(user.updatedAt, ZoneId.systemDefault()).plusDays(maxdays)

    entityDao.saveOrUpdate(user)
  }

  def remove(manager: User, user: User): Unit = {
    if (isManagedBy(manager, user)) {
      val removed = Collections.newBuffer[Entity[_]]
      removed ++= entityDao.findBy(classOf[Profile], "user", List(user))
      entityDao.remove(removed, user)
    }
  }

  override def getCategories(): Seq[Category] = {
    val query = OqlBuilder.from(classOf[Category], "uc")
    query.where("uc.org=:org", domainService.getOrg).cacheable()
    entityDao.search(query)
  }

  override def getAccount(code: String): Option[DefaultAccount] = {
    val domain = domainService.getDomain
    get(code) match {
      case Some(user) =>
        val account = new DefaultAccount(user.code, user.name)
        account.accountExpired = user.accountExpired
        account.accountLocked = user.locked
        account.credentialExpired = user.passwdInactive(config.idledays)
        account.disabled = !user.enabled
        account.categoryId = user.category.id

        val rs = getRoles(user, domain)
        account.authorities = rs.map(_.id.toString).toArray

        val upQuery = OqlBuilder.from(classOf[Profile], "up")
          .where("up.user=:user", user)
          .where("up.domain=:domain", domain)
        val ups = entityDao.search(upQuery)

        if (ups.nonEmpty) {
          account.profiles = Array.ofDim(ups.size)
          var i = 0
          ups foreach { up =>
            account.profiles(i) = ProfileData(up.id, up.name, up.properties.map(x => (x._1.name, x._2)).toMap)
            i += 1
          }
        }
        Some(account)
      case None => None
    }
  }

  private def getRoles(user: User, domain: Domain): Seq[Role] = {
    val roles = user.roles.filter(m => m.member && m.role.domain == domain).map { m => m.role }
    user.group foreach { g =>
      roles.addAll(g.roles filter (r => r.domain == domain))
    }
    user.groups foreach { gm =>
      roles.addAll(gm.group.roles filter (r => r.domain == domain))
    }
    roles.toSet.toSeq //去重后返回
  }

  override def enable(manager: User, userIds: Iterable[Long], enabled: Boolean): Int = {
    val users = entityDao.find(classOf[User], userIds)
    val updated = users.filter(a => isManagedBy(manager, a))
    updated.foreach { u => u.enabled = enabled }
    entityDao.saveOrUpdate(updated)
    updated.size
  }

  override def getActivePassword(code: String): Option[String] = {
    val builder = OqlBuilder.from[String](classOf[User].getName, "u")
    builder.where("u.code=:code", code)
    builder.where("u.org=:org", domainService.getOrg)
    builder.where("u.passwdExpiredOn >= :now", LocalDate.now.minusDays(config.idledays))
    builder.select("u.password")
    entityDao.search(builder).headOption
  }

  override def getPasswordAge(code: String): Option[CredentialAge] = {
    get(code) map { u => CredentialAge(u.updatedAt, u.passwdExpiredOn, u.passwdExpiredOn.plusDays(config.idledays)) }
  }

  override def updatePassword(code: String, rawPassword: String): Unit = {
    get(code) foreach { u =>
      val config = passwordConfigService.get()
      u.password = rawPassword
      u.updatedAt = Instant.now
      val maxdays = if (config.mindays > 10000) 10000 else config.maxdays
      u.passwdExpiredOn = LocalDate.ofInstant(u.updatedAt, ZoneId.systemDefault()).plusDays(maxdays)
      entityDao.saveOrUpdate(u)
    }
  }
}
