/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels.checkAnswers.adjustment

import base.SpecBase
import models.adjustment.AdjustmentType._
import models.adjustment.{AdjustmentEntry, AdjustmentType}
import play.api.i18n.Messages

class AdjustmentDutyRateSummarySpec extends SpecBase {
  "AdjustmentDutyRateSummary" - {
    "must return a row if adjustment type is not Spoilt or Repackaged and sprDutyRate is not defined" in new SetUp(
      Underdeclaration,
      false
    ) {
      val row = adjustmentDutyRateSummary.row(adjustmentEntry)

      row.get.key.content.asHtml.toString   mustBe "Duty rate"
      row.get.value.content.asHtml.toString mustBe "£1.10"
      row.get.actions                       mustBe None
    }

    "must return no row if adjustment type is Spoilt" in new SetUp(Spoilt, false) {
      adjustmentDutyRateSummary.row(adjustmentEntry) mustBe None
    }

    "must return no row if adjustment type is Repackaged" in new SetUp(RepackagedDraughtProducts, false) {
      adjustmentDutyRateSummary.row(adjustmentEntry) mustBe None
    }

    "must return no row if sprDutyRate is defined" in new SetUp(Overdeclaration, true) {
      adjustmentDutyRateSummary.row(adjustmentEntry) mustBe None
    }

    "must throw an exception if unable to get the adjustment type" in new SetUp(Overdeclaration, false) {
      val exception = intercept[IllegalStateException] {
        adjustmentDutyRateSummary.row(adjustmentEntry.copy(rateBand = None))
      }

      exception.getMessage mustBe "Adjustment duty rate is required but not found"
    }
  }

  class SetUp(adjustmentType: AdjustmentType, hasSprDutyRate: Boolean) {
    implicit val messages: Messages = getMessages(app)

    val maybeSPRDutyRate = if (hasSprDutyRate) Some(BigDecimal("1.23")) else None

    val adjustmentEntry =
      AdjustmentEntry(
        adjustmentType = Some(adjustmentType),
        rateBand = Some(coreRateBand),
        sprDutyRate = maybeSPRDutyRate
      )

    val adjustmentDutyRateSummary = new AdjustmentDutyRateSummary
  }
}
