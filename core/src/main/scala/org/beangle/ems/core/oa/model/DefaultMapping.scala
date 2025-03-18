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

package org.beangle.ems.core.oa.model

import org.beangle.data.orm.{IdGenerator, MappingModule}

object DefaultMapping extends MappingModule {

  override def binding(): Unit = {
    defaultIdGenerator(classOf[Long], IdGenerator.DateTime)
    defaultCache("ems.security", "read-write")

    bind[Doc].cacheable()

    bind[News].declare { e =>
      e.contents is lob
    }.cacheable()

    bind[Notice].declare { e =>
      e.contents is lob
      e.issuer is length(40)
    }.cacheable()

    bind[SensitiveWord].declare { e =>
      e.contents is length(30)
    }

    bind[Message].declare { e =>
      e.contents is length(1000)
      index("", false, e.sender)
      index("", false, e.recipient)
    }

    bind[Flow].declare { e =>
      e.activities is depends("flow")
      e.envJson is length(2000)
      e.flowJson is length(8000)
      e.guardJson is length(300)
    }.cacheable()

    bind[FlowActivity].declare { e =>
      e.assignees is length(300)
      e.depart is length(50)
    }.cacheable()

    bind[FlowActiveProcess].declare { e =>
      e.tasks is depends("process")
    }
    bind[FlowActiveTask]

    bind[FlowProcess].declare { e =>
      e.tasks is depends("process")
    }.generator(IdGenerator.Assigned)

    bind[FlowTask].declare { e =>
      e.comments is depends("task")
      e.attachments is depends("task")
      e.assignees is length(500)
    }.generator(IdGenerator.Assigned)

    bind[FlowComment].declare { e =>
      e.messages is length(4000)
    }

    bind[FlowAttachment].declare { e =>
      e.name is length(300)
      e.filePath is length(500)
    }

    bind[Notification]
    bind[Todo]

    bind[Signature].declare { e =>
      index("", true, e.user)
    }
  }
}
