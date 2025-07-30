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
import models.adjustment.AdjustmentEntry
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Key, SummaryListRow, Value}

class AdjustmentSmallProducerReliefDutyRateSummarySpec extends SpecBase {
  "AdjustmentSmallProducerReliefDutyRateSummary" - {
    "must return a the repackaged SPR duty rate row if the SPR duty rate and repackaged SPR duty rate can be fetched" in new SetUp(
      true,
      true
    ) {
      adjustmentSmallProducerReliefDutyRateSummary.row(adjustmentEntry) mustBe Some(
        SummaryListRow(
          Key(Text("Duty rate")),
          Value(Text("£3.45")),
          "",
          Some(
            Actions(items =
              List(
                ActionItem(
                  "/manage-alcohol-duty/complete-return/adjustments/adjustment/change/repackaged/new-spr-duty-rate",
                  Text("Change"),
                  Some("duty rate")
                )
              )
            )
          )
        )
      )
    }

    "must return a row if only the SPR duty rate can be fetched" in new SetUp(true, false) {
      adjustmentSmallProducerReliefDutyRateSummary.row(adjustmentEntry) mustBe Some(
        SummaryListRow(
          Key(Text("Duty rate")),
          Value(Text("£1.23")),
          "",
          Some(
            Actions(items =
              List(
                ActionItem(
                  "/manage-alcohol-duty/complete-return/adjustments/adjustment/change/spr/eligible-volume",
                  Text("Change"),
                  Some("duty rate")
                )
              )
            )
          )
        )
      )
    }

    "must return a row if only the repackaged SPR duty rate can be fetched" in new SetUp(false, true) {
      adjustmentSmallProducerReliefDutyRateSummary.row(adjustmentEntry) mustBe Some(
        SummaryListRow(
          Key(Text("Duty rate")),
          Value(Text("£3.45")),
          "",
          Some(
            Actions(items =
              List(
                ActionItem(
                  "/manage-alcohol-duty/complete-return/adjustments/adjustment/change/repackaged/new-spr-duty-rate",
                  Text("Change"),
                  Some("duty rate")
                )
              )
            )
          )
        )
      )
    }

    "must return no row if neither the repackaged SPR duty rate can be fetched" in new SetUp(false, false) {
      adjustmentSmallProducerReliefDutyRateSummary.row(adjustmentEntry) mustBe None
    }
  }

  class SetUp(hasSPRDutyRate: Boolean, hasRepackagedSPRDutyRate: Boolean) {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)

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
