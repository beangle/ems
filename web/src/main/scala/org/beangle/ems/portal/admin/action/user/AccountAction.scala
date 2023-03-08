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

package org.beangle.ems.portal.admin.action.user

import org.beangle.commons.collection.{Collections, Order}
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.{Condition, Operation, OqlBuilder}
import org.beangle.ems.app.EmsApp
import org.beangle.ems.app.web.WebBusinessLogger
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.user.model.*
import org.beangle.ems.core.user.service.{AccountService, PasswordConfigService, UserService}
import org.beangle.security.Securities
import org.beangle.security.authc.DBCredentialStore
import org.beangle.security.codec.DefaultPasswordEncoder
import org.beangle.web.action.context.ActionContext
import org.beangle.web.action.view.View
import org.beangle.web.servlet.util.RequestUtils
import org.beangle.webmvc.support.action.{ExportSupport, RestfulAction}

import java.time.Instant

/**
 * 用户管理响应处理类
 *
 * @author chaostone 2005-9-29
 */
class AccountAction extends RestfulAction[Account], ExportSupport[Account] {

  var userService: UserService = _

  var accountService: AccountService = _

  var credentialStore: DBCredentialStore = _

  var domainService: DomainService = _

  var businessLogger: WebBusinessLogger = _

  override def indexSetting(): Unit = {
    put("categories", userService.getCategories())
  }

  def saveRole(): View = {
    val user = entityDao.get(classOf[User], getLongId("user"))
    val userMembers = user.roles
    val memberMap = new collection.mutable.HashMap[Role, RoleMember]
    for (gm <- userMembers) {
      if (gm.role.domain == domainService.getDomain) {
        memberMap.put(gm.role, gm.asInstanceOf[RoleMember])
      }
    }
    val newMembers = Collections.newBuffer[RoleMember]
    val removedMembers = Collections.newBuffer[RoleMember]
    val manager = loginUser
    val isAdmin = userService.isRoot(manager, EmsApp.name)
    val members =
      if (isAdmin) {
        val adminRoleQuery = OqlBuilder.from(classOf[Role], "r").where("r.domain=:domain", domainService.getDomain)
        entityDao.search(adminRoleQuery).map(r => new RoleMember(manager, r, MemberShip.Granter))
      } else {
        userService.getRoles(manager, MemberShip.Granter)
      }
    for (member <- members) {
      var myMember = memberMap.getOrElse(member.role, null)
      val isMember = getBoolean("member" + member.role.id, defaultValue = false)
      val isGranter = getBoolean("granter" + member.role.id, defaultValue = false)
      val isManager = getBoolean("manager" + member.role.id, defaultValue = false)
      if (!isMember && !isGranter && !isManager) {
        if (null != myMember) {
          user.roles -= myMember
          removedMembers += myMember
        }
      } else {
        if (null == myMember) myMember = new RoleMember(user, member.role)
        myMember.updatedAt = Instant.now
        myMember.member = isMember
        myMember.granter = isGranter
        myMember.manager = isManager
        newMembers += myMember
      }
    }
    val ob = new Operation.Builder()
    for (m <- newMembers) ob.saveOrUpdate(m)
    for (m <- removedMembers) ob.remove(m)
    entityDao.execute(ob)
    entityDao.refresh(user)

    //记录日志
    val newRoleNames = newMembers.map(_.role.getName).mkString(",")
    val removeRoleNames = removedMembers.map(_.role.getName).mkString(",")
    val summary1 = if newMembers.nonEmpty then s",新增了${newRoleNames}" else ""
    val summary2 = if removedMembers.nonEmpty then s",删除了${removeRoleNames}" else ""
    businessLogger.info(s"修改${user.name}的角色${summary1}${summary2}", user.id, ActionContext.current.params)

    redirect("search", "info.save.success")
  }

  /**
   * 删除一个或多个用户
   */
  override def remove(): View = {
    val accountIds = getLongIds("account")
    val creator = loginUser
    val toBeRemoved = entityDao.find(classOf[Account], accountIds)
    val sb = new StringBuilder()
    var removed: Account = null
    var success = 0
    var expected = toBeRemoved.size
    try {
      for (one <- toBeRemoved) {
        removed = one
        // 不能删除自己
        if (one.user != creator) {
          val user = one.user
          user.acounts -= one
          entityDao.saveOrUpdate(user)
          if (user.acounts.isEmpty) {
            userService.remove(creator, user)
          }
          success += 1
        } else {
          addFlashError("security.info.cannotRemoveSelf")
          expected -= 1
        }
      }
    } catch {
      case _: Exception => sb.append(',').append(removed.user.getName())
    }
    if (sb.nonEmpty) {
      sb.deleteCharAt(0)
      addFlashMessage("security.info.userRemovePartial", success, sb)
    } else if (expected == success && success > 0) {
      addFlashMessage("info.remove.success")
    }
    redirect("search")
  }

  /**
   * 禁用或激活一个或多个用户
   */
  def activate(): View = {
    val accountIds = getLongIds("account")
    val isActivate = get("isActivate", "true")
    var successCnt: Int = 0
    val manager = loginUser
    var msg = "security.info.freeze.success"
    if (Strings.isNotEmpty(isActivate) && "false".equals(isActivate)) {
      successCnt = accountService.enable(manager, accountIds, false)
    } else {
      msg = "security.info.activate.success"
      successCnt = accountService.enable(manager, accountIds, true)
    }
    addFlashMessage(msg, successCnt)
    redirect("search")
  }

  protected override def getQueryBuilder: OqlBuilder[Account] = {
    val domain = domainService.getDomain
    put("domain", domain)
    val accQuery = OqlBuilder.from(classOf[Account], "account")
    accQuery.where("account.domain=:domain", domain)
    accQuery.join("account.user", "user")
    // 查询角色
    val sb = new StringBuilder()
    val params = new collection.mutable.ListBuffer[Object]
    val roleName = get("roleName", "")
    if (Strings.isNotEmpty(roleName)) {
      sb.append("exists(from account.user.roles m where ")
      sb.append("m.role.name like :roleName and m.role.domain=:domain ")
      params += ("%" + roleName + "%")
      params += domain
      sb.append(')')
    }
    if (sb.nonEmpty) {
      val roleCondition = new Condition(sb.toString)
      roleCondition.params(params)
      accQuery.where(roleCondition)
    }
    populateConditions(accQuery)
    accQuery.tailOrder("account.id")
    accQuery.orderBy(get(Order.OrderStr).orNull).limit(getPageLimit)
    accQuery
  }

  /**
   * 保存用户信息
   */
  protected override def saveAndRedirect(account: Account): View = {
    val user =
      if (getLong("user.id").isEmpty) {
        val code = get("user.code").head
        val org = domainService.getOrg
        val userQuery = OqlBuilder.from(classOf[User], "u")
        userQuery.where("u.code=:code and u.org=:org", code, org)
        entityDao.search(userQuery).headOption match {
          case Some(v) => v
          case None => populateEntity(classOf[User], "user")
        }
      } else {
        populateEntity(classOf[User], "user")
      }
    // check account exists
    if (!user.persisted) {
      user.beginOn = account.beginOn
      user.endOn = account.endOn
      userService.create(loginUser, user)
    } else {
      businessLogger.info(s"修改${user.name}的账户", user.id, ActionContext.current.params)
      entityDao.saveOrUpdate(user)
    }
    account.user = user
    updateAccount(account.user, account)
    redirect("search", "info.save.success")
  }

  private def loginUser: User = {
    userService.get(Securities.user).head
  }

  protected def updateAccount(user: User, account: Account): Unit = {
    var password = get("password").orNull
    if (Strings.isBlank(password) && !user.persisted) {
      password = user.code
    }
    if (Strings.isNotBlank(password)) {
      password = DefaultPasswordEncoder.generate(password, null, "sha")
    }
    if (account.persisted) {
      if (Strings.isNotBlank(password)) {
        credentialStore.updatePassword(user.code, password)
      }
    } else {
      if (null != password) {
        account.password = password
      }
      accountService.createAccount(user, account)
    }
  }

  protected override def editSetting(account: Account): Unit = {
    put("categories", userService.getCategories())
    val domain = domainService.getDomain
    val manager = loginUser
    val roles = new collection.mutable.HashSet[Role]
    val mngMemberMap = new collection.mutable.HashMap[Role, RoleMember]
    val emsAdmin = userService.isRoot(manager, EmsApp.name)
    if (emsAdmin) {
      val roleQuery = OqlBuilder.from(classOf[Role], "r").orderBy("r.indexno")
      roleQuery.where("r.domain=:domain", domain)
      roles ++= entityDao.search(roleQuery)
      for (role <- roles)
        mngMemberMap.put(role, new RoleMember(manager, role, MemberShip.Granter))
    } else {
      val members = userService.getRoles(manager, MemberShip.Granter)
      for (gm <- members) {
        roles.add(gm.role)
        mngMemberMap.put(gm.role, gm)
      }
    }
    put("roles", roles)

    val memberMap = new collection.mutable.HashMap[Role, RoleMember]
    if (null != account.user) {
      for (gm <- account.user.roles) {
        if (gm.role.domain == domain) {
          memberMap.put(gm.role, gm)
        }
      }
    }
    put("memberMap", memberMap)
    put("mngMemberMap", mngMemberMap)
  }
}
