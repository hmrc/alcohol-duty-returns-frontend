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

import models.adjustment.AdjustmentEntry
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.govuk.summarylist._

import javax.inject.Inject

class CheckYourAnswersSummaryListHelper @Inject() (whenDidYouPayDutySummary: WhenDidYouPayDutySummary) {

  def currentAdjustmentEntrySummaryList(
    adjustmentEntry: AdjustmentEntry
  )(implicit messages: Messages): Option[SummaryList] = {

    val newTaxType        = getOptionalRow(AdjustmentRepackagedTaxTypeSummary.row(adjustmentEntry))
    val sprDutyRate       = getOptionalRow(AdjustmentSmallProducerReliefDutyRateSummary.row(adjustmentEntry))
    val returnPeriod      = getOptionalRow(whenDidYouPayDutySummary.row(adjustmentEntry))
    val spoiltAlcoholType = getOptionalRow(AlcoholicProductTypeSummary.row(adjustmentEntry))
    val taxType           = getOptionalRow(AdjustmentTaxTypeSummary.row(adjustmentEntry))
    for {
      adjustmentType <- AdjustmentTypeSummary.row(adjustmentEntry)
      volume         <- AdjustmentVolumeSummary.row(adjustmentEntry)
      duty           <- AdjustmentDutyDueSummary.row(adjustmentEntry)
    } yield SummaryListViewModel(
      rows = Seq(adjustmentType) ++
        spoiltAlcoholType ++
        returnPeriod ++
        taxType ++
        newTaxType ++
        sprDutyRate ++
        Seq(volume) ++
        Seq(duty)
    )
  }

  private def getOptionalRow(row: Option[SummaryListRow]): Seq[SummaryListRow] =
    row match {
      case Some(row) => Seq(row)
      case None      => Seq.empty
    }
}
