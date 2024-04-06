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

import org.beangle.cdi.bind.BindModule
import org.beangle.ems.app.Ems
import org.beangle.ids.cas.web.action.{LoginAction, LogoutAction}
import org.beangle.ids.cas.web.helper.CaptchaHelper
import org.beangle.ids.cas.web.ws.{ServiceValidateAction, SessionAction}

class DefaultModule extends BindModule {
  override def binding(): Unit = {
    bind(classOf[LoginAction])
    bind(classOf[ServiceValidateAction])
    bind(classOf[LogoutAction])
    bind(classOf[SessionAction])
    bind(classOf[EditAction])
    bind(classOf[CaptchaHelper]).constructor(Ems.api + "/tools")
  }
}
