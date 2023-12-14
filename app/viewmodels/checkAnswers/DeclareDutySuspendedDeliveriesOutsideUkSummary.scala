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

package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, Mode, UserAnswers}
import pages.DeclareDutySuspendedDeliveriesOutsideUkPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object DeclareDutySuspendedDeliveriesOutsideUkSummary {

  // Default the summaries to use the final check mode, but allow them to be used for other modes.
  // This allows us to just change the mode so the links in the actions are correct.
  def row(answers: UserAnswers, mode: Mode = CheckMode)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(DeclareDutySuspendedDeliveriesOutsideUkPage).map { answer =>
      SummaryListRowViewModel(
        key = "declareDutySuspendedDeliveriesOutsideUk.checkYourAnswersLabel",
        value = ValueViewModel(s"${answer.toString} ${messages("unit.litres")}"),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            // pass in the mode here, so that the controller can pass it to the journey's navigator.
            routes.DeclareDutySuspendedDeliveriesOutsideUkController.onPageLoad(mode).url
          )
            .withVisuallyHiddenText(messages("declareDutySuspendedDeliveriesOutsideUk.change.hidden"))
        )
      )
    }
}
