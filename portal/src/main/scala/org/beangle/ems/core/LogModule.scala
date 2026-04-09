package org.beangle.ems.core

import org.beangle.commons.cdi.BindModule
import org.beangle.ems.app.log.{AsyncAppLogger, LogExceptionHandler, WebBusinessLogger}
import org.beangle.ems.core.log.service.{LogDbAppender, LogPersistBuffer}
import org.beangle.webmvc.dispatch.ExceptionHandler

class LogModule extends BindModule {

  protected override def binding(): Unit = {
    //日志 Web Logger
    bind(classOf[WebBusinessLogger])
    bind(classOf[LogPersistBuffer]).constructor(?, ?, 1024)
    bind(classOf[AsyncAppLogger]).property("appenders", list(classOf[LogDbAppender]))

    //如果生产环境，则启用日志上报功能
    if (!devEnabled) {
      bind(classOf[LogExceptionHandler]).primaryOf(classOf[ExceptionHandler])
    }
  }

}
