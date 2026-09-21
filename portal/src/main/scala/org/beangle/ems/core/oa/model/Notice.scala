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
import scala.compiletime.uninitialized

/** 公告
 * */
class Notice extends LongId, DateRange {

  var app: App = uninitialized

  var issuer: String = uninitialized

  var title: String = uninitialized

  var contents: String = uninitialized

  var categories: mutable.Set[Category] = Collections.newSet

  var sticky: Boolean = uninitialized

  var createdAt: Instant = uninitialized

  var updatedAt: Instant = uninitialized

  var publishedAt: Option[Instant] = None

  var operator: User = uninitialized

  var auditor: Option[User] = None

  var archived: Boolean = uninitialized

  var popup: Boolean = uninitialized

  var attachments: mutable.Buffer[NoticeAttachment] = Collections.newBuffer[NoticeAttachment]

  var status: NoticeStatus = NoticeStatus.Draft

  def addAttachment(a: NoticeAttachment): Unit = {
    a.notice = this
    attachments.addOne(a)
  }

  def docs: Seq[NoticeAttachment] = {
    attachments.filter(x => !x.embedded).toSeq
  }
}
