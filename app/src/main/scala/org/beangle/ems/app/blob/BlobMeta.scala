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

package org.beangle.ems.app.blob

import org.beangle.commons.json.Json
import org.beangle.data.model.pojo.Updated

object BlobMeta {
  def fromJson(json: String): BlobMeta = {
    val jb = Json.parseObject(json)
    val meta = new BlobMeta
    meta.name = jb.getString("name")
    meta.fileSize = jb.getInt("size")
    meta.sha = jb.getString("sha")
    meta.mediaType = jb.getString("type")
    meta.filePath = jb.getString("filePath")
    meta.updatedAt = jb.getInstant("updatedAt")
    meta
  }
}

class BlobMeta extends Updated {
  var name: String = _

  var fileSize: Int = _

  var sha: String = _

  var mediaType: String = _

  var filePath: String = _

}
