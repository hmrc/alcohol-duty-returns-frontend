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

import models.{AlcoholRegimes, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.govuk.summarylist.SummaryListViewModel

import javax.inject.Inject

class CheckYourAnswersSummaryListHelper @Inject() () {

  def dutySuspendedDeliveriesSummaryList(userAnswers: UserAnswers)(implicit messages: Messages): SummaryList = {
    val regimes = userAnswers.regimes

    val (maybeTotalBeer, maybePureAlcoholInBeer) =
      getBeerRows(userAnswers, regimes)

    val (maybeTotalCider, maybePureAlcoholInCider) =
      getWineRows(userAnswers, regimes)

    val (maybeTotalWine, maybePureAlcoholInWine) =
      getCiderRows(userAnswers, regimes)

    val (maybeTotalSpirits, maybePureAlcoholInSpirits) =
      getSpiritsRows(userAnswers, regimes)

    val (maybeTotalOtherFermentedSummary, maybePureAlcoholInOtherFermentedSummary) =
      getOFPRows(userAnswers, regimes)

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

  private def getOFPRows(userAnswers: UserAnswers, regimes: AlcoholRegimes)(implicit messages: Messages) =
    if (regimes.hasOtherFermentedProduct) {
      (
        Some(getOptionalRow(DutySuspendedOtherFermentedSummary.totalVolumeRow(userAnswers))),
        Some(getOptionalRow(DutySuspendedOtherFermentedSummary.pureAlcoholRow(userAnswers)))
      )
    } else { (None, None) }

  private def getSpiritsRows(userAnswers: UserAnswers, regimes: AlcoholRegimes)(implicit messages: Messages) =
    if (regimes.hasSpirits) {
      (
        Some(getOptionalRow(DutySuspendedSpiritsSummary.totalVolumeRow(userAnswers))),
        Some(getOptionalRow(DutySuspendedSpiritsSummary.pureAlcoholRow(userAnswers)))
      )
    } else { (None, None) }

  private def getCiderRows(userAnswers: UserAnswers, regimes: AlcoholRegimes)(implicit messages: Messages) =
    if (regimes.hasWine) {
      (
        Some(getOptionalRow(DutySuspendedWineSummary.totalVolumeRow(userAnswers))),
        Some(getOptionalRow(DutySuspendedWineSummary.pureAlcoholRow(userAnswers)))
      )
    } else { (None, None) }

  private def getWineRows(userAnswers: UserAnswers, regimes: AlcoholRegimes)(implicit messages: Messages) =
    if (regimes.hasCider) {
      (
        Some(getOptionalRow(DutySuspendedCiderSummary.totalVolumeRow(userAnswers))),
        Some(getOptionalRow(DutySuspendedCiderSummary.pureAlcoholRow(userAnswers)))
      )
    } else { (None, None) }

  private def getBeerRows(userAnswers: UserAnswers, regimes: AlcoholRegimes)(implicit messages: Messages) =
    if (regimes.hasBeer) {
      (
        Some(getOptionalRow(DutySuspendedBeerSummary.totalVolumeRow(userAnswers))),
        Some(getOptionalRow(DutySuspendedBeerSummary.pureAlcoholRow(userAnswers)))
      )
    } else { (None, None) }

  private def getOptionalRow(row: Option[SummaryListRow]): Seq[SummaryListRow] =
    row match {
      case Some(row) => Seq(row)
      case None      => Seq.empty
    }
}
