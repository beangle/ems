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
import org.beangle.commons.json.{Json, JsonObject}
import org.beangle.web.servlet.util.CookieUtils

object EmsCookie {

  def add(request: HttpServletRequest, response: HttpServletResponse, profile: EmsCookie): Unit = {
    CookieUtils.addCookie(request, response, CookieName, profile.toJson, "/", COOKIE_AGE)
  }

  def get(request: HttpServletRequest, response: HttpServletResponse): EmsCookie = {
    Option(CookieUtils.getCookieValue(request, CookieName)).map(parse).getOrElse(new EmsCookie)
  }

  def update(request: HttpServletRequest, response: HttpServletResponse, profile: EmsCookie, create: Boolean): Unit = {
    Option(CookieUtils.getCookieValue(request, CookieName)).map(parse) match {
      case Some(exists) =>
        if (exists != profile) {
          add(request, response, profile)
        }
      case None =>
        if (create) {
          add(request, response, profile)
        }
    }
  }

  private val CookieName = "beangle.ems.context"
  private val COOKIE_AGE = 60 * 60 * 24 * 7 // 7 days

  private def parse(cookieValue: String): EmsCookie = {
    val profile = new EmsCookie
    Json.parseObject(cookieValue) foreach { case (k, v) =>
      profile.data.add(k, v.toString)
    }
    profile
  }
}

class EmsCookie {

  private var data = new JsonObject

  def put(key: String, value: String): Unit = {
    data.add(key, value)
  }

  def remove(key: String): Unit = {
    data.remove(key)
  }

  def apply(key: String): String = {
    data(key).toString
  }

  def get(key: String): Option[String] = {
    data.get(key).map(_.toString)
  }

  def contains(key: String): Boolean = {
    data.contains(key)
  }

  def toJson: String = {
    this.data.toJson
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case that: EmsCookie => that.data == this.data
      case _ => false
    }
  }
}
