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

package org.beangle.ems.app.log

import org.beangle.commons.cdi.BindModule
import org.beangle.ems.app.Ems

class DefaultModule extends BindModule {

  override def binding(): Unit = {
    //val layout = new PatternLayout("%operateAt|%app|%entry|%summary|%operator|%resources|%details|%ip|%agent")
    bind(classOf[AsyncAppLogger])
      .property("appenders",
        List(new RemoteAppender(Ems.innerApi + s"/platform/log/push")))

    //如果不是开发环境，则启用日志上报功能
    if (!devEnabled) {
      bind(classOf[LogExceptionHandler]).primary()
    }
  }
}
