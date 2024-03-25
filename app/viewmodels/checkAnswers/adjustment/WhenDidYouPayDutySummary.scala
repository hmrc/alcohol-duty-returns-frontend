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

package viewmodels.checkAnswers.adjustment

import models.{CheckMode, UserAnswers, YearMonthModelFormatter}
import pages.adjustment.WhenDidYouPayDutyPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object WhenDidYouPayDutySummary extends YearMonthModelFormatter {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(WhenDidYouPayDutyPage).map { answer =>
      val value =
        HtmlFormat.escape(answer.getMonth.toString).toString + "<br/>" + HtmlFormat
          .escape(answer.getYear.toString)
          .toString

      SummaryListRowViewModel(
        key = "whenDidYouPayDuty.checkYourAnswersLabel",
        value = ValueViewModel(HtmlContent(value)),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.adjustment.routes.WhenDidYouPayDutyController.onPageLoad(CheckMode).url
          )
            .withVisuallyHiddenText(messages("whenDidYouPayDuty.change.hidden"))
        )
      )
    }
}
