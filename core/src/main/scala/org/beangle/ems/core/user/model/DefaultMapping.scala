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

package org.beangle.ems.core.user.model

import org.beangle.data.orm.{IdGenerator, MappingModule}

object DefaultMapping extends MappingModule {

  def binding(): Unit = {
    defaultIdGenerator(classOf[Int], IdGenerator.AutoIncrement)
    defaultIdGenerator(classOf[Long], IdGenerator.AutoIncrement)
    defaultCache("ems.security", "read-write")

    bind[Dimension].declare { e =>
      e.name & e.title are length(40)
      e.source.is(column("source_"), length(6000))
      e.keyName is length(20)
      e.properties is length(100)
      index("idx_dimension_name", true, e.domain, e.name)
    }

    bind[RoleMember].declare { e =>
      e.member is column("is_member")
      e.granter is column("is_granter")
      e.manager is column("is_manager")
      index("idx_role_member_user", false, e.user)
    }

    bind[Role].declare { e =>
      e.getName is length(100)
      e.children is depends("parent")
      e.members is depends("role")
      e.properties is eleLength(2000)
      index("idx_role_name", true, e.domain, e.name)
    }

    bind[User].declare { e =>
      e.code is length(30)
      e.getName is length(100)
      e.remark is length(100)
      e.roles is depends("user")
      e.groups is depends("user")
      e.mobile is length(15)
      e.openid is length(60)
      index("idx_user_code", true, e.org, e.code)
    }

    bind[PasswordConfig].declare { e =>
      index("idx_password_config", true, e.domain)
    }

    bind[Category].declare { e =>
      e.code.is(length(30), unique)
      e.name is length(100)
      index("idx_user_category", true, e.org, e.name)
    }

    bind[Profile].declare { e =>
      e.properties is eleLength(2000)
      index("idx_user_profile", false, e.user, e.domain)
    }

    bind[GroupMember].declare { e =>
      index("", true, e.group, e.user)
    }

    bind[Group].declare { e =>
      e.name is length(100)
      e.children is depends("parent")
      index("idx_group", true, e.org, e.code)
    }

    bind[Avatar].declare { e =>
      e.id is length(50)
      e.fileName is length(50)
      e.filePath is length(300)
    }.generator(IdGenerator.Assigned)

    bind[Depart].declare { e =>
      e.name is length(300)
      e.shortName is length(200)
      e.code is length(20)
      e.indexno is length(20)
      e.children is depends("parent")
    }
    bind[Root]

    all.except(classOf[RoleMember], classOf[GroupMember], classOf[User], classOf[Profile]).cacheAll()
  }

}
