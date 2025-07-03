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
import uk.gov.hmrc.govukfrontend.views.Aliases.Value
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import viewmodels.govuk.all.SummaryListViewModel

class CheckYourAnswersSummaryListHelperSpec extends SpecBase {
  "CheckYourAnswersSummaryListHelper must return all rows when adjustment type summary, volume summary, and duty due summary can be fetched" in new SetUp(
    true,
    true,
    true
  ) {
    checkYourAnswersSummaryListHelper.currentAdjustmentEntrySummaryList(adjustmentEntry) mustBe Some(
      SummaryListViewModel(rows = Seq(row6, row4, row3, row5, row1, row2, row7, row8))
    )
  }

  "CheckYourAnswersSummaryListHelper must return no rows when adjustment type summary can't be fetched" in new SetUp(
    false,
    true,
    true
  ) {
    checkYourAnswersSummaryListHelper.currentAdjustmentEntrySummaryList(adjustmentEntry) mustBe None
  }

  "CheckYourAnswersSummaryListHelper must return all rows when volume summary can't be fetched" in new SetUp(
    true,
    false,
    true
  ) {
    checkYourAnswersSummaryListHelper.currentAdjustmentEntrySummaryList(adjustmentEntry) mustBe None
  }

  "CheckYourAnswersSummaryListHelper must return all rows when duty due summary can't be fetched" in new SetUp(
    true,
    true,
    false
  ) {
    checkYourAnswersSummaryListHelper.currentAdjustmentEntrySummaryList(adjustmentEntry) mustBe None
  }

  class SetUp(
    canFetchAdjustmentTypeSummary: Boolean,
    canFetchAdjustmentVolumeSummary: Boolean,
    canFetchDutyDueSummary: Boolean
  ) {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)

    val row1 = SummaryListRow(key = Key(Text("Row1Key")), value = Value(Text("Row1Value")))
    val row2 = SummaryListRow(key = Key(Text("Row2Key")), value = Value(Text("Row2Value")))
    val row3 = SummaryListRow(key = Key(Text("Row3Key")), value = Value(Text("Row3Value")))
    val row4 = SummaryListRow(key = Key(Text("Row4Key")), value = Value(Text("Row4Value")))
    val row5 = SummaryListRow(key = Key(Text("Row5Key")), value = Value(Text("Row5Value")))
    val row6 = SummaryListRow(key = Key(Text("Row6Key")), value = Value(Text("Row6Value")))
    val row7 = SummaryListRow(key = Key(Text("Row7Key")), value = Value(Text("Row7Value")))
    val row8 = SummaryListRow(key = Key(Text("Row8Key")), value = Value(Text("Row8Value")))

    val mockAdjustmentRepackagedTaxTypeSummary           = mock[AdjustmentRepackagedTaxTypeSummary]
    val mockAdjustmentSmallProducerReliefDutyRateSummary = mock[AdjustmentSmallProducerReliefDutyRateSummary]
    val mockAdjustmentReturnPeriodSummary                = mock[AdjustmentReturnPeriodSummary]
    val mockSpoiltAlcoholicProductTypeSummary            = mock[SpoiltAlcoholicProductTypeSummary]
    val mockAdjustmentTaxTypeSummary                     = mock[AdjustmentTaxTypeSummary]
    val mockAdjustmentTypeSummary                        = mock[AdjustmentTypeSummary]
    val mockAdjustmentVolumeSummary                      = mock[AdjustmentVolumeSummary]
    val mockAdjustmentDutyDueSummary                     = mock[AdjustmentDutyDueSummary]

    val adjustmentEntry = AdjustmentEntry()

    when(mockAdjustmentRepackagedTaxTypeSummary.row(adjustmentEntry)).thenReturn(Some(row1))
    when(mockAdjustmentSmallProducerReliefDutyRateSummary.row(adjustmentEntry)).thenReturn(Some(row2))
    when(mockAdjustmentReturnPeriodSummary.row(adjustmentEntry)).thenReturn(Some(row3))
    when(mockSpoiltAlcoholicProductTypeSummary.row(adjustmentEntry)).thenReturn(Some(row4))
    when(mockAdjustmentTaxTypeSummary.row(adjustmentEntry)).thenReturn(Some(row5))

    if (canFetchAdjustmentTypeSummary) {
      when(mockAdjustmentTypeSummary.row(adjustmentEntry)).thenReturn(Some(row6))
    } else {
      when(mockAdjustmentTypeSummary.row(adjustmentEntry)).thenReturn(None)
    }

    if (canFetchAdjustmentVolumeSummary) {
      when(mockAdjustmentVolumeSummary.row(adjustmentEntry)).thenReturn(Some(row7))
    } else {
      when(mockAdjustmentVolumeSummary.row(adjustmentEntry)).thenReturn(None)
    }

    if (canFetchDutyDueSummary) {
      when(mockAdjustmentDutyDueSummary.row(adjustmentEntry)).thenReturn(Some(row8))
    } else {
      when(mockAdjustmentDutyDueSummary.row(adjustmentEntry)).thenReturn(None)
    }

    val checkYourAnswersSummaryListHelper = new CheckYourAnswersSummaryListHelper(
      mockAdjustmentRepackagedTaxTypeSummary,
      mockAdjustmentSmallProducerReliefDutyRateSummary,
      mockAdjustmentReturnPeriodSummary,
      mockSpoiltAlcoholicProductTypeSummary,
      mockAdjustmentTaxTypeSummary,
      mockAdjustmentTypeSummary,
      mockAdjustmentVolumeSummary,
      mockAdjustmentDutyDueSummary
    )
  }
}
