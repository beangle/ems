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

package org.beangle.ems.app.rule

import org.beangle.commons.bean.Properties
import org.beangle.commons.cdi.{Container, ContainerAware}
import org.beangle.commons.collection.Collections
import org.beangle.data.model.util.Populator
import org.springframework.beans.BeansException
import org.springframework.context.{ApplicationContext, ApplicationContextAware}

class DefaultRuleCheckerBuilder extends RuleExecutorBuilder, ContainerAware {
  var container: Container = null

  override def build(rule: Rule): RuleExecutor = {
    container.getBean(rule.name) match {
      case Some(b) => b.asInstanceOf[RuleExecutor]
      case None =>
        val nr = Class.forName(rule.name).newInstance.asInstanceOf[RuleExecutor]
        for (p <- rule.params) {
          Properties.copy(nr, p._1, p._2)
        }
        nr
    }
  }

  override def build(rules: Iterable[Rule], stopWhenFail: Boolean): Map[Rule, RuleExecutor] = {
    val executors = Collections.newMap[Rule, RuleExecutor]
    for (rule <- rules) {
      executors.put(rule, this.build(rule))
    }
    executors.toMap
  }
}
