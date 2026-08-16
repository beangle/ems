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

package org.beangle.ems.core.security.service

import org.beangle.commons.cdi.BindModule
import org.beangle.ems.core.security.service.impl.*

class DefaultModule extends BindModule {

  override def binding(): Unit = {
    // FuncPermissionServiceImpl / MenuServiceImpl 依赖 UserService，改注入字段后需重编本 Module
    bind(classOf[FuncPermissionServiceImpl])
    bind(classOf[MenuServiceImpl])
    bind(classOf[ProfileServiceImpl])
    bind(classOf[SessionInfoServiceImpl])
    bind(classOf[CategorySessionProfileImpl])

    bind(classOf[LoginEventTracker])
    bind(classOf[LogoutEventTracker])
    bind(classOf[OverTypeLoginEventTracker])
  }
}
