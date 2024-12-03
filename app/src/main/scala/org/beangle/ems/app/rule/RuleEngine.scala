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
import org.beangle.commons.net.http.HttpUtils
import org.beangle.ems.app.Ems
import org.beangle.ems.app.util.JSON

import java.util as ju

class RuleEngine(val rules: Iterable[Rule], val stopWhenFail: Boolean = false) {

  var builder: RuleExecutorBuilder = _

  def execute(context: Any): List[(Rule, Boolean, String)] = {
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
  def of(ruleIds: String, stopWhenFail: Boolean = false): RuleEngine = {
    val url = Ems.api + s"/platform/config/rules/${ruleIds}.json"
    val json = HttpUtils.getText(url).getText
    val rules = parseToRules(json)
    new RuleEngine(rules, stopWhenFail)
  }

  def list(business: String, profileId: String): Iterable[Rule] = {
    val url = Ems.api + s"/platform/config/rules/${business}/${profileId}.json"
    val json = HttpUtils.getText(url).getText
    parseToRules(json)
  }

  private def parseToRules(json: String): Iterable[Rule] = {
    val rules = JSON.parseSeq(json)
    rules.map { r =>
      val rm = r.asInstanceOf[ju.Map[String, Any]]
      val id = rm.get("id").toString.toLong
      val name = rm.get("name").toString
      val title = rm.get("title").toString
      val params = rm.get("params").asInstanceOf[ju.Map[String, Any]]
      import scala.jdk.javaapi.CollectionConverters.asScala
      Rule(id, name, title, asScala(params).toMap)
    }
  }
}
