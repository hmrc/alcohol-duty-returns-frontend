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
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.{RepackagedDraughtProducts, Spoilt}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class AdjustmentTaxTypeSummarySpec extends SpecBase {
  "AdjustmentTaxTypeSummary" - {
    "should create the summary list row view model from the rateBands" in new SetUp {
      val adjustmentEntry = AdjustmentEntry(adjustmentType = Some(Spoilt), rateBand = Some(coreRateBand))
      val result          = AdjustmentTaxTypeSummary.row(adjustmentEntry).get

      result.key.content mustBe Text("Tax type")
      result.value.content mustBe Text("Non-draught beer between 1% and 2% ABV (123)")
    }

    "should create the summary list row view model from the rateBands where adjustmentType is RepackagedDraughtProducts" in new SetUp {
      val adjustmentEntry =
        AdjustmentEntry(adjustmentType = Some(RepackagedDraughtProducts), rateBand = Some(coreRateBand))
      val result          = AdjustmentTaxTypeSummary.row(adjustmentEntry).get

      result.key.content mustBe Text("Original tax type")
      result.value.content mustBe Text("Non-draught beer between 1% and 2% ABV (123)")
    }

    "should throw an exception if unable to get the adjustment type" in new SetUp {
      val adjustmentEntry = AdjustmentEntry(adjustmentType = None, rateBand = Some(coreRateBand))
      a[RuntimeException] mustBe thrownBy(AdjustmentTaxTypeSummary.row(adjustmentEntry).get)
    }
  }

  class SetUp {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)
  }
}
