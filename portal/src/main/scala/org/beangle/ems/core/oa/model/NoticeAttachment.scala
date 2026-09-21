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

package org.beangle.ems.core.oa.model

import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.lang.Strings
import org.beangle.data.model.LongId
import org.beangle.data.model.pojo.Updatable
import org.beangle.ems.app.EmsApp

import java.net.URI
import scala.compiletime.uninitialized

/**
 * 系统公告的福建
 */
class NoticeAttachment extends LongId {

  var name: String = uninitialized

  var fileSize: Int = uninitialized

  var filePath: String = uninitialized

  var notice: Notice = uninitialized

  var embedded: Boolean = uninitialized

  def image: Boolean = {
    MediaTypes.Defaults.get(Strings.substringAfterLast(filePath, ".")) match
      case None => false
      case Some(mt) => mt.primaryType == "image"
  }

  def uri: URI = EmsApp.getBlobRepository().uri(this.filePath)

  def url: String = {
    uri.toString
  }
}
