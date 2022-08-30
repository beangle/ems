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

package org.beangle.ems.core.bulletin.service.impl

import org.beangle.data.dao.EntityDao
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.bulletin.model.Doc
import org.beangle.ems.core.bulletin.service.DocService

import java.io.InputStream
import java.time.ZoneId

class DocServiceImpl extends DocService {

  var entityDao: EntityDao = _

  def save(doc: Doc, filename: String, is: InputStream): Doc = {
    val repo = EmsApp.getBlobRepository()
    val user = doc.uploadBy
    val meta = repo.upload(s"/doc/${doc.updatedAt.atZone(ZoneId.systemDefault()).getYear}", is, filename, user.code + " " + user.name)

    if null == doc.name then doc.name = meta.name
    doc.updatedAt = meta.updatedAt
    doc.filePath = meta.filePath
    doc.fileSize = meta.fileSize
    entityDao.saveOrUpdate(doc)
    doc
  }

  def remove(doc: Doc): Unit = {
    val repo = EmsApp.getBlobRepository()
    repo.remove(doc.filePath)
    entityDao.remove(doc)
  }
}
