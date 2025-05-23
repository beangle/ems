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

package org.beangle.ems.portal.action.admin

import org.beangle.commons.cdi.BindModule

class OAModule extends BindModule {

  protected override def binding(): Unit = {
    bind(classOf[oa.DocAction], classOf[oa.NoticeAction], classOf[oa.NewsAction])
    bind(classOf[oa.NoticeAuditAction])
    bind(classOf[oa.FlowAction], classOf[oa.FlowProcessAction], classOf[oa.FlowActiveProcessAction])
    bind(classOf[oa.SignatureAction], classOf[oa.TodoAction])
    bind(classOf[oa.MessageTemplateAction])
  }
}
