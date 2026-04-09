package org.beangle.ems.core

import org.beangle.commons.cdi.BindModule
import org.beangle.cron.{CronTaskRegistrar, Scheduler}
import org.beangle.ems.ws.job.CronTaskRefresher

class CronModule extends BindModule {

  protected override def binding(): Unit = {
    //定时任务
    bind(classOf[Scheduler])
    bind(classOf[CronTaskRegistrar]).lazyInit(false)
    bind(classOf[CronTaskRefresher]).constructor("0 * * * * *").lazyInit(false)
  }

}
