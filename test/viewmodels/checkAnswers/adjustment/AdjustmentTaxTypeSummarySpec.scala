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

package viewmodels.checkAnswers.adjustment

import base.SpecBase
import models.CheckMode
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.{Drawback, RepackagedDraughtProducts, Spoilt}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.ActionItem

class AdjustmentTaxTypeSummarySpec extends SpecBase {
  "AdjustmentTaxTypeSummary" - {
    "must create the summary list row view model from the rateBands" in new SetUp {
      val adjustmentEntry = AdjustmentEntry(adjustmentType = Some(Drawback), rateBand = Some(coreRateBand))
      val result          = adjustmentTaxTypeSummary.row(adjustmentEntry).get

      val expectedAction = ActionItem(
        content = Text("Change"),
        href = controllers.adjustment.routes.AdjustmentTaxTypeController.onPageLoad(CheckMode).url,
        visuallyHiddenText = Some("tax type")
      )

      result.key.content            mustBe Text("Tax type")
      result.value.content          mustBe Text("Non-draught beer between 1% and 3% ABV (tax type code 123)")
      result.actions.get.items.head mustBe expectedAction
    }

    "must create the summary list row view model from the rateBands where adjustmentType is RepackagedDraughtProducts" in new SetUp {
      val adjustmentEntry =
        AdjustmentEntry(adjustmentType = Some(RepackagedDraughtProducts), rateBand = Some(coreRateBand))
      val result          = adjustmentTaxTypeSummary.row(adjustmentEntry).get

      val expectedAction = ActionItem(
        content = Text("Change"),
        href = controllers.adjustment.routes.AdjustmentTaxTypeController.onPageLoad(CheckMode).url,
        visuallyHiddenText = Some("original tax type")
      )

      result.key.content            mustBe Text("Original tax type")
      result.value.content          mustBe Text("Non-draught beer between 1% and 3% ABV (tax type code 123)")
      result.actions.get.items.head mustBe expectedAction
    }

    "must not create the summary list row view model from the rateBands for Spoilt adjustment" in new SetUp {
      val adjustmentEntry = AdjustmentEntry(adjustmentType = Some(Spoilt), rateBand = Some(coreRateBand))
      val result          = adjustmentTaxTypeSummary.row(adjustmentEntry)

      result mustBe None
    }

    "must throw an exception if unable to get the adjustment type" in new SetUp {
      val adjustmentEntry = AdjustmentEntry(adjustmentType = None, rateBand = Some(coreRateBand))
      a[RuntimeException] mustBe thrownBy(adjustmentTaxTypeSummary.row(adjustmentEntry).get)
    }
  }

  class SetUp {
    implicit val messages: Messages = getMessages(app)

    val adjustmentTaxTypeSummary = new AdjustmentTaxTypeSummary
  }
}
