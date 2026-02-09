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

import org.beangle.commons.bean.{Initializing, Properties}
import org.beangle.commons.cdi.BindModule
import org.beangle.commons.lang.reflect.Reflections
import org.beangle.commons.xml.Document
import org.beangle.ems.app.EmsApp
import org.beangle.ids.cas.web.action.SmsLoginAction
import org.beangle.notify.sms.{DefaultSmsCodeService, SmsSender}

import java.io.FileInputStream

class SmsModule extends BindModule {
  override def binding(): Unit = {

    EmsApp.getAppFile foreach { file =>
      val is = new FileInputStream(file)
      val app = Document.parse(is)
      (app \\ "sms") foreach { e =>
        bind(classOf[SmsLoginAction])
        bind(classOf[DefaultSmsCodeService])
        val sender = Reflections.newInstance[SmsSender](e("class"))
        e.attrs foreach { (k, v) =>
          if k != "class" then Properties.copy(sender, k, v)
        }
        sender match
          case i: Initializing => i.init()
          case _ =>

        bind("smsSender", sender)
      }
    }
  }
}
