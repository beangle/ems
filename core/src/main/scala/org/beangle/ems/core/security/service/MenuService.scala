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

import org.beangle.commons.collection.Properties
import org.beangle.ems.core.config.model.App
import org.beangle.ems.core.security.model.Menu
import org.beangle.ems.core.user.model.{Role, User}

trait MenuService {

  def getTopMenus(user: User): collection.Seq[Menu]

  def getTopMenus(app: App, user: User): collection.Seq[Menu]

  def getTopMenus(app: App, role: Role): collection.Seq[Menu]

  def getTopMenus(app: App): collection.Seq[Menu]

  def getMenus(app: App, role: Role): collection.Seq[Menu]

  def getMenus(app: App, user: User): collection.Seq[Menu]

  def getMenus(app: App): collection.Seq[Menu]

  def move(menu: Menu, location: Menu, index: Int): Unit

  def importFrom(app: App, xml: scala.xml.Node): Unit

  def convert(one: Menu, isEnName: Boolean): Properties

  def getDomainMenus(user:User,isEnName:Boolean):DomainMenus

}

case class AppMenus(app: Properties, menus: Iterable[Properties])

case class GroupMenus(group: Properties, appMenus: Iterable[AppMenus])

case class DomainMenus(domain: Properties, groups: Iterable[GroupMenus]);
