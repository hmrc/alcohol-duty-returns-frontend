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

import java.time.YearMonth

class AdjustmentReturnPeriodSummarySpec extends SpecBase {
  "AdjustmentReturnPeriodSummary" - {
    "must return a row if the period and adjustment type can be fetched" in new SetUp(true, true) {
      adjustmentReturnPeriodSummary.row(adjustmentEntry) mustBe Some(
        SummaryListRow(
          Key(Text("Original return period")),
          Value(HtmlContent("June 2024")),
          "",
          Some(
            Actions(items =
              List(
                ActionItem(
                  "/manage-alcohol-duty/complete-return/adjustments/adjustment/change/return-period",
                  Text("Change"),
                  Some("original return period")
                )
              )
            )
          )
        )
      )
    }

    "must return no row if no period type can be fetched" in new SetUp(false, true) {
      adjustmentReturnPeriodSummary.row(adjustmentEntry) mustBe None
    }

    "must return no row if no adjustment type can be fetched" in new SetUp(true, false) {
      adjustmentReturnPeriodSummary.row(adjustmentEntry) mustBe None
    }
  }

  class SetUp(hasPeriod: Boolean, hasAdjustmentType: Boolean) {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)

    val maybePeriod         = if (hasPeriod) { Some(YearMonth.now(clock)) }
    else { None }
    val maybeAdjustmentType = if (hasAdjustmentType) { Some(Underdeclaration) }
    else { None }
    val adjustmentEntry     = AdjustmentEntry(adjustmentType = maybeAdjustmentType, period = maybePeriod)

    val adjustmentReturnPeriodSummary = new AdjustmentReturnPeriodSummary(createDateTimeHelper())
  }
}
