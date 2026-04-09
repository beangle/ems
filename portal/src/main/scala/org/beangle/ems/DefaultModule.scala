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

package org.beangle.ems

import org.beangle.cache.caffeine.CaffeineCacheManager
import org.beangle.commons.cdi.BindModule
import org.beangle.commons.config.Config
import org.beangle.commons.script.ExpressionEvaluator
import org.beangle.ems.app.dao.AppDataSourceFactory
import org.beangle.ems.app.log.{AsyncAppLogger, LogExceptionHandler, WebBusinessLogger}
import org.beangle.ems.app.rule.ExpressionEvaluatorFactory
import org.beangle.ems.app.web.tag.EmsTagLibrary
import org.beangle.ems.app.{AppLogger, Ems, EmsApp}
import org.beangle.ems.core.log.service.{LogDbAppender, LogPersistBuffer}
import org.beangle.security.authz.Authorizer
import org.beangle.webmvc.dispatch.ExceptionHandler
import org.beangle.webmvc.execution.{CacheResult, DefaultResponseCache}

class DefaultModule extends BindModule, Config.Provider {

  protected override def binding(): Unit = {
    AppLogger.info("Ems Home:" + Ems.home)

    bind("DataSource.default", classOf[AppDataSourceFactory])

    //响应缓存 only 3 minutes,using on @response(cachable = true)
    val cm = new CaffeineCacheManager(true)
    cm.ttl = 3 * 60
    cm.tti = 3 * 60
    val responseCache = cm.getCache("mvc.response", classOf[String], classOf[CacheResult])
    bind("mvc.ResponseCache.caffeine", classOf[DefaultResponseCache]).constructor(responseCache)

    //标签
    bind("mvc.TagLibrary.ems", classOf[EmsTagLibrary]).onExist(classOf[Authorizer])

    //表达式引擎
    bind("expressionEvaluator", classOf[ExpressionEvaluatorFactory]).constructor("jexl3").onMissing(classOf[ExpressionEvaluator])
  }

  override def properties: collection.Map[String, String] = {
    EmsApp.properties
  }

  override def processors: Seq[Config.Processor] = {
    List(Ems.decryptor)
  }
}
