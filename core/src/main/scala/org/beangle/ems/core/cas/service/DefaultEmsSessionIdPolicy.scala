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

package org.beangle.ems.core.cas.service

import jakarta.servlet.http.HttpServletRequest
import org.beangle.ems.app.Ems
import org.beangle.ids.cas.id.impl.DefaultIdGenerator
import org.beangle.security.web.session.CookieSessionIdPolicy

/**
 * @author chaostone
 */
class DefaultEmsSessionIdPolicy extends CookieSessionIdPolicy(Ems.sid.name) {
  private val sessionIdGenerator = new DefaultIdGenerator(Ems.sid.prefix, 35)

  override def init(): Unit = {
    if (null == this.domain) {
      this.base = Ems.base
    }
  }

  protected override def generateId(request: HttpServletRequest): String = {
    sessionIdGenerator.nextid()
  }

}
