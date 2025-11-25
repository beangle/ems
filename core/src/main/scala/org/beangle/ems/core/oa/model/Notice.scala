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

import org.beangle.commons.collection.Collections
import org.beangle.data.model.LongId
import org.beangle.data.model.pojo.DateRange
import org.beangle.ems.core.config.model.App
import org.beangle.ems.core.user.model.{Category, User}

import java.time.Instant
import scala.collection.mutable

/** 公告
 * */
class Notice extends LongId, DateRange {

  var app: App = _

  var issuer: String = _

  var title: String = _

  var contents: String = _

  var categories: mutable.Set[Category] = Collections.newSet

  var sticky: Boolean = _

  var createdAt: Instant = _

  var updatedAt: Instant = _

  var publishedAt: Option[Instant] = None

  var operator: User = _

  var auditor: Option[User] = None

  var archived: Boolean = _

  var popup: Boolean = _

  var docs: mutable.Buffer[Doc] = Collections.newBuffer[Doc]

  var status: NoticeStatus = NoticeStatus.Draft
}
