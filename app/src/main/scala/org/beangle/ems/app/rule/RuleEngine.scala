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

import org.beangle.commons.collection.Collections
import org.beangle.commons.json.{JsonObject, JsonParser}
import org.beangle.commons.lang.Strings
import org.beangle.commons.net.http.HttpUtils
import org.beangle.ems.app.Ems

import scala.collection.mutable

class RuleEngine(val rules: Iterable[Rule], val stopWhenFail: Boolean = false) {

  var builder: RuleCheckerBuilder = new DefaultRuleCheckerBuilder

  def execute(context: Any*): List[(Rule, Boolean, String)] = {
    val executors = builder.build(rules, stopWhenFail)
    val results = Collections.newBuffer[(Rule, Boolean, String)]
    var result = true
    for (executor <- executors; if !this.stopWhenFail || result) {
      val rs = executor._2.check(context)
      result &= rs._1
      results.addOne((executor._1, rs._1, rs._2))
    }
    results.toList
  }

}

object RuleEngine {
  private val cache = new ThreadLocal[mutable.Map[String, RuleEngine]]

  def clearLocalCache(): Unit = {
    cache.remove()
  }

  def get(ruleIds: String, stopWhenFail: Boolean = false): RuleEngine = {
    if (cache.get == null) {
      cache.set(Collections.newMap[String, RuleEngine])
    }
    val key = ruleIds + stopWhenFail
    cache.get.get(key) match {
      case Some(engine) => engine
      case None =>
        val ng = of(ruleIds, stopWhenFail)
        cache.get.put(key, ng)
        ng
    }
  }

  def of(ruleIds: String, stopWhenFail: Boolean = false): RuleEngine = {
    if (Strings.isEmpty(ruleIds)) {
      new RuleEngine(Nil)
    } else {
      val url = Ems.api + s"/platform/config/rules/${ruleIds}.json"
      val json = HttpUtils.getText(url).getText
      val rules = parseToRules(json)
      new RuleEngine(rules, stopWhenFail)
    }
  }

  def list(ruleIds: String): Iterable[Rule] = {
    val url = Ems.api + s"/platform/config/rules/${ruleIds}.json"
    val json = HttpUtils.getText(url).getText
    parseToRules(json)
  }

  def list(business: String, profileId: String): Iterable[Rule] = {
    val url = Ems.api + s"/platform/config/rules/${business}/${profileId}.json"
    val json = HttpUtils.getText(url).getText
    parseToRules(json)
  }

  private def parseToRules(json: String): Iterable[Rule] = {
    val rules = JsonParser.parseArray(json)
    rules.map { r =>
      val rm = r.asInstanceOf[JsonObject]
      val id = rm.getLong("id")
      val name = rm.getString("name")
      val title = rm.getString("title")
      val params = rm.getObject("params").values
      Rule(id, name, title, params)
    }
  }
}
