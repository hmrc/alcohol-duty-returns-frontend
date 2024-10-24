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
import pages.spiritsQuestions.GrainsUsedPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object GrainsUsedSummary {

  def maltedBarleyRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(GrainsUsedPage).map { answer =>
      SummaryListRowViewModel(
        key = "grainsUsed.maltedBarleyQuantity.checkYourAnswersLabel",
        value = ValueViewModel(
          s"${messages("site.2DP", answer.maltedBarleyQuantity)} ${messages("site.unit.tonnes")}"
        ),
        actions = Seq(
          ActionItemViewModel("site.change", routes.GrainsUsedController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("grainsUsed.maltedBarleyQuantity.change.hidden"))
        )
      )
    }
  def wheatRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow]        =
    answers.get(GrainsUsedPage).map { answer =>
      SummaryListRowViewModel(
        key = "grainsUsed.wheatQuantity.checkYourAnswersLabel",
        value = ValueViewModel(s"${messages("site.2DP", answer.wheatQuantity)} ${messages("site.unit.tonnes")}"),
        actions = Seq(
          ActionItemViewModel("site.change", routes.GrainsUsedController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("grainsUsed.wheatQuantity.change.hidden"))
        )
      )
    }

  def maizeRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(GrainsUsedPage).map { answer =>
      SummaryListRowViewModel(
        key = "grainsUsed.maizeQuantity.checkYourAnswersLabel",
        value = ValueViewModel(s"${messages("site.2DP", answer.maizeQuantity)} ${messages("site.unit.tonnes")}"),
        actions = Seq(
          ActionItemViewModel("site.change", routes.GrainsUsedController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("grainsUsed.maizeQuantity.change.hidden"))
        )
      )
    }

  def ryeRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(GrainsUsedPage).map { answer =>
      SummaryListRowViewModel(
        key = "grainsUsed.ryeQuantity.checkYourAnswersLabel",
        value = ValueViewModel(s"${messages("site.2DP", answer.ryeQuantity)} ${messages("site.unit.tonnes")}"),
        actions = Seq(
          ActionItemViewModel("site.change", routes.GrainsUsedController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("grainsUsed.ryeQuantity.change.hidden"))
        )
      )
    }

  def unmaltedGrainQuantityRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(GrainsUsedPage).map { answer =>
      SummaryListRowViewModel(
        key = "grainsUsed.unmaltedGrainQuantity.checkYourAnswersLabel",
        value = ValueViewModel(
          s"${messages("site.2DP", answer.unmaltedGrainQuantity)} ${messages("site.unit.tonnes")}"
        ),
        actions = Seq(
          ActionItemViewModel("site.change", routes.GrainsUsedController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("grainsUsed.unmaltedGrainQuantity.change.hidden"))
        )
      )
    }
}
