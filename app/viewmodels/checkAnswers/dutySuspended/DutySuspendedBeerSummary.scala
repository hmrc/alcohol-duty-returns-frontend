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

package viewmodels.checkAnswers.dutySuspended

import controllers.dutySuspended.routes
import models.{CheckMode, UserAnswers}
import pages.dutySuspended.DutySuspendedBeerPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object DutySuspendedBeerSummary {

  def totalVolumeRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(DutySuspendedBeerPage).map { answer =>
      SummaryListRowViewModel(
        key = "dutySuspendedBeer.totalBeer.checkYourAnswersLabel",
        value = ValueViewModel(s"${messages("site.2DP", answer.totalBeer)} ${messages("site.unit.litres")}"),
        actions = Seq(
          ActionItemViewModel("site.change", routes.DutySuspendedBeerController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("dutySuspendedBeer.totalBeer.change.hidden"))
        )
      )

    }

  def pureAlcoholRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(DutySuspendedBeerPage).map { answer =>
      SummaryListRowViewModel(
        key = "dutySuspendedBeer.pureAlcoholInBeer.checkYourAnswersLabel",
        value = ValueViewModel(
          s"${messages("site.4DP", answer.pureAlcoholInBeer)} ${messages("site.unit.litres")}"
        ),
        actions = Seq(
          ActionItemViewModel("site.change", routes.DutySuspendedBeerController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("dutySuspendedBeer.pureAlcoholInBeer.change.hidden"))
        )
      )

    }
}
