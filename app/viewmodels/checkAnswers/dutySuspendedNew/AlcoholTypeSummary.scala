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

package viewmodels.checkAnswers.dutySuspendedNew

import controllers.dutySuspendedNew.routes
import models.{CheckMode, UserAnswers}
import pages.dutySuspendedNew.DutySuspendedAlcoholTypePage

import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AlcoholTypeSummary {

  def row(userAnswers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    userAnswers.get(DutySuspendedAlcoholTypePage).flatMap { alcoholTypes =>
      val rowValue = alcoholTypes.map(alcoholType => HtmlFormat.escape(messages(s"alcoholType.$alcoholType")).toString)

      val value = ValueViewModel(HtmlContent(s"""<span aria-label=${messages(
        "alcoholType.checkYourAnswersLabel"
      )}">${rowValue.mkString(",<br>")}</span>"""))

      Some(
        SummaryListRowViewModel(
          key = messages("checkYourAnswersDutySuspended.type"),
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", routes.DutySuspendedAlcoholTypeController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("alcoholType.change.hidden"))
          )
        )
      )
    }
}
