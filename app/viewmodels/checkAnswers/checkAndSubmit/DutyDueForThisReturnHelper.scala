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

package viewmodels.checkAnswers.checkAndSubmit

import models.{NormalMode, UserAnswers}
import pages.returns.{AlcoholDutyPage, DeclareAlcoholDutyQuestionPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.HeadCell
import viewmodels.{TableRowActionViewModel, TableRowViewModel, TableViewModel}

object DutyDueForThisReturnHelper {

  def dutyDueByRegime(userAnswers: UserAnswers)(implicit
    messages: Messages
  ): TableViewModel =
    TableViewModel(
      head = Seq(),
      rows = createRows(userAnswers),
      total = 0
    )

  private def createRows(userAnswers: UserAnswers)(implicit messages: Messages): Seq[TableRowViewModel] =
    (userAnswers.get(DeclareAlcoholDutyQuestionPage), userAnswers.get(AlcoholDutyPage)) match {
      case (Some(false), _)                  =>
        Seq(
          TableRowViewModel(
            cells = Seq(
              Text(messages("dutyDueForThisReturn.table.nil.label")),
              Text(messages("dutyDueForThisReturn.table.nil.value"))
            ),
            actions = Seq(
              TableRowActionViewModel(
                label = "Change",
                href = controllers.returns.routes.DeclareAlcoholDutyQuestionController.onPageLoad(NormalMode)
              )
            )
          )
        )
      case (Some(true), Some(alcoholDuties)) =>
        alcoholDuties.map { case (alcoholRegime, alcoholDuty) =>
          TableRowViewModel(
            cells = Seq(
              Text(messages("dutyDueForThisReturn.table.dutyDue", messages(s"return.regime.$alcoholRegime"))),
              Text(messages("site.currency.2DP", alcoholDuty.totalDuty))
            ),
            actions = Seq(
              TableRowActionViewModel(
                label = "Change",
                href = controllers.returns.routes.CheckYourAnswersController.onPageLoad(alcoholRegime)
              )
            )
          )
        }.toSeq
      case (None, None)                      => Seq.empty
    }
}
