/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.returns

import base.SpecBase
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.libs.json.{JsSuccess, Json}

class ReturnAdjustmentsRowSpec extends SpecBase {

  "ReturnAdjustmentsRow" - {
    "should serialize to JSON correctly" in {
      val adjustmentRow = ReturnAdjustmentsRow(
        adjustmentTypeKey = "underdeclaration",
        taxType = "beer",
        litresOfPureAlcohol = BigDecimal(50),
        dutyRate = BigDecimal(2.5),
        dutyValue = BigDecimal(125)
      )

      val expectedJson = Json.obj(
        "adjustmentTypeKey"   -> "underdeclaration",
        "taxType"             -> "beer",
        "litresOfPureAlcohol" -> 50,
        "dutyRate"            -> 2.5,
        "dutyValue"           -> 125
      )

      Json.toJson(adjustmentRow) shouldEqual expectedJson
    }

    "should deserialize from JSON correctly" in {
      val json = Json.obj(
        "adjustmentTypeKey"   -> "underdeclaration",
        "taxType"             -> "beer",
        "litresOfPureAlcohol" -> 50,
        "dutyRate"            -> 2.5,
        "dutyValue"           -> 125
      )

      json.validate[ReturnAdjustmentsRow] shouldEqual JsSuccess(
        ReturnAdjustmentsRow(
          adjustmentTypeKey = "underdeclaration",
          taxType = "beer",
          litresOfPureAlcohol = BigDecimal(50),
          dutyRate = BigDecimal(2.5),
          dutyValue = BigDecimal(125)
        )
      )
    }

    "should sort adjustments rows correctly by adjustmentTypeKey and then by taxType" in {
      val row1 = ReturnAdjustmentsRow("underdeclaration", "beer", BigDecimal(50), BigDecimal(2.5), BigDecimal(125))
      val row2 = ReturnAdjustmentsRow("overdeclaration", "wine", BigDecimal(30), BigDecimal(3.0), BigDecimal(90))
      val row3 = ReturnAdjustmentsRow("spoilt", "spirits", BigDecimal(20), BigDecimal(4.0), BigDecimal(80))

      val rows       = Seq(row1, row2, row3)
      val sortedRows = rows.sorted

      sortedRows shouldEqual Seq(row1, row2, row3)
    }
  }
}
