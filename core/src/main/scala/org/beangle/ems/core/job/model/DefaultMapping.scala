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

package org.beangle.ems.core.job.model

import org.beangle.data.orm.MappingModule

object DefaultMapping extends MappingModule {

  override def binding(): Unit = {

    bind[CronTask].declare { e =>
      e.name is length(100)
      e.target is length(500)
      e.description is length(500)
      e.command is length(500)
      e.expression is length(100)
      index("idx_cron_task", true, e.domain, e.name)
    }

    bind[CronTaskLog].declare { e =>
      e.resultFilePath is length(500)
    }
  }

}
