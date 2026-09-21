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

import org.beangle.data.dao.OqlBuilder
import org.beangle.data.model.IntId
import org.beangle.data.model.pojo.{TemporalOn, Updatable}
import org.beangle.ems.core.config.model.{App, Domain}

import java.time.LocalDate
import scala.compiletime.uninitialized

/**
 * @author chaostone
 */
class Root extends IntId, Updatable, TemporalOn {
  var domain: Domain = uninitialized
  var user: User = uninitialized
}

object Root {

  /** 追加根用户记录当前有效的查询条件 */
  def activeWhere(builder: OqlBuilder[?], alias: String = "r"): Unit = {
    builder.where(
      s"$alias.beginOn <= :now and ($alias.endOn is null or $alias.endOn >= :now)",
      LocalDate.now)
  }
}
