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

package org.beangle.ems.app.blob

import java.io.FileInputStream
import java.time.ZoneId

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class RemoteRepositoryTest extends AnyFunSpec with Matchers {
  describe("RemoteRepository") {
    val serviceReady = false
    if (serviceReady) {
      val repo = new RemoteRepository("http://127.0.0.1:8081/blob", "/platform", "platform-adminapp", "platform-adminapp")
      it("upload") {
        val meta = repo.upload("/a/b", new FileInputStream("/home/chaostone/a.txt"), "a.txt", "me 段体华")
        println(meta.updatedAt.atZone(ZoneId.systemDefault()))
      }
      it("download") {
        println(repo.url("/java_help.txt"))
      }
      it("delete") {
        println(repo.remove("/a/b/9dd663285d545199ca79f87fad55f489217d9fd2.txt"))
      }
    }
  }
}
