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
package org.beangle.ems.app.security

import org.junit.runner.RunWith
import org.scalatest.matchers.should.Matchers
import org.scalatest.funspec.AnyFunSpec
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RemoteServiceTest extends AnyFunSpec with Matchers {
  describe("RemoteService") {
    it("toAuthorities") {
      val content = """[{"roles":[],"scope":"Public","name":"/index","id":363,"title":"首页"}]"""
      val authorities = RemoteService.toAuthorities(content)
      authorities.size should be(1)
      authorities.head.roles.size should be(0)

      val content2 = """[{"roles":[3],"scope":"Public","name":"/index","id":363,"title":"首页"}]"""
      val authorities2 = RemoteService.toAuthorities(content2)
      authorities2.size should be(1)
      authorities2.head.roles.size should be(1)
    }

    it("parse empty") {
      val authorities3 = RemoteService.toAuthorities(null)
      val authorities4 = RemoteService.toAuthorities("")

      authorities3.size should be(0)
      authorities4.size should be(0)
    }

    it("parse missing"){
      val content = """[{"scope":"Public","name":"/index","id":363,"title":"首页"}]"""
      val authorities = RemoteService.toAuthorities(content)
      authorities.size should be(1)
      authorities.head.roles.size should be(0)
    }
  }

}
