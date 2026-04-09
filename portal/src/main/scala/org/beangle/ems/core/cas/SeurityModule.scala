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

package org.beangle.ems.core.cas

import org.beangle.commons.cdi.BindModule
import org.beangle.ems.core.cas.service.{DaoAccountStore, DefaultDomainProvider, DefaultEmsSessionIdPolicy, OAuthTokenCleaner}
import org.beangle.ems.core.security.service.DefaultEmsAuthorizer
import org.beangle.security.authc.{DefaultAccount, DefaultAccountRealm, Profile, RealmAuthenticator}
import org.beangle.security.session.jdbc.{DBSessionCleaner, DBSessionRegistry}
import org.beangle.security.session.protobuf.{AccountSerializer, AgentSerializer, ProfileSerializer, SessionSerializer}
import org.beangle.security.session.{DefaultSession, Session}
import org.beangle.security.web.WebSecurityManager
import org.beangle.security.web.access.{AuthorizationFilter, DefaultSecurityContextBuilder}
import org.beangle.serializer.protobuf.ProtobufSerializer

class SeurityModule extends BindModule {
  override def binding(): Unit = {
    val protobuf = new ProtobufSerializer
    protobuf.register(classOf[DefaultSession], SessionSerializer)
    protobuf.register(classOf[DefaultAccount], AccountSerializer)
    protobuf.register(classOf[Session.Agent], AgentSerializer)
    protobuf.register(classOf[Profile], ProfileSerializer)

    bind("domainProvider", classOf[DefaultDomainProvider])
    bind("Serializer.protobuf", protobuf)
    bind("security.SessionRegistry.db", classOf[DBSessionRegistry])
      .constructor(ref("domainProvider"), ?, ref("cache.Caffeine"), protobuf)
      .property("sessionTable", "ems.se_session_infoes")

    bind("security.SessionIdPolicy.ems", classOf[DefaultEmsSessionIdPolicy])
      .property("base", $("login.origin"))

    // authenticator
    bind("security.Realm.default", classOf[DefaultAccountRealm])
      .constructor(bean(classOf[DaoAccountStore]), ref("security.CredentialsChecker.default"))
    bind("security.Authenticator", classOf[RealmAuthenticator])
      .constructor(List(ref("security.Realm.default"))).wiredEagerly(false)

    //authorizer and manager
    bind("security.SecurityManager.default", classOf[WebSecurityManager])
    bind(classOf[DefaultSecurityContextBuilder])
    bind("security.Authorizer.ems", classOf[DefaultEmsAuthorizer])
    bind("security.Filter.authorization", classOf[AuthorizationFilter])

    //每5分钟清理一遍过期会话
    bind(classOf[DBSessionCleaner]).constructor(?, "0 */5 * * * *").lazyInit(false)
    //每10分钟清理一遍过期token
    bind(classOf[OAuthTokenCleaner]).constructor("0 */10 * * * *").lazyInit(false)
  }
}
