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
import models.adjustment.AdjustmentType.RepackagedDraughtProducts
import play.api.Application
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Value
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, Key, SummaryListRow}
import viewmodels.govuk.all.SummaryListViewModel

class repackagedTaxTypeSummaryListHelperSpec extends SpecBase {
  "repackagedTaxTypeSummaryListHelper must return all rows" in new SetUp {
    repackagedTaxTypeSummaryListHelper.repackagedTaxTypeSummaryList(
      adjustmentEntry.rateBand.get,
      adjustmentEntry.repackagedRateBand.get
    ) mustBe SummaryListViewModel(rows = Seq(row1, row2, row3))
  }

  class SetUp {
    val adjustmentEntry: AdjustmentEntry = AdjustmentEntry(
      adjustmentType = Some(RepackagedDraughtProducts),
      rateBand = fullRepackageAdjustmentEntry.rateBand,
      repackagedRateBand = fullRepackageAdjustmentEntry.repackagedRateBand
    )

    val application: Application    = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)

    val row1: SummaryListRow = SummaryListRow(
      key = Key(Text("Row1Key")),
      value = Value(Text("Row1Value")),
      actions = Some(Actions("", Seq(ActionItem("foo.bar"))))
    )
    val row2: SummaryListRow = SummaryListRow(key = Key(Text("Row2Key")), value = Value(Text("Row2Value")))
    val row3: SummaryListRow = SummaryListRow(key = Key(Text("Row3Key")), value = Value(Text("Row3Value")))

    val mockDraughtTaxTypeCodeSummary: DraughtTaxTypeCodeSummary               = mock[DraughtTaxTypeCodeSummary]
    val mockNonDraughtTaxTypeCodeSummary: NonDraughtTaxTypeCodeSummary         = mock[NonDraughtTaxTypeCodeSummary]
    val mockNonDraughtTaxTypeCodeDescSummary: NonDraughtTaxTypeCodeDescSummary = mock[NonDraughtTaxTypeCodeDescSummary]

    when(mockDraughtTaxTypeCodeSummary.row(adjustmentEntry.rateBand.get)).thenReturn(row1)
    when(mockNonDraughtTaxTypeCodeSummary.row(adjustmentEntry.repackagedRateBand.get)).thenReturn(row2)
    when(mockNonDraughtTaxTypeCodeDescSummary.row(adjustmentEntry.repackagedRateBand.get)).thenReturn(row3)

    val repackagedTaxTypeSummaryListHelper = new RepackagedTaxTypeSummaryListHelper(
      mockDraughtTaxTypeCodeSummary,
      mockNonDraughtTaxTypeCodeSummary,
      mockNonDraughtTaxTypeCodeDescSummary
    )
  }
}
