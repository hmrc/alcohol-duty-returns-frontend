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
import pages.spiritsQuestions.WhiskyPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object WhiskySummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(WhiskyPage).map { answer =>
      val value = HtmlFormat.escape(answer.scotchWhisky.toString()).toString + "<br/>" + HtmlFormat
        .escape(answer.irishWhiskey.toString())
        .toString

      SummaryListRowViewModel(
        key = "whisky.checkYourAnswersLabel",
        value = ValueViewModel(HtmlContent(value)),
        actions = Seq(
          ActionItemViewModel("site.change", routes.WhiskyController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("whisky.change.hidden"))
        )
      )
    }
}
