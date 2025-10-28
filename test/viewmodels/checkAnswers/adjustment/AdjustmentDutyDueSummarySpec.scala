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
import models.CheckMode
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.{RepackagedDraughtProducts, Spoilt}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}

class AdjustmentDutyDueSummarySpec extends SpecBase {
  "AdjustmentDutyDueSummary" - {
    "must return a row without actions and prioritise new duty over duty if not Spoilt" in new SetUp(
      false,
      true,
      false
    ) {
      val row = adjustmentDutyDueSummary.row(adjustmentEntry)

      row.get.key.content.asHtml.toString   mustBe "Duty value"
      row.get.value.content.asHtml.toString mustBe "£1.20"
      row.get.actions.get.items             mustBe Seq.empty
    }

    "must return a row without actions, and use duty if no new duty, if not Spoilt" in new SetUp(false, false, true) {
      val row = adjustmentDutyDueSummary.row(adjustmentEntry)

      row.get.key.content.asHtml.toString   mustBe "Duty value"
      row.get.value.content.asHtml.toString mustBe "£2.30"
      row.get.actions.get.items             mustBe Seq.empty
    }

    "must return a row with an action (and use duty) if Spoilt" in new SetUp(true, false, true) {
      val row = adjustmentDutyDueSummary.row(adjustmentEntry)

      val expectedAction = ActionItem(
        content = Text("Change"),
        href = controllers.adjustment.routes.SpoiltVolumeWithDutyController.onPageLoad(CheckMode).url,
        visuallyHiddenText = Some("duty value")
      )

      row.get.key.content.asHtml.toString   mustBe "Duty value"
      row.get.value.content.asHtml.toString mustBe "£2.30"
      row.get.actions.get.items.head        mustBe expectedAction
    }

    "must return no row if neither duty can be fetched" in new SetUp(true, false, false) {
      adjustmentDutyDueSummary.row(adjustmentEntry) mustBe None
    }
  }

  class SetUp(isSpoilt: Boolean, hasNewDuty: Boolean, hasDuty: Boolean) {
    implicit val messages: Messages = getMessages(app)

    val maybeAdjustmentType = if (isSpoilt) {
      Some(Spoilt)
    } else {
      Some(RepackagedDraughtProducts)
    }

    val maybeNewDuty = if (hasNewDuty) {
      Some(BigDecimal("1.20"))
    } else {
      None
    }

    val maybeDuty = if (hasDuty || hasNewDuty) {
      Some(BigDecimal("2.30"))
    } else {
      None
    }

    val adjustmentEntry =
      AdjustmentEntry(adjustmentType = maybeAdjustmentType, duty = maybeDuty, newDuty = maybeNewDuty)

    val adjustmentDutyDueSummary = new AdjustmentDutyDueSummary
  }
}
