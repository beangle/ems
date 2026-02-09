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

package org.beangle.ems.ws.oa

import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ems.core.oa.model.Signature
import org.beangle.webmvc.annotation.{mapping, param}
import org.beangle.webmvc.support.{ActionSupport, ServletSupport}
import org.beangle.webmvc.view.{Status, View}

/** 查询用户的签名文件
 *
 * @param entityDao
 */
class SignatureWS(entityDao: EntityDao) extends ActionSupport, ServletSupport {
  var domainService: DomainService = _

  @mapping("{userCode}")
  def info(@param("userCode") userCode: String): View = {
    loadSignaturePath(userCode) match {
      case Some(path) =>
        val p = EmsApp.getBlobRepository().path(path)
        response.addHeader("Access-Control-Allow-Origin", "*")
        redirect(to(p), "")
      case None => Status(404)
    }
  }

  private def loadSignaturePath(userCode: String): Option[String] = {
    val query = OqlBuilder.from[String](classOf[Signature].getName, "a")
    query.where("a.user.org=:org", domainService.getOrg)
    query.where("a.user.code = :code", userCode)
    query.select("a.filePath")
    entityDao.search(query).headOption
  }

}
