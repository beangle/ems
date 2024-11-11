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

import com.google.gson.Gson
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.beangle.web.servlet.util.CookieUtils
import org.beangle.security.authc.Profile
import org.beangle.web.action.context.Params

import scala.collection.mutable

object EmsCookie {

  def check(profiles: Array[Profile], cookie: EmsCookie): Unit = {
    var finded = false
    var i = 0
    while (i < profiles.length && !finded) {
      finded = profiles(i).id == cookie.profile
      i += 1
    }
    if (!finded && profiles.length >= 0) {
      cookie.profile = profiles(0).id
    }
  }

  def add(request: HttpServletRequest, response: HttpServletResponse, profile: EmsCookie): Unit = {
    CookieUtils.addCookie(request, response, CookieName, profile.toJson, "/", COOKIE_AGE)
  }

  def get(request: HttpServletRequest, response: HttpServletResponse): EmsCookie = {
    val p = Option(CookieUtils.getCookieValue(request, CookieName)).map(parse).getOrElse(new EmsCookie)
    var newProfileId = 0L
    //尝试从uri中获取profile
    Params.getLong("profile") foreach { pid =>
      newProfileId = pid
    }
    if (0 == newProfileId) {
      //在尝试从参数中获取?contextProfileId=id,只有这个参数可以更新cookie
      Params.getLong("contextProfileId") foreach { pid =>
        newProfileId = pid
        if (newProfileId != 0 && p.profile != newProfileId) {
          p.profile = newProfileId
          EmsCookie.update(request, response, p, true)
        }
      }
    }
    if (0 != newProfileId) {
      p.profile = newProfileId
    }
    p
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

  private val CookieName = "URP_PROFILE"
  private val COOKIE_AGE = 60 * 60 * 24 * 7 // 7 days

  private def parse(cookieValue: String): EmsCookie = {
    val gson = new Gson
    val profile = new EmsCookie
    import scala.jdk.javaapi.CollectionConverters._
    val v = gson.fromJson(cookieValue, classOf[java.util.Map[String, String]])
    profile.data ++= asScala(v)
    profile
  }
}

class EmsCookie {

  var data = new mutable.HashMap[String, String]

  def profile: Long = {
    data.get("profile") match {
      case Some(p) => p.toLong
      case None => 0L
    }
  }

  def profile_=(id: Long): Unit = {
    data.put("profile", id.toString)
  }

  def put(key: String, value: String): Unit = {
    data.put(key, value)
  }

  def remove(key: String): Unit = {
    data.remove(key)
  }

  def contains(key: String): Boolean = {
    data.contains(key)
  }

  def toJson: String = {
    val gson = new Gson
    import scala.jdk.javaapi.CollectionConverters._
    gson.toJson(asJava(this.data))
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case that: EmsCookie => that.data == this.data
      case _ => false
    }
  }
}
