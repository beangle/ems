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
import org.beangle.commons.json.{Json, JsonArray, JsonObject}
import org.beangle.commons.net.http.HttpUtils.getText
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
      resources ++= Json.parseArray(res.getText).map(_.toString)
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
    val resourceJsons = Json.parseArray(content)
    resourceJsons.map { rj =>
      val r = rj.asInstanceOf[JsonObject]
      val roles = r.get("roles") match {
        case None => Set.empty[String]
        case Some(roleList) => roleList.asInstanceOf[JsonArray].map(_.toString).toSet
      }
      resources += Authority(r.getString("name"), r.getString("scope"), roles)
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
    convert2Org(Json.parseObject(json))
  }

  def getDomain(locale: Locale): Ems.Domain = {
    val json = getText(Ems.api + "/platform/config/domains.json?request_locale=" + locale.toString).getOrElse(null)
    val data = Json.parseObject(json)
    val domain = new Ems.Domain
    domain.id = data.getInt("id")
    domain.name = data.getString("name")
    domain.title = data.getString("title")
    domain.logoUrl = data.getString("logoUrl")
    domain.org = convert2Org(data.getObject("org"))
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
    val data = Json.parseObject(json)
    val primaryColor = data.getString("primaryColor")
    val navbarBgColor = data.getString("navbarBgColor")
    val searchBgColor = data.getString("searchBgColor")
    val gridbarBgColor = data.getString("gridbarBgColor")
    val gridBorderColor = data.getString("gridBorderColor")

    Ems.Theme(primaryColor, navbarBgColor, searchBgColor, gridbarBgColor, gridBorderColor)
  }
}
