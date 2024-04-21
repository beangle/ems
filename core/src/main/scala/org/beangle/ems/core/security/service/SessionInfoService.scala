package org.beangle.ems.core.security.service

import org.beangle.commons.collection.page.PageLimit
import org.beangle.ems.core.security.model.SessionInfo

trait SessionInfoService {
  def find(principal: Option[String], limit: PageLimit, order: Option[String]): Iterable[SessionInfo]
}
