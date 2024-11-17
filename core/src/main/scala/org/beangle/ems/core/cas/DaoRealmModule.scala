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
import org.beangle.security.authc.{DefaultAccountRealm, RealmAuthenticator}
import org.beangle.ems.core.cas.service.DaoAccountStore

class DaoRealmModule extends BindModule {
  override def binding(): Unit = {
    bind("security.Realm.default", classOf[DefaultAccountRealm])
      .constructor(bean(classOf[DaoAccountStore]), ref("security.CredentialsChecker.default"))
    bind("security.Authenticator", classOf[RealmAuthenticator])
      .constructor(List(ref("security.Realm.default"))).wiredEagerly(false)
  }
}
