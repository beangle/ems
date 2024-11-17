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

package org.beangle.ems.portal.action.user

import jakarta.servlet.http.Part
import org.beangle.commons.codec.digest.Digests
import org.beangle.commons.logging.Logging
import org.beangle.data.dao.EntityDao
import org.beangle.ems.app.Ems
import org.beangle.ems.core.user.model.User
import org.beangle.ems.core.user.service.AvatarService
import org.beangle.security.Securities
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.view.{Status, View}

class AvatarAction extends ActionSupport, Logging {

  var entityDao: EntityDao = _

  var avatarService: AvatarService = _

  def index(): View = {
    put("avatar_url", Ems.api + "/platform/user/avatars/" + Digests.md5Hex(Securities.user) + "?t=" + System.currentTimeMillis())
    put("users", entityDao.findBy(classOf[User], "code", List(Securities.user)))
    forward()
  }

  def upload(): View = {
    val users = entityDao.findBy(classOf[User], "code", List(Securities.user))
    if (users.isEmpty) {
      logger.warn("Cannot find user info of " + Securities.user)
      Status.NotFound
    } else {
      val user = users.head
      getAll("photo", classOf[Part]) foreach { p =>
        avatarService.save(user, p.getSubmittedFileName, p.getInputStream)
      }
    }
    forward()
  }
}
