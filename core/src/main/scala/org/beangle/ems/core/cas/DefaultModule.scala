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

import org.beangle.cdi.PropertySource
import org.beangle.cdi.bind.BindModule
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings
import org.beangle.ems.app.{Ems, EmsApp}
import org.beangle.ids.cas.CasSetting
import org.beangle.security.authz.PublicAuthorizer
import org.beangle.security.realm.cas.{CasConfig, CasEntryPoint, CasPreauthFilter, DefaultTicketValidator}
import org.beangle.security.realm.ltpa.{LtpaConfig, LtpaPreauthFilter, LtpaTokenGenerator}
import org.beangle.security.web.access.{AuthorizationFilter, DefaultAccessDeniedHandler, DefaultSecurityContextBuilder, SecurityInterceptor}
import org.beangle.security.web.{UrlEntryPoint, WebSecurityManager}

import java.io.FileInputStream

/**
  * @author chaostone
  */
class DefaultModule extends BindModule with PropertySource {

  private val clients = Collections.newBuffer[String]

  private var remoteCasServer: Option[String] = None

  private var remoteLtpa: Option[LtpaConfig] = None

  override def binding(): Unit = {
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
        bind("security.EntryPoint.url", classOf[UrlEntryPoint]).constructor("/login").primary()
      }
    } else {
      // entry point
      bind("security.EntryPoint.url", classOf[UrlEntryPoint]).constructor("/login").primary()
    }

    //interceptor
    bind("security.AccessDeniedHandler.default", classOf[DefaultAccessDeniedHandler])
      .constructor($("security.access.errorPage", "/403.html"))
    bind("security.Filter.authorization", classOf[AuthorizationFilter])

    val interceptor = bind("web.Interceptor.security", classOf[SecurityInterceptor])
    var filters = List(ref("security.Filter.authorization"))
    if (remoteCasServer.isDefined || remoteLtpa.isDefined) {
      filters = List(ref("security.Filter.Preauth"), ref("security.Filter.authorization"))
    }
    interceptor.property("filters", filters)

    //authorizer and manager
    bind("security.SecurityManager.default", classOf[WebSecurityManager])
    bind(classOf[DefaultSecurityContextBuilder])
    bind("security.Authorizer.public", PublicAuthorizer)

    val setting = bind("casSetting", classOf[CasSetting])
      .property("enableCaptcha", $("login.enableCaptcha"))
      .property("forceHttps", $("login.forceHttps"))
      .property("displayLoginSwitch", $("login.displayLoginSwitch", "false"))
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

  override def properties: collection.Map[String, String] = {
    val datas = Collections.newMap[String, String]
    EmsApp.getAppFile foreach { file =>
      val is = new FileInputStream(file)
      val app = scala.xml.XML.load(is)
      (app \\ "ldap") foreach { e =>
        datas += ("ldap.url" -> (e \\ "url").text.trim)
        datas += ("ldap.user" -> (e \\ "user").text.trim)
        datas += ("ldap.password" -> (e \\ "password").text.trim)
        datas += ("ldap.base" -> (e \\ "base").text.trim)
        datas += ("login.passwordReadOnly" -> "true") //本系统使用DB，一般使用LDAP即为外部密码库,禁止修改
      }
      (app \\ "redis") foreach { e =>
        datas += ("redis.host" -> (e \\ "host").text.trim)
        datas += ("redis.port" -> (e \\ "port").text.trim)
      }
      (app \\ "config" \\ "login") foreach { n =>
        val e = n.asInstanceOf[scala.xml.Elem]
        datas += ("login.enableCaptcha" -> getAttribute(e, "enableCaptcha", "false"))
        datas += ("login.forceHttps" -> getAttribute(e, "forceHttps", "false"))
        datas += ("login.key" -> getAttribute(e, "key", Ems.base))
        datas += ("login.origin" -> getAttribute(e, "origin", Ems.base))
        datas += ("login.checkPasswordStrength" -> getAttribute(e, "checkPasswordStrength", "true"))
        datas += ("login.enableSmsLogin" -> getAttribute(e, "enableSmsLogin", "false"))
        getAttribute(e, "passwordReadOnly") match {
          case Some(pronly) => datas += ("login.passwordReadOnly" -> pronly)
          case None => datas.getOrElseUpdate("login.passwordReadOnly", "false")
        }
      }

      if (!datas.contains("login.origin")) {
        datas += ("login.key" -> Ems.base)
        datas += ("login.origin" -> Ems.base)
      }
      (app \\ "config" \\ "client") foreach { c =>
        clients += getAttribute(c, "base", null)
      }
      //在项目的配置文件中出现remote/cas节点的情况下才配置如下信息
      (app \\ "config" \\ "remote") foreach { r =>
        (r \ "cas") foreach { e =>
          val casServer = getAttribute(e, "server", null)
          val gateway = getAttribute(e, "gateway", "false")
          datas += ("remote.cas.server" -> casServer)
          datas += ("remote.cas.gateway" -> gateway)
          remoteCasServer = Some(casServer)
        }
        (r \ "ltpa") foreach { e =>
          val server = getAttribute(e, "server", null)
          val key = getAttribute(e, "key", null)
          val cookieName = getAttribute(e, "cookieName", null)
          val usernameDns = getAttribute(e, "usernameDns", null)
          val config = LtpaConfig(server, key, cookieName, usernameDns)
          if null != server && null != key then remoteLtpa = Some(config)
        }
      }
      is.close()
    }
    datas.toMap
  }

  private def getAttribute(e: scala.xml.Node, name: String): Option[String] = {
    val v = (e \ ("@" + name)).text.trim
    if Strings.isEmpty(v) then None else Some(v)
  }

  private def getAttribute(e: scala.xml.Node, name: String, defaultValue: String): String = {
    val v = (e \ ("@" + name)).text.trim
    if Strings.isEmpty(v) then defaultValue else v
  }
}
