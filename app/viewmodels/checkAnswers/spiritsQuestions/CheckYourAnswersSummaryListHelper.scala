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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Card, CardTitle, SummaryList}
import viewmodels.govuk.all.FluentSummaryList
import viewmodels.govuk.summarylist.SummaryListViewModel

object CheckYourAnswersSummaryListHelper {

  def spiritsSummaryList(userAnswers: UserAnswers)(implicit messages: Messages): Option[SummaryList] = {
    for {
      totalSpiritsSummaryRow <- DeclareSpiritsTotalSummary.row(userAnswers)
      scotchWhiskySummaryRow <- WhiskySummary.scotchWhiskyRow(userAnswers)
      irishWhiskySummaryRow <- WhiskySummary.irishWhiskeyRow(userAnswers)
      spiritTypeSummaryRow <- SpiritTypeSummary.row(userAnswers)
    } yield SummaryListViewModel(
      Seq(
        totalSpiritsSummaryRow,
        scotchWhiskySummaryRow,
        irishWhiskySummaryRow,
        spiritTypeSummaryRow
      )
    ).copy(card = Some(Card(title = Some(CardTitle(content = Text(messages("spiritsQuestions.checkYourAnswersLabel.card1")))))))
}

  def alcoholUsedSummaryList(userAnswers: UserAnswers)(implicit messages: Messages): Option[SummaryList] = {
    for {
      beerSummaryRow <- AlcoholUsedSummary.beerRow(userAnswers)
      wineSummaryRow <- AlcoholUsedSummary.wineRow(userAnswers)
      madeWineSummaryRow <- AlcoholUsedSummary.madeWineRow(userAnswers)
      ciderOrPerrySummaryRow <- AlcoholUsedSummary.ciderOrPerryRow(userAnswers)
    } yield SummaryListViewModel(
      Seq(
        beerSummaryRow,
        wineSummaryRow,
        madeWineSummaryRow,
        ciderOrPerrySummaryRow
      )
    ).copy(card = Some(Card(title = Some(CardTitle(content = Text(messages("spiritsQuestions.checkYourAnswersLabel.card2")))))))
  }
}