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
import org.beangle.commons.xml.Document
import org.beangle.ems.app.{AppLogger, Ems, EmsApp}
import org.beangle.ems.ws.security.{data, func}
import org.beangle.ems.ws.user.*
import org.beangle.notify.sms.{DefaultSmsCodeService, SmsSender}
import org.beangle.webmvc.execution.{CacheResult, DefaultResponseCache}

import java.io.FileInputStream

class DefaultModule extends BindModule, Config.Provider {

  protected override def binding(): Unit = {
    AppLogger.info("Ems Home:" + Ems.home)
    bind(classOf[config.DatasourceWS], classOf[config.OrgWS], classOf[config.FileWS])
    bind(classOf[config.DomainWS], classOf[config.ThemeWS])
    bind(classOf[config.TextBundleWS], classOf[config.RedisWS])
    bind(classOf[config.RuleWS])

    bind(classOf[oa.NoticeWS], classOf[oa.DocWS], classOf[oa.FlowWS])
    bind(classOf[oa.SignatureWS], classOf[oa.SmsWS])

    bind(classOf[func.MenuWS])
    bind(classOf[func.ResourceWS], classOf[func.PermissionWS])
    bind(classOf[data.PermissionWS], classOf[data.ResourceWS])

    bind(classOf[log.PushWS], classOf[log.ListWS])

    bind(classOf[AccountWS], classOf[AppWS], classOf[DimensionWS], classOf[AvatarWS])
    bind(classOf[RootWS], classOf[ProfileWS], classOf[CredentialWS], classOf[UserWS])

    // response cache is only 3 minutes
    val cm = new CaffeineCacheManager(true)
    cm.ttl = 3 * 60
    cm.tti = 3 * 60
    val responseCache = cm.getCache("mvc.response", classOf[String], classOf[CacheResult])
    bind("mvc.ResponseCache.caffeine", classOf[DefaultResponseCache]).constructor(responseCache)

    bind(classOf[oauth.LoginWS])

    //绑定sms服务
    EmsApp.getAppFile foreach { file =>
      val is = new FileInputStream(file)
      val app = Document.parse(is)
      (app \\ "sms") foreach { e =>
        bind(classOf[DefaultSmsCodeService])
        val sender = Reflections.newInstance[SmsSender](e("class"))
        e.attrs foreach { (k, v) =>
          if k != "class" then Properties.copy(sender, k, v)
        }
        sender match
          case i: Initializing => i.init()
          case _ =>
        bind("smsSender", sender)
      }
    }
  }

  override def properties: collection.Map[String, String] = {
    EmsApp.properties
  }

  override def processors: Seq[Config.Processor] = {
    EmsApp.encryptor.toList
  }
}
