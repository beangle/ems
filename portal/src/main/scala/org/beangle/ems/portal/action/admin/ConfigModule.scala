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

package org.beangle.ems.portal.action.admin

import org.beangle.commons.cdi.BindModule

class ConfigModule extends BindModule {

  protected override def binding(): Unit = {
    bind(classOf[config.AppAction], classOf[config.AppGroupAction])
    bind(classOf[config.DbAction])
    bind(classOf[config.CredentialAction])
    bind(classOf[config.FileAction])

    bind(classOf[config.PortaletAction])
    bind(classOf[config.ThemeAction])
    bind(classOf[config.TextBundleAction])

    bind(classOf[config.BusinessAction])
    bind(classOf[config.RuleAction])
    bind(classOf[config.RuleMetaAction])
    bind(classOf[config.ThirdPartyAppAction])
  }
}
