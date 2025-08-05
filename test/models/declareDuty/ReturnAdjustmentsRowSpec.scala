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

package models.declareDuty

import base.SpecBase
import models.returns.ReturnAdjustmentsRow
import play.api.libs.json.{JsSuccess, Json}

class ReturnAdjustmentsRowSpec extends SpecBase {

  "ReturnAdjustmentsRow" - {
    "must serialize to JSON correctly" in {
      val adjustmentRow = ReturnAdjustmentsRow(
        adjustmentTypeKey = "underdeclaration",
        returnPeriodAffected = Some(periodKey),
        taxType = "311",
        litresOfPureAlcohol = BigDecimal(50),
        dutyRate = Some(BigDecimal(2.5)),
        dutyValue = BigDecimal(125)
      )

      val expectedJson = Json.obj(
        "adjustmentTypeKey"    -> "underdeclaration",
        "returnPeriodAffected" -> periodKey,
        "taxType"              -> "311",
        "litresOfPureAlcohol"  -> 50,
        "dutyRate"             -> 2.5,
        "dutyValue"            -> 125
      )

      Json.toJson(adjustmentRow) mustEqual expectedJson
    }

    "must deserialize from JSON correctly" in {
      val json = Json.obj(
        "adjustmentTypeKey"    -> "underdeclaration",
        "returnPeriodAffected" -> periodKey,
        "taxType"              -> "311",
        "litresOfPureAlcohol"  -> 50,
        "dutyRate"             -> 2.5,
        "dutyValue"            -> 125
      )

      json.validate[ReturnAdjustmentsRow] mustEqual JsSuccess(
        ReturnAdjustmentsRow(
          adjustmentTypeKey = "underdeclaration",
          returnPeriodAffected = Some(periodKey),
          taxType = "311",
          litresOfPureAlcohol = BigDecimal(50),
          dutyRate = Some(BigDecimal(2.5)),
          dutyValue = BigDecimal(125)
        )
      )
    }

    "must sort adjustments rows correctly by adjustmentTypeKey and then by taxType" in {
      val row1 = ReturnAdjustmentsRow(
        "underdeclaration",
        Some(periodKeyJan),
        "311",
        BigDecimal(50),
        Some(BigDecimal(2.5)),
        BigDecimal(125)
      )
      val row2 = ReturnAdjustmentsRow(
        "overdeclaration",
        Some(periodKeyFeb),
        "312",
        BigDecimal(30),
        Some(BigDecimal(3.0)),
        BigDecimal(90)
      )
      val row3 = ReturnAdjustmentsRow("spoilt", None, "313", BigDecimal(20), None, BigDecimal(80))

      val rows       = Seq(row1, row2, row3)
      val sortedRows = rows.sorted

      sortedRows mustEqual Seq(row1, row2, row3)
    }
  }
}
