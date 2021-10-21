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
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.user.model.Avatar
import org.beangle.web.action.support.{ActionSupport, ServletSupport}
import org.beangle.web.action.annotation.{mapping, param}
import org.beangle.web.action.view.{Stream, View}

class AvatarWS(entityDao: EntityDao)
  extends ActionSupport with ServletSupport {

  var expireMinutes: Int = 60 * 24 * 7

  @mapping("default")
  def defaultAvatar(): View = {
    Stream(ClassLoaders.getResourceAsStream("org/beangle/ems/ws/default_avatar.jpg").get,
      MediaTypes.ImageJpeg.toString(), "default_avatar.jpg", None)
  }

  @mapping("{avatarId}")
  def info(@param("avatarId") avatarId: String): View = {
    loadAvatarPath(avatarId) match {
      case Some(a) => deliver(a); null
      case None => this.redirect("defaultAvatar")
    }
  }

  private def deliver(path: String): Unit = {
    EmsApp.getBlobRepository(true).path(path) match {
      case Some(p) => response.sendRedirect(p)
      case None => response.setStatus(404)
    }
  }

  private def loadAvatarPath(avatarId: String): Option[String] = {
    val query = OqlBuilder.from[String](classOf[Avatar].getName, "a")
    query.where("a.id = :id", avatarId)
    query.select("a.filePath")
    entityDao.search(query).headOption
  }

}
