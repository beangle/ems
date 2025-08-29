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

import org.beangle.commons.codec.digest.Digests
import org.beangle.ems.app.Ems
import org.beangle.template.api.{ComponentContext, UIBean}

class AvatarTag(context: ComponentContext) extends UIBean(context) {

  var href: String = _
  var username: String = _

  override def evaluateParams(): Unit = {
    if (null == this.href) {
      href = Ems.api + s"/platform/user/avatars/${Digests.md5Hex(username)}.jpg"
    }
    if (null == this.cssClass && !parameters.contains("style")) {
      parameters.put("style", "border-radius: 50%;width:40px;")
    }
  }
}
