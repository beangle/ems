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
package org.beangle.ems.core.security.model

import org.beangle.data.orm.{IdGenerator, MappingModule}
import org.beangle.ems.core.config.model.DefaultMapping.defaultIdGenerator

object DefaultMapping extends MappingModule {

  def binding(): Unit = {
    defaultIdGenerator(classOf[Int], IdGenerator.AutoIncrement)
    defaultIdGenerator(classOf[Long], IdGenerator.AutoIncrement)

    defaultCache("ems.security", "read-write")

    bind[FuncPermission].declare { e =>
      e.role & e.resource & e.beginAt are notnull
      e.actions is length(100)
      e.restrictions is length(100)
      e.remark is length(100)
    }

    bind[Menu].declare { e =>
      e.app & e.indexno & e.name & e.title are notnull
      e.name & e.title & e.remark are length(100)
      e.indexno is length(50)
      e.children is(depends("parent"), orderby("indexno"))
      e.params is length(200)
    }

    bind[FuncResource].declare { e =>
      e.name is(notnull, length(200))
      e.app & e.scope are notnull
      e.scope is column("scope_")
      e.title is(notnull, length(200))
      e.remark & e.actions are length(200)
    }

    bind[DataResource].declare { e =>
      e.name & e.typeName are length(200)
      e.title is length(200)
      e.scope is column("scope_")
      e.remark & e.actions are length(200)
    }

    bind[DataPermission].declare { e =>
      e.description is length(100)
      e.filters is length(600)
    }

    bind[AppPermission].declare { e =>
      e.app & e.resource are notnull
      e.actions is length(500)
      e.restrictions is length(500)
    }

    all.cacheAll()
  }

}
