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

package org.beangle.ems.app.web.tag

import org.beangle.commons.security.DefaultRequest
import org.beangle.ems.app.Ems
import org.beangle.security.authz.Authorizer
import org.beangle.security.context.SecurityContext
import org.beangle.template.api.{AbstractModels, ComponentContext, Tag}

class EmsModels(context: ComponentContext, authorizer: Authorizer) extends AbstractModels(context) {
  def api(url: String): String = {
    Ems.api + url
  }

  def webapp: String = {
    Ems.webapp
  }

  def avatar: Tag = get(classOf[AvatarTag])

  def permitted(res: String): Boolean = {
    authorizer.isPermitted(SecurityContext.get, new DefaultRequest(res, null))
  }

  def user: Tag = get(classOf[UserTag])
}
