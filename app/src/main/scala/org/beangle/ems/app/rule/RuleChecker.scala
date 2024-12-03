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

import java.lang.reflect.Method

trait RuleChecker {
  def check(context: Seq[Any]): (Boolean, String)
}

trait RuleCheckerBuilder {
  def build(rule: Rule): RuleChecker

  def build(rules: Iterable[Rule], stopWhenFail: Boolean): Map[Rule, RuleChecker]
}

class ProxyRuleChecker(checker: AnyRef, method: Method) extends RuleChecker {
  def check(context: Seq[Any]): (Boolean, String) = {
    method.invoke(checker, context: _*).asInstanceOf[(Boolean, String)]
  }
}
