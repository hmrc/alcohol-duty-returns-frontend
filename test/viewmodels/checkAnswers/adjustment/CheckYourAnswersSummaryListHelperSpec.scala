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
import models.adjustment.{AdjustmentEntry, AdjustmentType}
import models.adjustment.AdjustmentType.Underdeclaration
import org.mockito.ArgumentMatchers.any
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Value
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import viewmodels.govuk.all.SummaryListViewModel

class CheckYourAnswersSummaryListHelperSpec extends SpecBase {
  "CheckYourAnswersSummaryListHelper" - {
    "must return rows in the correct order for a spoilt adjustment" in new SetUp(true, true, true) {
      when(mockAdjustmentRepackagedTaxTypeSummary.row(any[AdjustmentEntry])(any())).thenReturn(None)
      when(mockAdjustmentSmallProducerReliefDutyRateSummary.row(any[AdjustmentEntry])(any())).thenReturn(None)
      when(mockAdjustmentReturnPeriodSummary.row(any[AdjustmentEntry])(any())).thenReturn(None)
      when(mockSpoiltAlcoholicProductTypeSummary.row(any[AdjustmentEntry])(any()))
        .thenReturn(Some(spoiltAlcoholTypeRow))
      when(mockAdjustmentTaxTypeSummary.row(any[AdjustmentEntry])(any())).thenReturn(None)
      when(mockAdjustmentTypeSummary.row(any[AdjustmentEntry])(any())).thenReturn(Some(adjustmentTypeRow))
      when(mockAdjustmentVolumeSummary.row(any[AdjustmentEntry])(any())).thenReturn(Some(adjustmentVolumeRow))
      when(mockAdjustmentDutyDueSummary.row(any[AdjustmentEntry])(any())).thenReturn(Some(dutyDueRow))
      when(mockAdjustmentDutyRateSummary.row(any[AdjustmentEntry])(any())).thenReturn(None)

      val result = checkYourAnswersSummaryListHelper.currentAdjustmentEntrySummaryList(
        adjustmentEntry.copy(adjustmentType = Some(AdjustmentType.Spoilt))
      )

      result mustBe Some(
        SummaryListViewModel(rows = Seq(adjustmentTypeRow, spoiltAlcoholTypeRow, adjustmentVolumeRow, dutyDueRow))
      )
    }

    "must return rows in the correct order for a repackaged adjustment (SPR)" in new SetUp(true, true, true) {
      when(mockAdjustmentRepackagedTaxTypeSummary.row(any[AdjustmentEntry])(any()))
        .thenReturn(Some(repackagedTaxTypeRow))
      when(mockAdjustmentSmallProducerReliefDutyRateSummary.row(any[AdjustmentEntry])(any()))
        .thenReturn(Some(sprDutyRateRow))
      when(mockAdjustmentReturnPeriodSummary.row(any[AdjustmentEntry])(any())).thenReturn(Some(returnPeriodRow))
      when(mockSpoiltAlcoholicProductTypeSummary.row(any[AdjustmentEntry])(any())).thenReturn(None)
      when(mockAdjustmentTaxTypeSummary.row(any[AdjustmentEntry])(any())).thenReturn(Some(taxTypeRow))
      when(mockAdjustmentTypeSummary.row(any[AdjustmentEntry])(any())).thenReturn(Some(adjustmentTypeRow))
      when(mockAdjustmentVolumeSummary.row(any[AdjustmentEntry])(any())).thenReturn(Some(adjustmentVolumeRow))
      when(mockAdjustmentDutyDueSummary.row(any[AdjustmentEntry])(any())).thenReturn(Some(dutyDueRow))
      when(mockAdjustmentDutyRateSummary.row(any[AdjustmentEntry])(any())).thenReturn(None)

      val result = checkYourAnswersSummaryListHelper.currentAdjustmentEntrySummaryList(
        adjustmentEntry.copy(adjustmentType = Some(AdjustmentType.RepackagedDraughtProducts))
      )

      result mustBe Some(
        SummaryListViewModel(rows =
          Seq(
            adjustmentTypeRow,
            returnPeriodRow,
            taxTypeRow,
            repackagedTaxTypeRow,
            adjustmentVolumeRow,
            sprDutyRateRow,
            dutyDueRow
          )
        )
      )
    }

    "must return rows in the correct order for other adjustment types (SPR)" in new SetUp(true, true, true) {
      when(mockAdjustmentRepackagedTaxTypeSummary.row(any[AdjustmentEntry])(any())).thenReturn(None)
      when(mockAdjustmentSmallProducerReliefDutyRateSummary.row(any[AdjustmentEntry])(any()))
        .thenReturn(Some(sprDutyRateRow))
      when(mockAdjustmentReturnPeriodSummary.row(any[AdjustmentEntry])(any())).thenReturn(Some(returnPeriodRow))
      when(mockSpoiltAlcoholicProductTypeSummary.row(any[AdjustmentEntry])(any())).thenReturn(None)
      when(mockAdjustmentTaxTypeSummary.row(any[AdjustmentEntry])(any())).thenReturn(Some(taxTypeRow))
      when(mockAdjustmentTypeSummary.row(any[AdjustmentEntry])(any())).thenReturn(Some(adjustmentTypeRow))
      when(mockAdjustmentVolumeSummary.row(any[AdjustmentEntry])(any())).thenReturn(Some(adjustmentVolumeRow))
      when(mockAdjustmentDutyDueSummary.row(any[AdjustmentEntry])(any())).thenReturn(Some(dutyDueRow))
      when(mockAdjustmentDutyRateSummary.row(any[AdjustmentEntry])(any())).thenReturn(None)

      val result = checkYourAnswersSummaryListHelper.currentAdjustmentEntrySummaryList(adjustmentEntry)

      result mustBe Some(
        SummaryListViewModel(rows =
          Seq(adjustmentTypeRow, returnPeriodRow, taxTypeRow, adjustmentVolumeRow, sprDutyRateRow, dutyDueRow)
        )
      )
    }

    "must return rows in the correct order for other adjustment types (non-SPR)" in new SetUp(true, true, true) {
      when(mockAdjustmentRepackagedTaxTypeSummary.row(any[AdjustmentEntry])(any())).thenReturn(None)
      when(mockAdjustmentSmallProducerReliefDutyRateSummary.row(any[AdjustmentEntry])(any())).thenReturn(None)
      when(mockAdjustmentReturnPeriodSummary.row(any[AdjustmentEntry])(any())).thenReturn(Some(returnPeriodRow))
      when(mockSpoiltAlcoholicProductTypeSummary.row(any[AdjustmentEntry])(any())).thenReturn(None)
      when(mockAdjustmentTaxTypeSummary.row(any[AdjustmentEntry])(any())).thenReturn(Some(taxTypeRow))
      when(mockAdjustmentTypeSummary.row(any[AdjustmentEntry])(any())).thenReturn(Some(adjustmentTypeRow))
      when(mockAdjustmentVolumeSummary.row(any[AdjustmentEntry])(any())).thenReturn(Some(adjustmentVolumeRow))
      when(mockAdjustmentDutyDueSummary.row(any[AdjustmentEntry])(any())).thenReturn(Some(dutyDueRow))
      when(mockAdjustmentDutyRateSummary.row(any[AdjustmentEntry])(any())).thenReturn(Some(dutyRateRow))

      val result = checkYourAnswersSummaryListHelper.currentAdjustmentEntrySummaryList(
        adjustmentEntry.copy(adjustmentType = Some(AdjustmentType.Overdeclaration))
      )

      result mustBe Some(
        SummaryListViewModel(rows =
          Seq(adjustmentTypeRow, returnPeriodRow, taxTypeRow, adjustmentVolumeRow, dutyRateRow, dutyDueRow)
        )
      )
    }

    "must return no rows when adjustment type summary can't be fetched" in new SetUp(false, true, true) {
      checkYourAnswersSummaryListHelper.currentAdjustmentEntrySummaryList(adjustmentEntry) mustBe None
    }

    "must return no rows when volume summary can't be fetched" in new SetUp(true, false, true) {
      checkYourAnswersSummaryListHelper.currentAdjustmentEntrySummaryList(adjustmentEntry) mustBe None
    }

    "must return no rows when duty due summary can't be fetched" in new SetUp(true, true, false) {
      checkYourAnswersSummaryListHelper.currentAdjustmentEntrySummaryList(adjustmentEntry) mustBe None
    }
  }

  class SetUp(
    canFetchAdjustmentTypeSummary: Boolean,
    canFetchAdjustmentVolumeSummary: Boolean,
    canFetchDutyDueSummary: Boolean
  ) {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)

    val repackagedTaxTypeRow = SummaryListRow(key = Key(Text("Row1Key")), value = Value(Text("Row1Value")))
    val sprDutyRateRow       = SummaryListRow(key = Key(Text("Row2Key")), value = Value(Text("Row2Value")))
    val returnPeriodRow      = SummaryListRow(key = Key(Text("Row3Key")), value = Value(Text("Row3Value")))
    val spoiltAlcoholTypeRow = SummaryListRow(key = Key(Text("Row4Key")), value = Value(Text("Row4Value")))
    val taxTypeRow           = SummaryListRow(key = Key(Text("Row5Key")), value = Value(Text("Row5Value")))
    val adjustmentTypeRow    = SummaryListRow(key = Key(Text("Row6Key")), value = Value(Text("Row6Value")))
    val adjustmentVolumeRow  = SummaryListRow(key = Key(Text("Row7Key")), value = Value(Text("Row7Value")))
    val dutyDueRow           = SummaryListRow(key = Key(Text("Row8Key")), value = Value(Text("Row8Value")))
    val dutyRateRow          = SummaryListRow(
      key = Key(Text("Duty rate")),
      value = Value(HtmlContent("£10.00"))
    )

    val mockAdjustmentRepackagedTaxTypeSummary           = mock[AdjustmentRepackagedTaxTypeSummary]
    val mockAdjustmentSmallProducerReliefDutyRateSummary = mock[AdjustmentSmallProducerReliefDutyRateSummary]
    val mockAdjustmentReturnPeriodSummary                = mock[AdjustmentReturnPeriodSummary]
    val mockSpoiltAlcoholicProductTypeSummary            = mock[SpoiltAlcoholicProductTypeSummary]
    val mockAdjustmentTaxTypeSummary                     = mock[AdjustmentTaxTypeSummary]
    val mockAdjustmentTypeSummary                        = mock[AdjustmentTypeSummary]
    val mockAdjustmentVolumeSummary                      = mock[AdjustmentVolumeSummary]
    val mockAdjustmentDutyDueSummary                     = mock[AdjustmentDutyDueSummary]
    val mockAdjustmentDutyRateSummary                    = mock[AdjustmentDutyRateSummary]

    val adjustmentEntry = AdjustmentEntry(
      adjustmentType = Some(Underdeclaration),
      sprDutyRate = Some(BigDecimal("10.00"))
    )

    when(mockAdjustmentRepackagedTaxTypeSummary.row(adjustmentEntry)).thenReturn(Some(repackagedTaxTypeRow))
    when(mockAdjustmentSmallProducerReliefDutyRateSummary.row(adjustmentEntry)).thenReturn(Some(sprDutyRateRow))
    when(mockAdjustmentReturnPeriodSummary.row(adjustmentEntry)).thenReturn(Some(returnPeriodRow))
    when(mockSpoiltAlcoholicProductTypeSummary.row(adjustmentEntry)).thenReturn(Some(spoiltAlcoholTypeRow))
    when(mockAdjustmentTaxTypeSummary.row(adjustmentEntry)).thenReturn(Some(taxTypeRow))
    when(mockAdjustmentDutyRateSummary.row(adjustmentEntry)).thenReturn(Some(dutyRateRow))

    if (canFetchAdjustmentTypeSummary) {
      when(mockAdjustmentTypeSummary.row(adjustmentEntry)).thenReturn(Some(adjustmentTypeRow))
    } else {
      when(mockAdjustmentTypeSummary.row(adjustmentEntry)).thenReturn(None)
    }

    if (canFetchAdjustmentVolumeSummary) {
      when(mockAdjustmentVolumeSummary.row(adjustmentEntry)).thenReturn(Some(adjustmentVolumeRow))
    } else {
      when(mockAdjustmentVolumeSummary.row(adjustmentEntry)).thenReturn(None)
    }

    if (canFetchDutyDueSummary) {
      when(mockAdjustmentDutyDueSummary.row(adjustmentEntry)).thenReturn(Some(dutyDueRow))
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
      mockAdjustmentDutyDueSummary,
      mockAdjustmentDutyRateSummary
    )
  }
}
