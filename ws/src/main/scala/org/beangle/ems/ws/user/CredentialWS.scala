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

package org.beangle.ems.ws.user

import org.beangle.data.dao.EntityDao
import org.beangle.security.authc.{PasswordPolicy, PasswordStrengthChecker}
import org.beangle.web.action.support.ActionSupport
import org.beangle.web.action.annotation.{param, response}
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.user.model.PasswordConfig

class CredentialWS extends ActionSupport {

  var entityDao: EntityDao = _

  var domainService: DomainService = _

  private def getConfig: PasswordConfig = {
    val configs = entityDao.findBy(classOf[PasswordConfig], "domain", List(domainService.getDomain))
    if (configs.nonEmpty) {
      configs.head
    } else {
      PasswordConfig(PasswordPolicy.Medium)
    }
  }

  @response
  def comment(): String = {
    val config = getConfig
    val cmt = new StringBuilder(s"长度${config.minlen}~${config.maxlen}之间")
    var clazz = 0
    if (config.dcredit > 0 || config.lcredit > 0 || config.ucredit > 0 || config.ocredit > 0) {
      cmt ++= "，至少包含"
      if (config.dcredit > 0) {
        cmt ++= s"${config.dcredit}数字、"
        clazz += 1
      }
      if (config.lcredit > 0) {
        cmt ++= s"${config.lcredit}小写字母、"
        clazz += 1
      }
      if (config.ucredit > 0) {
        cmt ++= s"${config.ucredit}大写字母、"
        clazz += 1
      }
      if (config.ocredit > 0) {
        cmt ++= s"${config.ocredit}特殊字符、"
        clazz += 1
      }
    }
    if (cmt.endsWith("、")) {
      cmt.deleteCharAt(cmt.length - 1)
    }
    if (clazz < config.minclass) {
      cmt ++= s"，至少含有${config.minclass}类字符(数字、小写、大写、特殊)"
    }
    if (config.usercheck) {
      cmt ++= s"，且不能含有用户名"
    }
    cmt.mkString
  }

  @response
  def check(@param("pwd") pwd: String): Boolean = {
    val config = getConfig
    val strengthOk = PasswordStrengthChecker.check(pwd, getConfig)
    if (strengthOk && config.usercheck) {
      get("user") match {
        case Some(u) => !pwd.toLowerCase().contains(u)
        case None => false
      }
    } else {
      strengthOk
    }
  }
}
