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

package org.beangle.ems.core.blob.model

import org.beangle.data.model.LongId
import org.beangle.data.model.pojo.Updated
import org.beangle.ems.core.config.model.Domain

class BlobMeta extends LongId with Updated {
  var domain:Domain=_
  var owner: String = _
  var name: String = _
  var fileSize: Long = _
  var sha: String = _
  var mediaType: String = _
  var profile: Profile = _
  var filePath: String = _
}
