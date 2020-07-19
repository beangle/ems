/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2020, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.ems.app.cas

import org.beangle.cache.caffeine.CaffeineCacheManager
import org.beangle.cdi.bind.BindModule
import org.beangle.ems.app.Ems
import org.beangle.ems.app.security.RemoteAuthorizer
import org.beangle.security.authc.{DefaultAccount, RealmAuthenticator}
import org.beangle.security.realm.cas.{CasConfig, CasEntryPoint}
import org.beangle.security.session.protobuf.{AccountSerializer, AgentSerializer, SessionSerializer}
import org.beangle.security.session.{DefaultSession, Session}
import org.beangle.security.web.access.{AuthorizationFilter, DefaultAccessDeniedHandler, DefaultSecurityContextBuilder, SecurityInterceptor}
import org.beangle.security.web.session.CookieSessionIdReader
import org.beangle.serializer.protobuf.ProtobufSerializer

class DefaultModule extends BindModule {

  override def binding(): Unit = {
    // entry point
    bind("security.EntryPoint.cas", classOf[CasEntryPoint]).primary()

    //interceptor and filters
    bind("security.AccessDeniedHandler.default", classOf[DefaultAccessDeniedHandler])
      .constructor($("security.access.errorPage", "/403.html"))
    bind("security.Filter.authorization", classOf[AuthorizationFilter])
    bind("web.Interceptor.security", classOf[SecurityInterceptor]).property(
      "filters", List(ref("security.Filter.authorization")))

    bind("security.Authenticator", classOf[RealmAuthenticator])

    bind("security.SessionIdReader.ems", classOf[CookieSessionIdReader]).constructor(Ems.sid.name)
    bind("cache.Caffeine", classOf[CaffeineCacheManager]).constructor(true)

    val protobuf = new ProtobufSerializer
    protobuf.register(classOf[DefaultSession], SessionSerializer)
    protobuf.register(classOf[DefaultAccount], AccountSerializer)
    protobuf.register(classOf[Session.Agent], AgentSerializer)

    bind("security.SessionRepo.http", classOf[CasHttpSessionRepo])
      .constructor(ref("casConfig"), ref("cache.Caffeine"), protobuf)

    bind(classOf[DefaultSecurityContextBuilder])
    //cas
    bind("casConfig", classOf[CasConfig]).constructor(Ems.cas)

    //authorizer and manager
    bind("security.Authorizer.remote", classOf[RemoteAuthorizer])

  }

}
