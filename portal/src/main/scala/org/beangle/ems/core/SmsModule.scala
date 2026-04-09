package org.beangle.ems.core

import org.beangle.commons.cdi.BindModule
import org.beangle.commons.xml.Document
import org.beangle.cron.{CronTaskRegistrar, Scheduler}
import org.beangle.ems.app.EmsApp
import org.beangle.ems.ws.job.CronTaskRefresher
import org.beangle.notify.sms.{DefaultSmsCodeService, SmsSenderFactory}

import java.io.FileInputStream

class SmsModule extends BindModule {

  protected override def binding(): Unit = {
    //绑定sms服务
    EmsApp.getAppFile foreach { file =>
      val is = new FileInputStream(file)
      val app = Document.parse(is)
      (app \\ "sms") foreach { e =>
        bind(classOf[DefaultSmsCodeService])
        bind("smsSender", SmsSenderFactory.createSender(e.attrs))
      }
    }
  }

}
