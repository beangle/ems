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

import java.time.{Instant, LocalDate, ZoneId}

import org.beangle.commons.bean.Initializing
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.user.model.{Account, PasswordConfig, RoleMember, User, UserProfile}
import org.beangle.ems.core.user.service.{AccountService, PasswordConfigService, UserService}
import org.beangle.security.authc.{CredentialAge, DefaultAccount, Profile}

class AccountServiceImpl extends AccountService with Initializing {

  var entityDao: EntityDao = _
  var domainService: DomainService = _
  var userService: UserService = _
  var passwordConfigService: PasswordConfigService = _

  private var config: PasswordConfig = _

  override def init(): Unit = {
    config = passwordConfigService.get()
  }

  def get(code: String): Option[Account] = {
    val query = OqlBuilder.from(classOf[Account], "acc")
    query.where("acc.domain=:domain", domainService.getDomain)
    query.where("acc.user.org=:org", domainService.getOrg)
    query.where("acc.user.code=:code", code)
    entityDao.search(query).headOption
  }

  def getActivePassword(code: String): Option[String] = {
    val builder = OqlBuilder.from[String](classOf[Account].getName, "c")
    builder.where("c.user.code=:code", code)
    builder.where("c.user.org=:org", domainService.getOrg)
    builder.where("c.domain=:domain", domainService.getDomain)
    builder.where("c.passwdExpiredOn >= :now", LocalDate.now.minusDays(config.idledays))
    builder.select("c.password")
    entityDao.search(builder).headOption
  }

  override def getAuthAccount(code: String): Option[DefaultAccount] = {
    val domain = domainService.getDomain
    get(code) match {
      case Some(acc) =>
        val user = acc.user
        val account = new DefaultAccount(user.code, user.name)
        account.accountExpired = acc.accountExpired
        account.accountLocked = acc.locked
        account.credentialExpired = acc.passwdInactive(config.idledays)
        account.disabled = !acc.enabled
        account.categoryId = user.category.id

        val query = OqlBuilder.from[Int](classOf[RoleMember].getName, "rm")
          .where("rm.user=:user and rm.member=true", user)
          .where("rm.role.domain=:domain", domain)
          .select("rm.role.id")
        val rs = entityDao.search(query)
        account.authorities = rs.map(_.toString).toArray

        val upQuery = OqlBuilder.from(classOf[UserProfile], "up")
          .where("up.user=:user", user)
          .where("up.domain=:domain", domain)
        val ups = entityDao.search(upQuery)

        if (ups.nonEmpty) {
          account.profiles = Array.ofDim(ups.size)
          var i = 0
          ups foreach { up =>
            account.profiles(i) = Profile(up.id, up.name, up.properties.map(x => (x._1.name, x._2)).toMap)
            i += 1
          }
        }
        Some(account)
      case None => None
    }
  }

  private def isManagedBy(manager: User, user: User): Boolean = {
    true
  }

  override def enable(manager: User, accountIds: Iterable[Long], enabled: Boolean): Int = {
    val accounts = entityDao.find(classOf[Account], accountIds)
    val updated = accounts.filter(a => isManagedBy(manager, a.user))
    updated.foreach { u => u.enabled = enabled }
    entityDao.saveOrUpdate(updated)
    updated.size
  }

  override def getPasswordAge(code: String): Option[CredentialAge] = {
    get(code) map { c => CredentialAge(c.updatedAt, c.passwdExpiredOn, c.passwdExpiredOn.plusDays(config.idledays)) }
  }

  override def updatePassword(code: String, rawPassword: String): Unit = {
    val account = get(code) match {
      case Some(ac) => Some(ac)
      case None =>
        userService.get(code) match {
          case Some(u) =>
            val acc = new Account
            acc.user = u
            acc.domain = domainService.getDomain
            Some(acc)
          case None => None
        }
    }
    account foreach { acc =>
      val config = passwordConfigService.get()
      acc.password = rawPassword
      acc.updatedAt = Instant.now
      val maxdays = if (config.mindays > 10000) 10000 else config.maxdays
      acc.passwdExpiredOn = LocalDate.ofInstant(acc.updatedAt, ZoneId.systemDefault()).plusDays(maxdays)
      entityDao.saveOrUpdate(acc)
    }
  }

  override def createAccount(user: User, acc: Account): Unit = {
    acc.user = user
    acc.domain = domainService.getDomain
    val config = passwordConfigService.get()
    acc.updatedAt = Instant.now
    val maxdays = if (config.mindays > 10000) 10000 else config.maxdays
    acc.passwdExpiredOn = LocalDate.ofInstant(acc.updatedAt, ZoneId.systemDefault()).plusDays(maxdays)
    entityDao.saveOrUpdate(acc)
  }
}
