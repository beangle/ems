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

package org.beangle.ems.ws

import org.beangle.cache.caffeine.CaffeineCacheManager
import org.beangle.commons.bean.{Initializing, Properties}
import org.beangle.commons.cdi.BindModule
import org.beangle.commons.config.Config
import org.beangle.commons.lang.reflect.Reflections
import org.beangle.commons.script.ExpressionEvaluator
import org.beangle.commons.xml.Document
import org.beangle.cron.{CronTaskRegistrar, Scheduler}
import org.beangle.ems.app.rule.ExpressionEvaluatorFactory
import org.beangle.ems.app.{AppLogger, Ems, EmsApp}
import org.beangle.ems.ws.job.CronTaskRefresher
import org.beangle.notify.sms.{DefaultSmsCodeService, SmsSender, SmsSenderFactory}
import org.beangle.webmvc.execution.{CacheResult, DefaultResponseCache}

import java.io.FileInputStream

class DefaultModule extends BindModule, Config.Provider {

  protected override def binding(): Unit = {
    AppLogger.info("Ems Home:" + Ems.home)
    // response cache is only 3 minutes
    val cm = new CaffeineCacheManager(true)
    cm.ttl = 3 * 60
    cm.tti = 3 * 60
    val responseCache = cm.getCache("mvc.response", classOf[String], classOf[CacheResult])
    bind("mvc.ResponseCache.caffeine", classOf[DefaultResponseCache]).constructor(responseCache)

    bind(classOf[Scheduler])
    bind(classOf[CronTaskRegistrar]).lazyInit(false)

    bind(classOf[CronTaskRefresher]).constructor("0 * * * * *").lazyInit(false)

    //表达式引擎
    bind("expressionEvaluator", classOf[ExpressionEvaluatorFactory]).constructor("jexl3").onMissing(classOf[ExpressionEvaluator])

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

  override def properties: collection.Map[String, String] = {
    EmsApp.properties
  }

  override def processors: Seq[Config.Processor] = {
    List(Ems.decryptor)
  }
}
