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

import org.beangle.commons.cdi.BindModule
import org.beangle.ems.ws.security.{data, func}

class WSModule extends BindModule {

  override protected def binding(): Unit = {
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

    bind(classOf[user.AccountWS], classOf[user.AppWS], classOf[user.DimensionWS], classOf[user.AvatarWS])
    bind(classOf[user.RootWS], classOf[user.ProfileWS], classOf[user.CredentialWS], classOf[user.UserWS])

    bind(classOf[oauth.LoginWS])
  }
}
