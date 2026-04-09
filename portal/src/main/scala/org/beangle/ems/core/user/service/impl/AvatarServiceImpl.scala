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

package org.beangle.ems.core.user.service.impl

import org.beangle.commons.codec.digest.Digests
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.user.model.{Avatar, User}
import org.beangle.ems.core.user.service.AvatarService
import org.beangle.event.bus.{DataEvent, DataEventBus}

import java.io.InputStream

class AvatarServiceImpl extends AvatarService {

  var entityDao: EntityDao = _
  var databus: DataEventBus = _

  def save(user: User, filename: String, is: InputStream): Unit = {
    val repo = EmsApp.getBlobRepository()
    val query = OqlBuilder.from(classOf[Avatar], "avatar")
    query.where("avatar.user=:user", user)
    val avatars = entityDao.search(query)
    var avatar: Avatar = null
    if (avatars.isEmpty) {
      avatar = new Avatar(user)
      avatar.id = Digests.md5Hex(user.code)
    } else {
      avatar = avatars.head
      if (null != avatar.filePath) {
        repo.remove(avatar.filePath)
      }
      if (avatar.id != Digests.md5Hex(user.code)) {
        entityDao.remove(avatar)
        databus.publish(DataEvent.remove(avatar))
        avatar = new Avatar(user)
        avatar.id = Digests.md5Hex(user.code)
      }
    }
    val meta = repo.upload(s"/avatar/${user.beginOn.getYear}", is, filename, user.code + " " + user.name)
    user.avatarId = Some(avatar.id)
    avatar.fileName = meta.name
    avatar.updatedAt = meta.updatedAt
    avatar.filePath = meta.filePath
    entityDao.saveOrUpdate(avatar, user)
    databus.publish(DataEvent.update(avatar))
  }
}
