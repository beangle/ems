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

package org.beangle.ems.portal.action.admin.security

import org.beangle.commons.lang.Strings
import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.core.config.model.{App, ChannelType, EmbedMode}
import org.beangle.ems.core.security.model.{Channel, Menu}
import org.beangle.ems.portal.action.admin.DomainSupport
import org.beangle.event.bus.DataEvent
import org.beangle.she.webmvc.RestfulAction
import org.beangle.webmvc.annotation.ignore
import org.beangle.webmvc.view.View

/** 应用菜单端（Channel）维护 */
class ChannelAction extends RestfulAction[Channel], DomainSupport {

  private val appParam = "channel.app.id"

  protected override def indexSetting(): Unit = {
    put("apps", appService.getApps)
    put("channelTypes", entityDao.getAll(classOf[ChannelType]).sortBy(_.id))
  }

  override def search(): View = {
    super.search()
    forward()
  }

  override def getQueryBuilder: OqlBuilder[Channel] = {
    val builder = super.getQueryBuilder
    builder.where("channel.app.domain=:domain", domainService.getDomain)
    getInt("embedMode.id").foreach { id =>
      builder.where("channel.embedMode=:embedMode", EmbedMode.fromId(id))
    }
    builder.orderBy("channel.app.indexno,channel.channelType.id")
    builder
  }

  protected override def editSetting(channel: Channel): Unit = {
    getInt(appParam).orElse(getInt("app.id")) foreach { id =>
      if (channel.app == null) channel.app = entityDao.get(classOf[App], id)
    }
    if (!channel.persisted) {
      channel.enabled = true
      channel.embedMode = EmbedMode.Iframe
      if (channel.app != null && Strings.isBlank(channel.base)) {
        channel.base = channel.app.base
      }
    }
    put("apps", appService.getApps)
    put("channelTypes", entityDao.getAll(classOf[ChannelType]).sortBy(_.id))
  }

  @ignore
  protected override def saveAndRedirect(channel: Channel): View = {
    getInt("embedMode.id").foreach { id =>
      channel.embedMode = EmbedMode.fromId(id)
    }
    if (Strings.isBlank(channel.base) && channel.app != null) {
      channel.base = channel.app.base
    }
    val builder = OqlBuilder.from[Int](classOf[Channel].getName, "c")
      .where("c.app=:app and c.channelType=:channelType", channel.app, channel.channelType)
      .select("c.id")
    val ids = entityDao.search(builder)
    if (!channel.persisted && ids.nonEmpty || channel.persisted && ids.nonEmpty && !ids.contains(channel.id)) {
      return redirect("edit", "该应用下此端类型已存在")
    }
    entityDao.saveOrUpdate(channel)
    databus.publish(DataEvent.update(channel))
    redirect("search", "info.save.success")
  }

  @ignore
  protected override def removeAndRedirect(entities: Seq[Channel]): View = {
    val menus = entityDao.search(OqlBuilder.from(classOf[Menu], "m").where("m.channel in (:channels)", entities))
    menus.foreach(_.parent = None)
    if (menus.nonEmpty) {
      entityDao.saveOrUpdate(menus)
      entityDao.remove(menus)
      databus.publish(DataEvent.remove(menus))
    }
    entityDao.remove(entities)
    databus.publish(DataEvent.remove(entities))
    redirect("search", "info.remove.success")
  }
}
