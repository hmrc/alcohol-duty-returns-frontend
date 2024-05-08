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

package viewmodels.checkAnswers.spiritsQuestions

import models.UserAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Card, CardTitle, SummaryList, SummaryListRow}
import viewmodels.govuk.summarylist.SummaryListViewModel

object CheckYourAnswersSummaryListHelper {
  def spiritsSummaryList(userAnswers: UserAnswers)(implicit messages: Messages): Option[SummaryList] = {
    val spiritTypeSummaryRow = getOptionalRow(SpiritTypeSummary.row(userAnswers))
    for {
      totalSpiritsSummaryRow <- DeclareSpiritsTotalSummary.row(userAnswers)
      scotchWhiskySummaryRow <- WhiskySummary.scotchWhiskyRow(userAnswers)
      irishWhiskySummaryRow  <- WhiskySummary.irishWhiskeyRow(userAnswers)
      otherSpiritSummaryRow  <- OtherSpiritsProducedSummary.row(userAnswers)
    } yield SummaryListViewModel(
      Seq(totalSpiritsSummaryRow) ++
        Seq(scotchWhiskySummaryRow) ++
        Seq(irishWhiskySummaryRow) ++
        spiritTypeSummaryRow ++
        Seq(otherSpiritSummaryRow)
    ).copy(card =
      Some(Card(title = Some(CardTitle(content = Text(messages("spiritsQuestions.checkYourAnswersLabel.card1"))))))
    )
  }

  def grainsUsedSummaryList(userAnswers: UserAnswers)(implicit messages: Messages): Option[SummaryList]  =
    for {
      maltedBarleySummaryRow  <- GrainsUsedSummary.maltedBarleyRow(userAnswers)
      wheatSummaryRow         <- GrainsUsedSummary.wheatRow(userAnswers)
      maizeSummaryRow         <- GrainsUsedSummary.maizeRow(userAnswers)
      ryeSummaryRow           <- GrainsUsedSummary.ryeRow(userAnswers)
      unmaltedGrainSummaryRow <- GrainsUsedSummary.unmaltedGrainQuantityRow(userAnswers)
    } yield SummaryListViewModel(
      Seq(
        maltedBarleySummaryRow,
        wheatSummaryRow,
        maizeSummaryRow,
        ryeSummaryRow,
        unmaltedGrainSummaryRow
      )
    ).copy(card =
      Some(Card(title = Some(CardTitle(content = Text(messages("spiritsQuestions.checkYourAnswersLabel.card3"))))))
    )
  def alcoholUsedSummaryList(userAnswers: UserAnswers)(implicit messages: Messages): Option[SummaryList] =
    for {
      beerSummaryRow         <- AlcoholUsedSummary.beerRow(userAnswers)
      wineSummaryRow         <- AlcoholUsedSummary.wineRow(userAnswers)
      madeWineSummaryRow     <- AlcoholUsedSummary.madeWineRow(userAnswers)
      ciderOrPerrySummaryRow <- AlcoholUsedSummary.ciderOrPerryRow(userAnswers)
    } yield SummaryListViewModel(
      Seq(
        beerSummaryRow,
        wineSummaryRow,
        madeWineSummaryRow,
        ciderOrPerrySummaryRow
      )
    ).copy(card =
      Some(Card(title = Some(CardTitle(content = Text(messages("spiritsQuestions.checkYourAnswersLabel.card2"))))))
    )

  private def getOptionalRow(row: Option[SummaryListRow]): Seq[SummaryListRow] =
    row match {
      case Some(row) => Seq(row)
      case None      => Seq.empty
    }

}
