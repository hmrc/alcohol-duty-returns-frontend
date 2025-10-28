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

class AdjustmentRepackagedTaxTypeSummarySpec extends SpecBase {
  "AdjustmentRepackagedTaxTypeSummary" - {
    "must return a row if the period and adjustment type can be fetched" in new SetUp(true) {
      val row = adjustmentRepackagedTaxTypeSummary.row(adjustmentEntry)

      val expectedAction = ActionItem(
        content = Text("Change"),
        href = controllers.adjustment.routes.AdjustmentRepackagedTaxTypeController.onPageLoad(CheckMode).url,
        visuallyHiddenText = Some("new tax type")
      )

      row.get.key.content.asHtml.toString   mustBe "New tax type"
      row.get.value.content.asHtml.toString mustBe "Non-draught beer between 1% and 3% ABV (tax type code 123)"
      row.get.actions.get.items.head        mustBe expectedAction
    }

    "must return no row if no repackaged rate band can be fetched" in new SetUp(false) {
      adjustmentRepackagedTaxTypeSummary.row(adjustmentEntry) mustBe None
    }
  }

  class SetUp(hasRepackagedRateBand: Boolean) {
    implicit val messages: Messages = getMessages(app)

    val maybeRepackagedRateBand = if (hasRepackagedRateBand) { Some(coreRateBand) }
    else { None }
    val adjustmentEntry         = AdjustmentEntry(repackagedRateBand = maybeRepackagedRateBand)

    val adjustmentRepackagedTaxTypeSummary = new AdjustmentRepackagedTaxTypeSummary()
  }
}
