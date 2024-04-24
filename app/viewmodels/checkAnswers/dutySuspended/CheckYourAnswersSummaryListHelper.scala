/*
 * Copyright 2023 HM Revenue & Customs
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

package viewmodels.checkAnswers.dutySuspended

import models.UserAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.govuk.summarylist.SummaryListViewModel

class CheckYourAnswersSummaryListHelper(userAnswers: UserAnswers)(implicit messages: Messages) {

  def dutySuspendedDeliveriesSummaryList: Option[SummaryList] = {

    val totalbeer                          = getOptionalRow(DutySuspendedBeerSummary.totalVolumeRow(userAnswers))
    val pureAlcoholInBeer                  = getOptionalRow(DutySuspendedBeerSummary.pureAlcoholRow(userAnswers))
    val totalCider                         = getOptionalRow(DutySuspendedCiderSummary.totalVolumeRow(userAnswers))
    val pureAlcoholInCider                 = getOptionalRow(DutySuspendedCiderSummary.pureAlcoholRow(userAnswers))
    val totalWine                          = getOptionalRow(DutySuspendedWineSummary.totalVolumeRow(userAnswers))
    val pureAlcoholInWine                  = getOptionalRow(DutySuspendedWineSummary.pureAlcoholRow(userAnswers))
    val totalSpirts                        = getOptionalRow(DutySuspendedSpiritsSummary.totalVolumeRow(userAnswers))
    val pureAlcoholInSpirits               = getOptionalRow(DutySuspendedSpiritsSummary.pureAlcoholRow(userAnswers))
    val totalOtherFermentedSummary         = getOptionalRow(DutySuspendedOtherFermentedSummary.totalVolumeRow(userAnswers))
    val pureAlcoholInOtherFermentedSummary = getOptionalRow(
      DutySuspendedOtherFermentedSummary.pureAlcoholRow(userAnswers)
    )
    Some(
      SummaryListViewModel(
        rows = totalbeer ++
          pureAlcoholInBeer ++
          totalCider ++
          pureAlcoholInCider ++
          totalWine ++
          pureAlcoholInWine ++
          totalSpirts ++
          pureAlcoholInSpirits ++
          totalOtherFermentedSummary ++
          pureAlcoholInOtherFermentedSummary
      )
    )
  }
  private def getOptionalRow(row: Option[SummaryListRow]): Seq[SummaryListRow] =
    row match {
      case Some(row) => Seq(row)
      case None      => Seq.empty
    }
}
