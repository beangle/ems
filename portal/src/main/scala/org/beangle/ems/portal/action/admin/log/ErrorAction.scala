package org.beangle.ems.portal.action.admin.log

import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.app.log.Level
import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.ems.core.log.model.ErrorLog
import org.beangle.webmvc.support.action.{ExportSupport, RestfulAction}
import org.beangle.webmvc.support.helper.QueryHelper

class ErrorAction extends RestfulAction[ErrorLog], ExportSupport[ErrorLog] {

  var appService: AppService = _
  var domainService: DomainService = _

  override protected def indexSetting(): Unit = {
    put("apps", appService.getApps)
    put("levels", Level.values)
    super.indexSetting()
  }

  override protected def getQueryBuilder: OqlBuilder[ErrorLog] = {
    val builder = super.getQueryBuilder
    builder.where("errorLog.app.domain=:domain", domainService.getDomain)
    QueryHelper.dateBetween(builder, null, "occurredAt", "beginOn", "endOn")
    builder
  }

}

