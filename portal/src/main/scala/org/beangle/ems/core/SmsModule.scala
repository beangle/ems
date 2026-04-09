/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
