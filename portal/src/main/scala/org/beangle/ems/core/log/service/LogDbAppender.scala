package org.beangle.ems.core.log.service

import org.beangle.ems.app.log.{Appender, LogEvent}

class LogDbAppender(buffer: LogPersistBuffer) extends Appender {
  override def append(event: LogEvent): Unit = {
    buffer.push(event)
  }
}
