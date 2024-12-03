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

package org.beangle.ems.portal.action.admin.config

import org.beangle.ems.core.config.model.{Business, Rule, RuleMeta, RuleParam}
import org.beangle.ems.core.config.service.DomainService
import org.beangle.event.bus.{DataEvent, DataEventBus}
import org.beangle.webmvc.support.action.RestfulAction
import org.beangle.webmvc.view.View

import java.time.Instant

class RuleAction extends RestfulAction[Rule] {
  var databus: DataEventBus = _

  var domainService: DomainService = _

  override def indexSetting(): Unit = {
    super.indexSetting()
    put("businesses", entityDao.getAll(classOf[Business]))
  }

  override protected def editSetting(rule: Rule): Unit = {
    val metas = getLong("rule.meta.business.id") match
      case Some(bid) => entityDao.findBy(classOf[RuleMeta], "business.id", bid)
      case None => entityDao.getAll(classOf[RuleMeta])

    if (!rule.persisted) {
      metas.headOption foreach { m =>
        rule.meta = m
        rule.name = m.name
      }
    }
    put("metas", metas)
    super.editSetting(rule)
  }

  override protected def saveAndRedirect(rule: Rule): View = {
    val meta = entityDao.get(classOf[RuleMeta], rule.meta.id)

    val paramMetaIds = meta.params.map(_.id).toSet
    val outdated = rule.params.filter(x => !paramMetaIds.contains(x.meta.id))
    rule.params.subtractAll(outdated)

    var i = 0
    while (i < meta.params.size) {
      val ruleMetaId = getLong("param" + i + ".meta.id").getOrElse(0L)
      if (ruleMetaId > 0) {
        val paramId = getLong("param" + i + ".id").getOrElse(0L)
        val param = if (paramId > 0) entityDao.get(classOf[RuleParam], paramId) else new RuleParam
        populate(param, "param" + i)
        if (!param.persisted) {
          param.rule = rule
          rule.params.addOne(param)
        }
      }
      i += 1
    }
    rule.updatedAt = Instant.now
    rule.domain = domainService.getDomain
    entityDao.saveOrUpdate(rule)
    databus.publish(DataEvent.update(rule))
    super.saveAndRedirect(rule)
  }

  def metaInfo(): View = {
    val metaId = getLongId("meta");
    put("meta", entityDao.get(classOf[RuleMeta], metaId))
    forward()
  }
}
