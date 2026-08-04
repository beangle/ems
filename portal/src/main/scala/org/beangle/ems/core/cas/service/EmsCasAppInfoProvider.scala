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

package org.beangle.ems.core.cas.service

import org.beangle.data.dao.EntityDao
import org.beangle.ems.core.config.model.App
import org.beangle.ems.core.config.service.DomainService
import org.beangle.ids.cas.service.{CasAppInfo, CasAppInfoProvider}

/** 扫码登录应用信息提供者。
 *
 *  按本域注册应用的 base 前缀匹配扫码登录的 service 地址，
 *  为确认页提供应用名称、logo 与主页地址。
 */
class EmsCasAppInfoProvider extends CasAppInfoProvider {

  var domainService: DomainService = _
  var entityDao: EntityDao = _

  override def get(service: String): Option[CasAppInfo] = {
    if service == null || service.isEmpty then None
    else {
      val domain = domainService.getDomain
      entityDao.findBy(classOf[App], "domain" -> domain)
        .find(a => a.base != null && service.startsWith(a.base))
        .map(a => CasAppInfo(
          if (a.title != null && a.title.nonEmpty) a.title else a.name,
          a.logoUrl,
          Some(a.base)))
    }
  }
}
