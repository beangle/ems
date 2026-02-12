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

import org.beangle.commons.cdi.{BindModule, Binder}
import org.beangle.commons.collection.Collections
import org.beangle.commons.config.Config
import org.beangle.commons.xml.Document
import org.beangle.ems.app.{Ems, EmsApp}
import org.beangle.ids.cas.CasSetting
import org.beangle.security.authz.PublicAuthorizer
import org.beangle.security.realm.cas.{CasConfig, CasEntryPoint, CasPreauthFilter, DefaultTicketValidator}
import org.beangle.security.realm.ltpa.{LtpaConfig, LtpaPreauthFilter, LtpaTokenGenerator}
import org.beangle.security.realm.openid.OpenidPreauthFilter
import org.beangle.security.web.access.{AuthorizationFilter, DefaultAccessDeniedHandler, DefaultSecurityContextBuilder, SecurityInterceptor}
import org.beangle.security.web.{EntryPoint, UrlEntryPoint, WebSecurityManager}

import java.io.FileInputStream

/**
 * @author chaostone
 */
class DefaultModule extends BindModule, Config.Provider {

  private val clients = Collections.newBuffer[String]
  private var remoteCasServer: Option[String] = None
  private var remoteOpenidServer: Option[String] = None
  private var remoteLtpa: Option[LtpaConfig] = None

  override def binding(): Unit = {
    readAppFile()
    //1.如果有配置CAS方式的SSO
    if (remoteCasServer.isDefined) {
      bind(classOf[CasConfig]).constructor($("remote.cas.server"))
        .property("gateway", $("remote.cas.gateway"))
        .property("localLoginUri", "/login")
      bind("security.Filter.Preauth", classOf[CasPreauthFilter])
      bind(classOf[DefaultTicketValidator])
      bind(classOf[CasEntryPoint]).property("allowSessionIdAsParameter", false).shortName()
    } else if (remoteLtpa.isDefined) {
      remoteLtpa foreach { config =>
        bind("ltpaConfig", config)
        bind("ltpaTokenGenerator", classOf[LtpaTokenGenerator]).constructor(config.key, config.usernameDns)
        bind("security.Filter.Preauth", classOf[LtpaPreauthFilter])
        // entry point
        bind("security.EntryPoint.url", classOf[UrlEntryPoint]).constructor("/login").primaryOf(classOf[EntryPoint])
      }
    } else {
      // entry point
      bind("security.EntryPoint.url", classOf[UrlEntryPoint]).constructor("/login").primaryOf(classOf[EntryPoint])
    }

    remoteOpenidServer foreach { serverUrl =>
      bind("security.Filter.OpenidPreauth", classOf[OpenidPreauthFilter]).property("serviceUrl", serverUrl)
    }
    //interceptor
    bind("security.AccessDeniedHandler.default", classOf[DefaultAccessDeniedHandler])
      .constructor($("security.access.errorPage:/403.html"))
    bind("security.Filter.authorization", classOf[AuthorizationFilter])

    val interceptor = bind("web.Interceptor.security", classOf[SecurityInterceptor])
    val filters = Collections.newBuffer[Binder.Reference]
    if (remoteOpenidServer.isDefined) {
      filters.addOne(ref("security.Filter.OpenidPreauth"))
    }
    if (remoteCasServer.isDefined || remoteLtpa.isDefined) {
      filters.addOne(ref("security.Filter.Preauth"))
    }
    filters.addOne(ref("security.Filter.authorization"))
    interceptor.property("filters", filters.toList)

    //authorizer and manager
    bind("security.SecurityManager.default", classOf[WebSecurityManager])
    bind(classOf[DefaultSecurityContextBuilder])
    bind("security.Authorizer.public", PublicAuthorizer)

    val setting = bind("casSetting", classOf[CasSetting])
      .property("enableCaptcha", $("login.enableCaptcha"))
      .property("forceHttps", $("login.forceHttps"))
      .property("displayLoginSwitch", $("login.displayLoginSwitch:false"))
      .property("key", $("login.key"))
      .property("origin", $("login.origin"))
      .property("checkPasswordStrength", $("login.checkPasswordStrength"))
      .property("passwordReadOnly", $("login.passwordReadOnly"))
      .property("enableSmsLogin", $("login.enableSmsLogin"))
      .property("clients", List("http://localhost", Ems.base) ++ clients)

    remoteCasServer foreach { casServer =>
      val remoteCasServer = new CasConfig(casServer)
      setting.property("remoteLoginUrl", remoteCasServer.loginUrl)
      setting.property("remoteLogoutUrl", remoteCasServer.logoutUrl)
    }
    remoteLtpa foreach { config =>
      setting.property("remoteLoginUrl", config.loginUrl)
      setting.property("remoteLogoutUrl", config.logoutUrl)
    }
  }

  private def readAppFile(): Unit = {
    EmsApp.getAppFile foreach { file =>
      val is = new FileInputStream(file)
      val app = Document.parse(is)
      (app \\ "config" \\ "client") foreach { c =>
        clients += c("base")
      }
      //在项目的配置文件中出现remote/cas节点的情况下才配置如下信息
      (app \\ "config" \\ "remote") foreach { r =>
        (r \ "cas") foreach { e =>
          val casServer = e.get("server", null)
          remoteCasServer = Some(casServer)
        }
        (r \ "ltpa") foreach { e =>
          val server = e.get("server", null)
          val key = e.get("key", null)
          val cookieName = e.get("cookieName", null)
          val usernameDns = e.get("usernameDns", null)
          val config = LtpaConfig(server, key, cookieName, usernameDns)
          if null != server && null != key then remoteLtpa = Some(config)
        }
        (r \ "openid") foreach { e =>
          val server = e.get("server", null)
          remoteOpenidServer = Some(server)
        }
      }
      is.close()
    }
  }

  override def properties: collection.Map[String, String] = {
    val datas = Collections.newMap[String, String]
    EmsApp.getAppFile foreach { file =>
      val is = new FileInputStream(file)
      val app = Document.parse(is)
      (app \\ "ldap") foreach { e =>
        datas += ("ldap.url" -> (e \\ "url").text.trim)
        datas += ("ldap.user" -> (e \\ "user").text.trim)
        datas += ("ldap.password" -> (e \\ "password").text.trim)
        datas += ("ldap.base" -> (e \\ "base").text.trim)
        datas += ("login.passwordReadOnly" -> "true") //本系统使用DB，一般使用LDAP即为外部密码库,禁止修改
      }
      (app \\ "config" \\ "login") foreach { e =>
        datas += ("login.enableCaptcha" -> e.get("enableCaptcha", "false"))
        datas += ("login.forceHttps" -> e.get("forceHttps", "false"))
        datas += ("login.key" -> e.get("key", Ems.base))
        datas += ("login.origin" -> e.get("origin", Ems.base))
        datas += ("login.checkPasswordStrength" -> e.get("checkPasswordStrength", "true"))
        datas += ("login.enableSmsLogin" -> e.get("enableSmsLogin", "false"))
        e.get("passwordReadOnly") match {
          case Some(pronly) => datas += ("login.passwordReadOnly" -> pronly)
          case None => datas.getOrElseUpdate("login.passwordReadOnly", "false")
        }
      }

      if (!datas.contains("login.origin")) {
        datas += ("login.key" -> Ems.base)
        datas += ("login.origin" -> Ems.base)
      }
      //在项目的配置文件中出现remote/cas节点的情况下才配置如下信息
      (app \\ "config" \\ "remote") foreach { r =>
        (r \ "cas") foreach { e =>
          val casServer = e.get("server", null)
          val gateway = e.get("gateway", "false")
          datas += ("remote.cas.server" -> casServer)
          datas += ("remote.cas.gateway" -> gateway)
        }
      }
      is.close()
    }
    datas.toMap
  }
}
