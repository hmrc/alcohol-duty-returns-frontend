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

import models.adjustment.{AdjustmentEntry, AdjustmentType}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow, Value}
import viewmodels.govuk.summarylist._

import javax.inject.Inject

class CheckYourAnswersSummaryListHelper @Inject() (
  adjustmentRepackagedTaxTypeSummary: AdjustmentRepackagedTaxTypeSummary,
  adjustmentSmallProducerReliefDutyRateSummary: AdjustmentSmallProducerReliefDutyRateSummary,
  adjustmentReturnPeriodSummary: AdjustmentReturnPeriodSummary,
  spoiltAlcoholicProductTypeSummary: SpoiltAlcoholicProductTypeSummary,
  adjustmentTaxTypeSummary: AdjustmentTaxTypeSummary,
  adjustmentTypeSummary: AdjustmentTypeSummary,
  adjustmentVolumeSummary: AdjustmentVolumeSummary,
  adjustmentDutyDueSummary: AdjustmentDutyDueSummary
) {

  def currentAdjustmentEntrySummaryList(
    adjustmentEntry: AdjustmentEntry
  )(implicit messages: Messages): Option[SummaryList] = {
    val newTaxType        = adjustmentRepackagedTaxTypeSummary.row(adjustmentEntry).toList
    val sprDutyRate       = adjustmentSmallProducerReliefDutyRateSummary.row(adjustmentEntry).toList
    val returnPeriod      = adjustmentReturnPeriodSummary.row(adjustmentEntry).toList
    val spoiltAlcoholType = spoiltAlcoholicProductTypeSummary.row(adjustmentEntry).toList
    val taxType           = adjustmentTaxTypeSummary.row(adjustmentEntry).toList

    for {
      adjustmentType <- adjustmentTypeSummary.row(adjustmentEntry)
      volume         <- adjustmentVolumeSummary.row(adjustmentEntry)
      duty           <- adjustmentDutyDueSummary.row(adjustmentEntry)
    } yield {
      val baseRows = Seq(adjustmentType) ++
        spoiltAlcoholType ++
        returnPeriod ++
        taxType ++
        newTaxType ++
        Seq(volume) ++
        sprDutyRate

      val rowsWithDutyRate = adjustmentEntry.adjustmentType match {
        case adjustmentType
            if adjustmentType.contains(AdjustmentType.RepackagedDraughtProducts) ||
              adjustmentEntry.sprDutyRate.isDefined ||
              adjustmentType.contains(AdjustmentType.Spoilt) =>
          baseRows :+ duty
        case _ =>
          baseRows ++ Seq(
            SummaryListRow(
              key = Key(content = Text(messages("tellUsAboutMultipleSPRRate.checkYourAnswersLabel.dutyRate.label"))),
              value = Value(content =
                HtmlContent(
                  adjustmentEntry.rate
                    .getOrElse(
                      throw new IllegalStateException("Duty rate is mandatory unless repackaged draught product")
                    ) match {
                    case r => messages("site.currency.2DP", r)
                  }
                )
              ),
              actions = None
            ),
            duty
          )
      }

      SummaryListViewModel(rows = rowsWithDutyRate)
    }
  }
}
