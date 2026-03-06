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

package org.beangle.ems.app.web

import org.beangle.commons.cdi.BindModule
import org.beangle.commons.config.Config
import org.beangle.commons.text.i18n.{HttpTextBundleLoader, TextBundleLoader}
import org.beangle.cron.CronTaskRegistrar
import org.beangle.ems.app.log.{AsyncAppLogger, LogExceptionHandler, RemoteAppender, WebBusinessLogger}
import org.beangle.ems.app.web.tag.EmsTagLibrary
import org.beangle.ems.app.{AppLogger, Ems, EmsApp}
import org.beangle.security.authz.Authorizer
import org.beangle.webmvc.dispatch.ExceptionHandler

class DefaultModule extends BindModule, Config.Provider {

  protected override def binding(): Unit = {
    AppLogger.info("Ems Home:" + Ems.home)

    bind("mvc.TagLibrary.ems", classOf[EmsTagLibrary]).onExist(classOf[Authorizer])

    //Cron
    bind(classOf[CronTaskRegistrar]).lazyInit(false).onMissing(classOf[CronTaskRegistrar])

    //Web Logger
    bind(classOf[WebBusinessLogger])
    bind(classOf[AsyncAppLogger])
      .property("appenders",
        List(new RemoteAppender(Ems.innerApi + s"/platform/log/push")))

    //如果不是开发环境，则启用日志上报功能
    if (!devEnabled) {
      bind(classOf[LogExceptionHandler]).primaryOf(classOf[ExceptionHandler])
    }

    //DefaultTextBundle
    bind("mvc.TextBundleLoader.http", classOf[HttpTextBundleLoader])
      .constructor(s"${Ems.innerApi}/platform/config/text-bundles/${EmsApp.name}/{path}", true)
      .onMissing(classOf[TextBundleLoader])
  }

  override def properties: collection.Map[String, String] = {
    EmsApp.properties
  }

  override def processors: Seq[Config.Processor] = {
    EmsApp.encryptor.toList
  }
}
