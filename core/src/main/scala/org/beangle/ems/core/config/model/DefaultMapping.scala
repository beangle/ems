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

package org.beangle.ems.core.config.model

import org.beangle.data.orm.{IdGenerator, MappingModule}

object DefaultMapping extends MappingModule {

  def binding(): Unit = {
    defaultIdGenerator(classOf[Int], IdGenerator.AutoIncrement)
    defaultIdGenerator(classOf[Long], IdGenerator.AutoIncrement)
    defaultCache("ems-security", "read-write")

    bind[Org].declare { e =>
      e.code.is(length(50), unique)
      e.name & e.shortName are length(100)
      e.wwwUrl & e.logoUrl are length(200)
    }

    bind[App].declare { e =>
      e.getName is length(100)
      e.title is length(100)
      e.secret is length(200)
      e.url is length(200)
      e.navStyle is length(50)
      e.remark is length(200)
      e.indexno is length(50)
      e.datasources is depends("app")
      index("idx_app", true, e.domain, e.name)
    }

    bind[AppGroup].declare { e =>
      e.name is length(100)
      index("idx_app_group", true, e.domain, e.name)
    }

    bind[Credential].declare { e =>
      e.username is length(100)
      e.password is length(200)
      e.name is length(100)
      index("idx_credential", true, e.domain, e.name)
    }

    bind[DataSource].declare { e =>
      e.name is length(100)
      e.remark is length(200)
      index("idx_datasource", true, e.app, e.name)
    }

    bind[Db].declare { e =>
      e.name.is(length(100), unique)
      e.driver is length(100)
      e.databaseName & e.serverName is length(100)
      e.url is length(200)
      e.remark is length(200)
      index("idx_db", true, e.domain, e.name)
    }

    bind[Domain].declare { e =>
      e.name.is(length(100), unique)
      e.hostname.is(length(100), unique)
      e.title is length(200)
      index("idx_domain", true, e.org, e.hostname)
    }

    bind[AppType].declare { e =>
      index("", true, e.name)
    }

    bind[Portalet]

    bind[File].declare { e =>
      index("", true, e.app, e.name)
    }

    bind[Theme].declare { e =>
      e.gridBorderColor & e.gridbarBgColor & e.navbarBgColor & e.searchBgColor are length(15)
    }

    bind[TextBundle] declare { e =>
      e.texts is lob
    }

    //rule
    bind[Business].declare { e =>
      e.code is length(50)
      e.name is length(100)
    }.generator(IdGenerator.AutoIncrement)

    bind[RuleMeta] declare { e =>
      e.name is length(50)
      e.title is length(80)
      e.description is length(500)
      e.params is depends("ruleMeta")
    }

    bind[RuleParamMeta] declare { e =>
      e.name is length(50)
      e.title is length(80)
      e.description is length(200)
    }

    bind[Rule] declare { e =>
      e.params is depends("rule")
    }

    bind[RuleParam] declare { e =>
      e.contents is length(500)
    }

    all.cacheAll()
  }

}
