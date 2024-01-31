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

package viewmodels.checkAnswers.productEntry

import controllers.productEntry.routes
import models.{CheckMode, UserAnswers}
import pages.productEntry.SmallProducerReliefQuestionPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
object SmallProducerReliefQuestionSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(SmallProducerReliefQuestionPage).map { answer =>
      val value = if (answer) "site.yes" else "site.no"

      SummaryListRowViewModel(
        key = "smallProducerReliefQuestion.checkYourAnswersLabel",
        value = ValueViewModel(messages(value)),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.SmallProducerReliefQuestionController.onPageLoad(CheckMode).url
          )
            .withVisuallyHiddenText(messages("smallProducerReliefQuestion.change.hidden"))
        )
      )
    }

}
