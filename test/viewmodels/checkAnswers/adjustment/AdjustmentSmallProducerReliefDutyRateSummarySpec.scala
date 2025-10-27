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
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}

class AdjustmentSmallProducerReliefDutyRateSummarySpec extends SpecBase {
  "AdjustmentSmallProducerReliefDutyRateSummary" - {
    "must return a row with the repackaged SPR duty rate if both SPR duty rate and repackaged SPR duty rate can be fetched" in new SetUp(
      true,
      true
    ) {
      val row = adjustmentSmallProducerReliefDutyRateSummary.row(adjustmentEntry)

      val expectedAction = ActionItem(
        content = Text("Change"),
        href = controllers.adjustment.routes.AdjustmentSmallProducerReliefDutyRateController.onPageLoad(CheckMode).url,
        visuallyHiddenText = Some("duty rate")
      )

      row.get.key.content.asHtml.toString   mustBe "Duty rate"
      row.get.value.content.asHtml.toString mustBe "£3.45"
      row.get.actions.get.items.head        mustBe expectedAction
    }

    "must return a row if only the SPR duty rate can be fetched" in new SetUp(true, false) {
      val row = adjustmentSmallProducerReliefDutyRateSummary.row(adjustmentEntry)

      val expectedAction = ActionItem(
        content = Text("Change"),
        href = controllers.adjustment.routes.AdjustmentVolumeWithSPRController.onPageLoad(CheckMode).url,
        visuallyHiddenText = Some("duty rate")
      )

      row.get.key.content.asHtml.toString   mustBe "Duty rate"
      row.get.value.content.asHtml.toString mustBe "£1.23"
      row.get.actions.get.items.head        mustBe expectedAction
    }

    "must return a row if only the repackaged SPR duty rate can be fetched" in new SetUp(false, true) {
      val row = adjustmentSmallProducerReliefDutyRateSummary.row(adjustmentEntry)

      val expectedAction = ActionItem(
        content = Text("Change"),
        href = controllers.adjustment.routes.AdjustmentSmallProducerReliefDutyRateController.onPageLoad(CheckMode).url,
        visuallyHiddenText = Some("duty rate")
      )

      row.get.key.content.asHtml.toString   mustBe "Duty rate"
      row.get.value.content.asHtml.toString mustBe "£3.45"
      row.get.actions.get.items.head        mustBe expectedAction
    }

    "must return no row if neither SPR duty rate nor repackaged SPR duty rate can be fetched" in new SetUp(
      false,
      false
    ) {
      adjustmentSmallProducerReliefDutyRateSummary.row(adjustmentEntry) mustBe None
    }
  }

  class SetUp(hasSPRDutyRate: Boolean, hasRepackagedSPRDutyRate: Boolean) {
    implicit val messages: Messages = getMessages(app)

    val maybeSPRDutyRate           = if (hasSPRDutyRate) {
      Some(BigDecimal("1.23"))
    } else {
      None
    }
    val maybeRepackagedSPRDutyRate = if (hasRepackagedSPRDutyRate) {
      Some(BigDecimal("3.45"))
    } else {
      None
    }
    val adjustmentEntry            =
      AdjustmentEntry(sprDutyRate = maybeSPRDutyRate, repackagedSprDutyRate = maybeRepackagedSPRDutyRate)

    val adjustmentSmallProducerReliefDutyRateSummary = new AdjustmentSmallProducerReliefDutyRateSummary()
  }
}
