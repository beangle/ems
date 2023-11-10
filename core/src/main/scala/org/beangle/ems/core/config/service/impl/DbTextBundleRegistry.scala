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

package org.beangle.ems.core.config.service.impl

import org.beangle.commons.bean.Initializing
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Charsets
import org.beangle.commons.text.i18n.DefaultTextBundleRegistry
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.config.model.TextBundle
import org.beangle.ems.core.config.service.DomainService

import java.io.{ByteArrayInputStream, InputStream}
import java.util.Locale

class DbTextBundleRegistry extends DefaultTextBundleRegistry, Initializing {

  var entityDao: EntityDao = _

  var domainService: DomainService = _

  private val bundles = Collections.newMap[String, Long]

  override def init(): Unit = {
    val query = OqlBuilder.from(classOf[TextBundle], "b")
    query.where("b.app.name=:name", EmsApp.name)
    query.where("b.app.domain=:domain", domainService.getDomain)
    entityDao.search(query) foreach { b =>
      bundles.put(b.name + "@" + b.locale.toString, b.id)
    }
  }

  override protected def loadExtra(locale: Locale, bundleName: String): collection.Seq[(String, InputStream)] = {
    bundles.get(s"${bundleName}@${locale.toString}") match {
      case None => List.empty
      case Some(id) =>
        val b = entityDao.get(classOf[TextBundle], id)
        if reloadable then entityDao.refresh(b)
        List((b.name + "@db", new ByteArrayInputStream(b.texts.getBytes(Charsets.UTF_8))))
    }
  }

}
