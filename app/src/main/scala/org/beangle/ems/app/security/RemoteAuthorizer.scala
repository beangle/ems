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

package org.beangle.ems.app.security

import org.beangle.security.authz.{AbstractRoleBasedAuthorizer, AuthorityDomain}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author chaostone
 */
class RemoteAuthorizer extends AbstractRoleBasedAuthorizer {

  override def fetchDomain(): AuthorityDomain = {
    val roots = RemoteService.roots
    val resources = RemoteService.getAuthorities
    AuthorityDomain(roots.getOrElse(Set.empty), resources)
  }

  /**
   * 此处一定要重载，因为获取权限需要访问网络服务，有可能该服务就是自身进程提供的，容易卡住主线程
   */
  override def init(): Unit = {
    Future {
      refresh()
    }
  }
}
