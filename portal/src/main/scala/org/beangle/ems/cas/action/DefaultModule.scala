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

package org.beangle.ems.cas.action

import org.beangle.commons.cdi.BindModule
import org.beangle.ems.app.Ems
import org.beangle.ids.cas.service.LoginRetryServiceImpl
import org.beangle.ids.cas.web.action.{AuthAction, LoginAction, LogoutAction, SmsLoginAction}
import org.beangle.ids.cas.web.helper.CaptchaHelper
import org.beangle.ids.cas.web.ws.{ServiceValidateAction, SessionAction}
import org.beangle.notify.sms.SmsCodeService

class DefaultModule extends BindModule {
  override def binding(): Unit = {
    bind(classOf[IndexAction])
    //standard cas action
    bind(classOf[LoginAction])
    bind(classOf[SmsLoginAction]).onExist(classOf[SmsCodeService])
    bind(classOf[ServiceValidateAction])
    bind(classOf[LogoutAction])

    //front end login
    bind(classOf[AuthAction])

    bind(classOf[SessionAction])
    bind(classOf[EditAction])
    bind(classOf[OAuthAction])
    bind(classOf[CaptchaHelper]).constructor(Ems.innerApi + "/tools")
    bind(classOf[LoginRetryServiceImpl]).constructor(ref("redis.Factory"))
  }
}
