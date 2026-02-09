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

import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.lang.ClassLoaders
import org.beangle.data.dao.EntityDao
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.user.model.Avatar
import org.beangle.webmvc.annotation.{mapping, param}
import org.beangle.webmvc.support.{ActionSupport, ServletSupport}
import org.beangle.webmvc.view.{Stream, View}

class AvatarWS(entityDao: EntityDao)
  extends ActionSupport with ServletSupport {

  var expireMinutes: Int = 60 * 24 * 7

  @mapping("default")
  def defaultAvatar(): View = {
    Stream(ClassLoaders.getResourceAsStream("org/beangle/ems/ws/default_avatar.jpg").get,
      MediaTypes.jpeg, "default_avatar.jpg", None)
  }

  @mapping("{avatarId}")
  def info(@param("avatarId") avatarId: String): View = {
    val avatar = entityDao.get(classOf[Avatar], avatarId)
    if null == avatar then this.redirect("defaultAvatar")
    else redirect(to(EmsApp.getBlobRepository().path(avatar.filePath)), "")
  }

}
