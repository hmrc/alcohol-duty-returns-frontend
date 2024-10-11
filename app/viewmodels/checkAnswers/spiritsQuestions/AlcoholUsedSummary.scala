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

import controllers.spiritsQuestions.routes
import models.{CheckMode, UserAnswers}
import pages.spiritsQuestions.AlcoholUsedPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AlcoholUsedSummary {
  def beerRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AlcoholUsedPage).map { answer =>
      SummaryListRowViewModel(
        key = "alcoholUsed.beer.checkYourAnswersLabel",
        value = ValueViewModel(s"${messages("site.2DP.noPadding", answer.beer)} ${messages("site.unit.litres")}"),
        actions = Seq(
          ActionItemViewModel("site.change", routes.AlcoholUsedController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("alcoholUsed.beer.change.hidden"))
        )
      )
    }
  def wineRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AlcoholUsedPage).map { answer =>
      SummaryListRowViewModel(
        key = "alcoholUsed.wine.checkYourAnswersLabel",
        value = ValueViewModel(s"${messages("site.2DP.noPadding", answer.wine)} ${messages("site.unit.litres")}"),
        actions = Seq(
          ActionItemViewModel("site.change", routes.AlcoholUsedController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("alcoholUsed.wine.change.hidden"))
        )
      )
    }

  def madeWineRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AlcoholUsedPage).map { answer =>
      SummaryListRowViewModel(
        key = "alcoholUsed.madeWine.checkYourAnswersLabel",
        value = ValueViewModel(s"${messages("site.2DP.noPadding", answer.madeWine)} ${messages("site.unit.litres")}"),
        actions = Seq(
          ActionItemViewModel("site.change", routes.AlcoholUsedController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("alcoholUsed.madeWine.change.hidden"))
        )
      )
    }

  def ciderOrPerryRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AlcoholUsedPage).map { answer =>
      SummaryListRowViewModel(
        key = "alcoholUsed.ciderOrPerry.checkYourAnswersLabel",
        value =
          ValueViewModel(s"${messages("site.2DP.noPadding", answer.ciderOrPerry)} ${messages("site.unit.litres")}"),
        actions = Seq(
          ActionItemViewModel("site.change", routes.AlcoholUsedController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("alcoholUsed.ciderOrPerry.change.hidden"))
        )
      )
    }
}
