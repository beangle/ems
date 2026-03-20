package org.beangle.ems.ws.security.oauth

import org.beangle.commons.bean.Scheduled
import org.beangle.data.orm.AbstractDaoTask
import org.beangle.ems.ws.WsLogger

import java.time.Instant

/**
 * 清理过期的Token
 */
class OAuthTokenCleaner extends AbstractDaoTask, Scheduled {
  var expression: String = _

  def execute(): Unit = {
    val removed = entityDao.executeUpdate("delete from OAuthToken token where token.expiresAt <= :now", "now" -> Instant.now)
    if (removed > 0) {
      WsLogger.info(s"Evict ${removed} oauth tokens")
    }
  }
}
