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

package org.beangle.ems.app.oa

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class FlowsTest extends AnyFunSpec with Matchers {
  describe("Flows") {
    it("convert") {
      val rs = Flows.convertFlow(
        """
          |{
          |    "data": [
          |        {
          |            "attributes": {
          |                "code": "alter_apply_1",
          |                "name": "22"
          |            },
          |            "id": "2025012018162405962",
          |            "type": "flows",
          |            "relationships": {
          |                "tasks": {
          |                    "data": [
          |                        {
          |                            "id": "2025012018162405963",
          |                            "type": "flow-tasks"
          |                        },
          |                        {
          |                            "id": "2025012018172905964",
          |                            "type": "flow-tasks"
          |                        },
          |                        {
          |                            "id": "2025012018182205965",
          |                            "type": "flow-tasks"
          |                        }
          |                    ]
          |                }
          |            }
          |        }
          |    ],
          |    "included": [
          |        {
          |            "attributes": {
          |                "code":"std",
          |                "name": "学生"
          |            },
          |            "id": "2",
          |            "type": "groups"
          |        },
          |        {
          |            "attributes": {
          |                "code":"staff",
          |                "name": "教职工"
          |            },
          |            "id": "3",
          |            "type": "groups"
          |        },
          |        {
          |            "attributes": {
          |                "name": "导师审批",
          |                "idx": 1
          |            },
          |            "id": "2025012018162405963",
          |            "type": "flow-tasks",
          |            "relationships": {
          |                "group": {
          |                    "data": {
          |                        "id": "3",
          |                        "type": "groups"
          |                    }
          |                }
          |            }
          |        },
          |        {
          |            "attributes": {
          |                "name": "提交申请",
          |                "idx": 0
          |            },
          |            "id": "2025012018172905964",
          |            "type": "flow-tasks",
          |            "relationships": {
          |                "group": {
          |                    "data": {
          |                        "id": "2",
          |                        "type": "groups"
          |                    }
          |                }
          |            }
          |        },
          |        {
          |            "attributes": {
          |                "name": "学院审批",
          |                "idx": 2
          |            },
          |            "id": "2025012018182205965",
          |            "type": "flow-tasks",
          |            "relationships": {
          |                "group": {
          |                    "data": {
          |                        "id": "2",
          |                        "type": "groups"
          |                    }
          |                }
          |            }
          |        }
          |    ]
          |}
          |""".stripMargin
      )
      println(rs)
    }
  }
}
