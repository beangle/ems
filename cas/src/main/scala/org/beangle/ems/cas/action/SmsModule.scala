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

import org.beangle.commons.cdi.BindModule
import org.beangle.commons.bean.{Initializing, Properties}
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.reflect.Reflections
import org.beangle.ems.app.EmsApp
import org.beangle.ids.cas.web.action.SmsLoginAction
import org.beangle.ids.sms.service.impl.{DefaultSmsCacheService, DefaultSmsCodeService}
import org.beangle.notify.sms.SmsSender

import java.io.FileInputStream

class SmsModule extends BindModule {
  override def binding(): Unit = {

    EmsApp.getAppFile foreach { file =>
      val is = new FileInputStream(file)
      val app = scala.xml.XML.load(is)
      (app \\ "sms") foreach { e =>
        bind(classOf[SmsLoginAction])
        bind(classOf[DefaultSmsCacheService])
        bind(classOf[DefaultSmsCodeService])

        val senderClass = getAttribute(e, "class")
        val sender = Reflections.newInstance[SmsSender](senderClass)

        e.attributes foreach { m =>
          if m.key != "class" then Properties.copy(sender, m.key, getAttribute(e, m.key))
        }
        sender match
          case i: Initializing => i.init()
          case _ =>

        bind("smsSender", sender)
      }
    }
  }

  private def getAttribute(e: scala.xml.Node, name: String): String = {
    val v = (e \ ("@" + name)).text.trim
    if Strings.isEmpty(v) then "" else v
  }
}
