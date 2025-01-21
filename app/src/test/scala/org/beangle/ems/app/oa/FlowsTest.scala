package org.beangle.ems.app.oa

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class FlowsTest extends AnyFunSpec with Matchers {
  describe("Flows") {
    it("convert") {
      val rs = Flows.convert(
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
          |                "name": "学生"
          |            },
          |            "id": "2",
          |            "type": "groups"
          |        },
          |        {
          |            "attributes": {
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
