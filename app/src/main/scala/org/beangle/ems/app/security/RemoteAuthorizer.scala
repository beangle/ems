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

import scala.concurrent.Future

/**
 * @author chaostone
 */
class RemoteAuthorizer extends AbstractRoleBasedAuthorizer {

  override def fetchDomain(): AuthorityDomain = {
    AuthorityDomain(RemoteService.getAuthorities)
  }

  /**
   * 此处重载是为了加快启动速度
   */
  override def init(): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    Future {
      refresh()
    }
  }
}
