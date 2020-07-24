/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2020, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.ems.admin.action.blob

import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.blob.model.{BlobMeta, Profile}
import org.beangle.ems.core.config.service.DomainService
import org.beangle.webmvc.api.action.ServletSupport
import org.beangle.webmvc.api.annotation.mapping
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.entity.action.RestfulAction

class MetaAction extends RestfulAction[BlobMeta] with ServletSupport {

  var domainService: DomainService = _

  override def indexSetting(): Unit = {
    val query = OqlBuilder.from(classOf[Profile], "p")
    query.where("p.domain=:domain", domainService.getDomain)
    put("profiles", entityDao.search(query))
  }

  override protected def getQueryBuilder: OqlBuilder[BlobMeta] = {
    val builder = super.getQueryBuilder
    builder.where("blobMeta.domain=:domain", domainService.getDomain)
  }

  @mapping(value = "{id}")
  override def info(id: String): View = {
    val meta = entityDao.get(classOf[BlobMeta], id.toLong)
    response.sendRedirect(EmsApp.getBlobRepository(true).path(meta.path).get)
    null
  }
}
