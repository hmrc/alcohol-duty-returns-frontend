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
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AlcoholUsedSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AlcoholUsedPage).map { answer =>
      val value = HtmlFormat.escape(answer.beer.toString()).toString + "<br/>" + HtmlFormat
        .escape(answer.wine.toString())
        .toString + "<br/>" + HtmlFormat.escape(answer.madeWine.toString()).toString + "<br/>" + HtmlFormat
        .escape(answer.ciderOrPerry.toString())
        .toString

      SummaryListRowViewModel(
        key = "alcoholUsed.checkYourAnswersLabel",
        value = ValueViewModel(HtmlContent(value)),
        actions = Seq(
          ActionItemViewModel("site.change", routes.AlcoholUsedController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("alcoholUsed.change.hidden"))
        )
      )
    }
}