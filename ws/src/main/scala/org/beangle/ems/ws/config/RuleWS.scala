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

package org.beangle.ems.ws.config

import org.beangle.commons.collection.Properties
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.core.config.model.Rule
import org.beangle.ems.core.config.service.DomainService
import org.beangle.webmvc.annotation.{mapping, response}
import org.beangle.webmvc.support.ActionSupport

/** 规则查询服务
 */
class RuleWS extends ActionSupport {

  var entityDao: EntityDao = _
  var domainService: DomainService = _

  @response
  @mapping("{ids}")
  def list(ids: String): Iterable[Properties] = {
    val b = OqlBuilder.from(classOf[Rule], "r")
    b.where("r.id in(:ids)", Strings.splitToLong(ids))
    b.cacheable(true)
    val rules = entityDao.search(b)
    convert(rules)
  }

  @response
  @mapping("{businessCode}/{profileId}")
  def profile(businessCode: String, profileId: String): Iterable[Properties] = {
    val b = OqlBuilder.from(classOf[Rule], "r")
    b.where("r.meta.business.code=:businessCode", businessCode)
    b.where("r.profileId=:profileId", profileId)
    b.where("r.domain=:domain", domainService.getDomain)
    b.cacheable()
    val rules = entityDao.search(b)
    convert(rules)
  }

  private def convert(rules: Iterable[Rule]): Iterable[Properties] = {
    rules.map { r =>
      val p = new Properties(r, "id,")
      p.put("name", r.meta.name)
      p.put("title", r.meta.title)
      val pp = new Properties()
      r.params foreach { rp =>
        pp.put(rp.meta.name, rp.contents)
      }
      p.put("params", pp)
      p
    }
  }
}
