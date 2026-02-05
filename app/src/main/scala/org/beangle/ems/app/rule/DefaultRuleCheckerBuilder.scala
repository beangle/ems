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
import org.beangle.commons.cdi.Container
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.reflect.Reflections

class DefaultRuleCheckerBuilder extends RuleCheckerBuilder {

  override def build(rule: Rule): RuleChecker = {
    val container = Container.get("ROOT")
    val checker = container.getBean[Object](rule.name) match {
      case Some(b) => populate(b, rule)
      case None => populate(Reflections.newInstance[Object](rule.name), rule)
    }

    val methods = checker.getClass.getMethods.toIndexedSeq.filter(x => x.getName == "check" && x.getReturnType == classOf[(_, _)])
    if (methods.isEmpty) {
      throw new IllegalArgumentException("rule " + rule.name + " has no check method")
    } else {
      new ProxyRuleChecker(checker, methods.head)
    }
  }

  private def populate(checker: Object, rule: Rule): Object = {
    for (p <- rule.params) {
      Properties.copy(checker, p._1, p._2)
    }
    checker
  }

  override def build(rules: Iterable[Rule], stopWhenFail: Boolean): Map[Rule, RuleChecker] = {
    val executors = Collections.newMap[Rule, RuleChecker]
    for (rule <- rules) {
      executors.put(rule, this.build(rule))
    }
    executors.toMap
  }
}
