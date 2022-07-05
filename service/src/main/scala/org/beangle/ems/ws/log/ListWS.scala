package org.beangle.ems.ws.log

import org.beangle.commons.collection.Properties
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.core.config.service.AppService
import org.beangle.ems.core.log.model.BusinessLog
import org.beangle.web.action.annotation.{action, mapping, param}
import org.beangle.web.action.support.{ActionSupport, EntitySupport, ServletSupport}
import org.beangle.web.action.view.{Status, View}

@action("list/{appName}")
class ListWS extends ActionSupport {
  var entityDao: EntityDao = _
  var appService: AppService = _

  @mapping("{resourceId}")
  def resource(@param("appName") appName: String, @param("resourceId") resourceId: String): Iterable[Properties] = {
    appService.getApp(appName) match {
      case None => List.empty
      case Some(app) =>
        val query = OqlBuilder.from(classOf[BusinessLog], "log")
        query.where("log.app=:app", app)
        query.where("log.resources=:resourceId", resourceId)
        val logs = entityDao.search(query)
        val sorted=logs.sortBy(_.operateAt).reverse
        sorted.map(log => new Properties(log, "id", "operator", "operateAt", "summary", "agent", "ip"))
    }
  }

}
