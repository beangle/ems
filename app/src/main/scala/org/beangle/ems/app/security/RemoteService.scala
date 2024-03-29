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

package org.beangle.ems.app.security

import org.beangle.commons.collection.Collections
import org.beangle.commons.net.http.HttpUtils.getText
import org.beangle.ems.app.util.JSON
import org.beangle.ems.app.{Ems, EmsApp}
import org.beangle.security.Securities
import org.beangle.security.authz.Authority

import java.util.Locale

/**
  * @author chaostone
  */
object RemoteService {

  def roots: Option[Set[String]] = {
    val url = Ems.api + "/platform/user/roots.json?app=" + EmsApp.name
    val res = getText(url)
    if (res.status == 200) {
      val resources = Collections.newSet[String]
      resources ++= JSON.parseSeq(res.getText).map(_.toString)
      Some(resources.toSet)
    } else {
      None
    }
  }

  def getAuthorities: collection.Seq[Authority] = {
    val url = Ems.api + "/platform/security/func/" + EmsApp.name + "/resources.json"
    toAuthorities(getText(url).getOrElse(null))
  }

  protected[security] def toAuthorities(content: String): collection.Seq[Authority] = {
    val resources = Collections.newBuffer[Authority]
    val resourceJsons = JSON.parseSeq(content)
    resourceJsons.map { rj =>
      val r = rj.asInstanceOf[collection.Map[String, _]]
      val roles = r.get("roles") match {
        case None => Set.empty[String]
        case Some(roleList) => roleList.asInstanceOf[Iterable[Number]].map(_.intValue.toString).toSet
      }
      resources += Authority(r("name").toString, r("scope").toString, roles)
    }
    resources
  }

  def getMenusJson(locale: Locale): String = {
    getText(Ems.api + "/platform/security/func/" + EmsApp.name + "/menus/user/" + Securities.user + ".json?request_locale=" + locale.toString).getOrElse(null)
  }

  def getDomainMenusJson(locale: Locale): String = {
    getText(Ems.api + "/platform/security/func/" + EmsApp.name + "/menus/user/" + Securities.user + ".json?forDomain=1&request_locale=" + locale.toString).getOrElse(null)
  }

  def getAppsJson: String = {
    getText(Ems.api + "/platform/user/apps/" + Securities.user + ".json").getOrElse(null)
  }

  def getProfiles(userCode: String, function: String): String = {
    val url = Ems.api + "/platform/user/profiles/" + userCode + ".json"
    getText(url).getOrElse(null)
  }

  def getOrg: Ems.Org = {
    val json = getText(Ems.api + "/platform/config/orgs.json").getOrElse(null)
    val data = JSON.parseObj(json)
    convert2Org(data)
  }

  def getDomain(locale: Locale): Ems.Domain = {
    val json = getText(Ems.api + "/platform/config/domains.json?request_locale=" + locale.toString).getOrElse(null)
    val data = JSON.parseObj(json)
    val domain = new Ems.Domain
    data.get("id") foreach (e => domain.id = e.asInstanceOf[Number].intValue)
    data.get("name") foreach (e => domain.name = e.toString)
    data.get("title") foreach (e => domain.title = e.toString)
    data.get("logoUrl") foreach (e => domain.logoUrl = e.toString)
    domain.org = convert2Org(data.get("org").orNull.asInstanceOf[collection.Map[String, Any]])
    domain
  }

  private def convert2Org(data: collection.Map[String, Any]): Ems.Org = {
    val org = new Ems.Org
    if null != data then
      data.get("id") foreach (e => org.id = e.asInstanceOf[Number].intValue)
      data.get("code") foreach (e => org.code = e.toString)
      data.get("name") foreach (e => org.name = e.toString)
      data.get("shortName") foreach (e => org.shortName = e.toString)
      data.get("logoUrl") foreach (e => org.logoUrl = e.toString)
      data.get("wwwUrl") foreach (e => org.wwwUrl = e.toString)
    org
  }

  def getTheme: Ems.Theme = {
    val json = getText(Ems.api + "/platform/config/themes.json").getOrElse(null)
    val data = JSON.parseObj(json)
    var primaryColor: String = null
    var navbarBgColor: String = null
    var searchBgColor: String = null
    var gridbarBgColor: String = null
    var gridBorderColor: String = null

    data.get("primaryColor") foreach (e => primaryColor = e.toString)
    data.get("navbarBgColor") foreach (e => navbarBgColor = e.toString)
    data.get("searchBgColor") foreach (e => searchBgColor = e.toString)
    data.get("gridbarBgColor") foreach (e => gridbarBgColor = e.toString)
    data.get("gridBorderColor") foreach (e => gridBorderColor = e.toString)
    Ems.Theme(primaryColor, navbarBgColor, searchBgColor, gridbarBgColor, gridBorderColor)
  }
}
