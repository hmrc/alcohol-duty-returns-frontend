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
import pages.dutySuspended.DutySuspendedCiderPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object DutySuspendedCiderSummary {

  def totalVolumeRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(DutySuspendedCiderPage).map { answer =>
      SummaryListRowViewModel(
        key = "dutySuspendedCider.totalCider.checkYourAnswersLabel",
        value = ValueViewModel(s"${answer.totalCider.toString} ${messages("site.unit.litres")}"),
        actions = Seq(
          ActionItemViewModel("site.change", routes.DutySuspendedCiderController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("dutySuspendedCider.change.hidden"))
        )
      )
    }

  def pureAlcoholRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(DutySuspendedCiderPage).map { answer =>
      SummaryListRowViewModel(
        key = "dutySuspendedCider.pureAlcoholInCider.checkYourAnswersLabel",
        value = ValueViewModel(s"${answer.pureAlcoholInCider.toString} ${messages("site.unit.litres")}"),
        actions = Seq(
          ActionItemViewModel("site.change", routes.DutySuspendedCiderController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("dutySuspendedCider.change.hidden"))
        )
      )
    }
}
