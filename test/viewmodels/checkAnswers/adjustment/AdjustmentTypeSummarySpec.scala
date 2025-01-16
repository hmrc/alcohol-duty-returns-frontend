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
import models.adjustment.AdjustmentType.Underdeclaration
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Key, SummaryListRow, Value}

class AdjustmentTypeSummarySpec extends SpecBase {
  "AdjustmentTypeSummary" - {
    "must return a row if the adjustment type can be fetched" in new SetUp(true) {
      adjustmentTypeSummary.row(adjustmentEntry) mustBe Some(
        SummaryListRow(
          Key(Text("Adjustment")),
          Value(HtmlContent("Under-declared")),
          "",
          Some(
            Actions(items =
              List(
                ActionItem(
                  "/manage-alcohol-duty/complete-return/adjustments/adjustment/change/type",
                  Text("Change"),
                  Some("adjustment")
                )
              )
            )
          )
        )
      )
    }

    "must return no row if no adjustment type can be fetched" in new SetUp(false) {
      adjustmentTypeSummary.row(adjustmentEntry) mustBe None
    }
  }

  class SetUp(hasAdjustmentType: Boolean) {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)

    val maybeAdjustmentType = if (hasAdjustmentType) {
      Some(Underdeclaration)
    } else {
      None
    }

    val adjustmentEntry = AdjustmentEntry(adjustmentType = maybeAdjustmentType)

    val adjustmentTypeSummary = new AdjustmentTypeSummary
  }
}
