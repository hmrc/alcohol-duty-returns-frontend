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

import javax.inject.Inject

class CheckYourAnswersSummaryListHelper @Inject() () {

  def dutySuspendedDeliveriesSummaryList(userAnswers: UserAnswers)(implicit messages: Messages): SummaryList = {
    val regimes = userAnswers.regimes

    val hasBeer              = regimes.hasBeer()
    val hasCider             = regimes.hasCider()
    val hasWine              = regimes.hasWine()
    val hasSpirits           = regimes.hasSpirits()
    val hasFermentedProducts = regimes.hasOtherFermentedProduct()

    val maybeTotalBeer                          =
      if (hasBeer) Some(getOptionalRow(DutySuspendedBeerSummary.totalVolumeRow(userAnswers))) else None
    val maybePureAlcoholInBeer                  =
      if (hasBeer) Some(getOptionalRow(DutySuspendedBeerSummary.pureAlcoholRow(userAnswers))) else None
    val maybeTotalCider                         =
      if (hasCider) Some(getOptionalRow(DutySuspendedCiderSummary.totalVolumeRow(userAnswers))) else None
    val maybePureAlcoholInCider                 =
      if (hasCider) Some(getOptionalRow(DutySuspendedCiderSummary.pureAlcoholRow(userAnswers))) else None
    val maybeTotalWine                          =
      if (hasWine) Some(getOptionalRow(DutySuspendedWineSummary.totalVolumeRow(userAnswers))) else None
    val maybePureAlcoholInWine                  =
      if (hasWine) Some(getOptionalRow(DutySuspendedWineSummary.pureAlcoholRow(userAnswers))) else None
    val maybeTotalSpirits                       =
      if (hasSpirits) Some(getOptionalRow(DutySuspendedSpiritsSummary.totalVolumeRow(userAnswers))) else None
    val maybePureAlcoholInSpirits               =
      if (hasSpirits) Some(getOptionalRow(DutySuspendedSpiritsSummary.pureAlcoholRow(userAnswers))) else None
    val maybeTotalOtherFermentedSummary         =
      if (hasFermentedProducts) Some(getOptionalRow(DutySuspendedOtherFermentedSummary.totalVolumeRow(userAnswers)))
      else None
    val maybePureAlcoholInOtherFermentedSummary =
      if (hasFermentedProducts)
        Some(
          getOptionalRow(
            DutySuspendedOtherFermentedSummary.pureAlcoholRow(userAnswers)
          )
        )
      else None

    val rows = Seq(
      maybeTotalBeer,
      maybePureAlcoholInBeer,
      maybeTotalCider,
      maybePureAlcoholInCider,
      maybeTotalWine,
      maybePureAlcoholInWine,
      maybeTotalSpirits,
      maybePureAlcoholInSpirits,
      maybeTotalOtherFermentedSummary,
      maybePureAlcoholInOtherFermentedSummary
    ).flatten.fold(Seq.empty)(_ ++ _)

    SummaryListViewModel(rows = rows)
  }

  private def getOptionalRow(row: Option[SummaryListRow]): Seq[SummaryListRow] =
    row match {
      case Some(row) => Seq(row)
      case None      => Seq.empty
    }
}
