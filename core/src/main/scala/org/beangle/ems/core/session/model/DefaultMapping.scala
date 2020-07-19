/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2020, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.ems.core.session.model

import org.beangle.data.orm.MappingModule

object DefaultMapping extends MappingModule {

  def binding(): Unit = {
    bind[SessionEvent].declare { e =>
      e.principal & e.username & e.name are length(100)
      e.detail is length(1000)
    }

    bind[SessionConfig].declare { e =>
      index("idx_session_config", true, e.domain, e.category)
    }
  }

}
