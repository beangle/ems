/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2020, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.ems.app.util

import org.junit.runner.RunWith
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

/**
 * @author chaostone
 */
@RunWith(classOf[JUnitRunner])
class JSONTest extends AnyFunSpec with Matchers {
  describe("JSON") {
    it("parse") {
      val result = JSON.parseObj(
        """
            {"accountLocked":false,"details":{"isRoot":false},"authorities":[1,2],"accountExpired":false,"description":"duan","principal":"abc","credentialExpired":false,"disabled":false}
            """)
      assert(result("authorities").isInstanceOf[Iterable[_]])
      val a = """[{"roles":[],"scope":"Protected","name":"/config/home","title":"首页","id":94}]"""
      val data = JSON.parseSeq(a)
      assert(null != data)
      assert(data.head.isInstanceOf[collection.Map[_, _]])
    }
  }
}
