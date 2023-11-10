package org.beangle.ems.core.config.service.impl

import org.beangle.commons.bean.Initializing
import org.beangle.commons.lang.Charsets
import org.beangle.commons.text.i18n.DefaultTextBundleRegistry
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.app.EmsApp
import org.beangle.ems.core.config.model.TextBundle
import org.beangle.ems.core.config.service.DomainService

import java.io.{ByteArrayInputStream, InputStream}
import java.util.Locale

class DbTextBundleRegistry extends DefaultTextBundleRegistry {

  var entityDao: EntityDao = _

  var domainService: DomainService = _

  override protected def loadExtra(locale: Locale, bundleName: String): collection.Seq[(String, InputStream)] = {
    val query = OqlBuilder.from(classOf[TextBundle], "b")
    query.where("b.app.name=:name", EmsApp.name)
    query.where("b.app.domain=:domain", domainService.getDomain)
    query.where("b.locale=:locale and b.name=:bundleName", locale, bundleName)
    entityDao.search(query).map { b =>
      (b.name + "@db", new ByteArrayInputStream(b.texts.getBytes(Charsets.UTF_8)))
    }
  }

}
