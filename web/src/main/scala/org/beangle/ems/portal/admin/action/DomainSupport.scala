package org.beangle.ems.portal.admin.action

import org.beangle.ems.core.config.service.{AppService, DomainService}
import org.beangle.event.bus.{DataEvent, DataEventBus}

trait DomainSupport {
  var domainService: DomainService = _
  var appService: AppService = _
  var databus: DataEventBus = _

  def publishUpdate(clazz: Class[_], filters: Map[String, String], comment: Option[String] = None): Unit = {
    databus.publishUpdate(clazz, filters, comment)
  }

  def publishUpdate(obj: AnyRef): Unit = {
    databus.publish(DataEvent.update(obj))
  }
}
