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

package org.beangle.ems.app.web

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.beangle.commons.lang.Strings
import org.beangle.security.authc.Profile
import org.beangle.security.web.CookieKeys
import org.beangle.web.servlet.util.CookieUtils
import org.beangle.webmvc.context.Params

object EmsCookie {

  def check(profiles: collection.Seq[Profile], profileId: String): Option[String] = {
    if (Strings.isNotBlank(profileId)) {
      val hit = profiles.exists(_.id.toString == profileId)
      if (hit) return Some(profileId)
    }
    if (profiles.nonEmpty) Some(profiles.head.id.toString) else None
  }

  def add(request: HttpServletRequest, response: HttpServletResponse, profileId: String): Unit = {
    CookieUtils.addCookie(request, response, CookieKeys.ProfileIdKey, profileId, "/", COOKIE_AGE)
  }

  /**
   * 解析当前 profileId：优先 request 参数（profile / contextProfileId），否则读 cookie。
   * 仅 contextProfileId 会写回 cookie（与历史行为一致）。
   */
  def get(request: HttpServletRequest, response: HttpServletResponse): Option[String] = {
    val cookieId = Option(CookieUtils.getCookieValue(request, CookieKeys.ProfileIdKey)).filter(Strings.isNotBlank)

    def param(name: String): Option[String] = {
      Option(request.getParameter(name)).map(_.trim).filter(Strings.isNotBlank)
        .orElse(Params.get(name).map(_.trim).filter(Strings.isNotBlank))
    }

    param("profile") match {
      case some@Some(_) => some
      case None =>
        param("contextProfileId") match {
          case Some(pid) =>
            if (!cookieId.contains(pid)) {
              EmsCookie.update(request, response, pid, true)
            }
            Some(pid)
          case None => cookieId
        }
    }
  }

  def update(request: HttpServletRequest, response: HttpServletResponse, profileId: String, create: Boolean): Unit = {
    Option(CookieUtils.getCookieValue(request, CookieKeys.ProfileIdKey)) match {
      case Some(exists) =>
        if (exists != profileId) {
          add(request, response, profileId)
        }
      case None =>
        if (create) {
          add(request, response, profileId)
        }
    }
  }

  private val COOKIE_AGE = 60 * 60 * 24 * 7 // 7 days

}
