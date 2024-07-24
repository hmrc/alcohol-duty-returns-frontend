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

import config.Constants
import models.{NormalMode, UserAnswers}
import pages.returns.{AlcoholDutyPage, DeclareAlcoholDutyQuestionPage}
import play.api.Logging
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import viewmodels.{Money, TableRowActionViewModel, TableRowViewModel, TableViewModel}

object DutyDueForThisReturnHelper extends Logging {

  def dutyDueByRegime(userAnswers: UserAnswers)(implicit
    messages: Messages
  ): Either[String, TableViewModel] =
    createRows(userAnswers).map { rows =>
      TableViewModel(
        head = Seq(),
        rows = rows
      )
    }

  private def createRows(
    userAnswers: UserAnswers
  )(implicit messages: Messages): Either[String, Seq[TableRowViewModel]] =
    (userAnswers.get(DeclareAlcoholDutyQuestionPage), userAnswers.get(AlcoholDutyPage)) match {
      case (Some(false), _)                  =>
        Right(
          Seq(
            TableRowViewModel(
              cells = Seq(
                TableRow(
                  content = Text(messages("dutyDueForThisReturn.table.nil.label")),
                  classes = Constants.boldFontCssClass
                ),
                TableRow(Text(messages("dutyDueForThisReturn.table.nil.value")))
              ),
              actions = Seq(
                TableRowActionViewModel(
                  label = "Change",
                  href = controllers.returns.routes.DeclareAlcoholDutyQuestionController.onPageLoad(NormalMode)
                )
              )
            )
          )
        )
      case (Some(true), Some(alcoholDuties)) =>
        Right(alcoholDuties.map { case (alcoholRegime, alcoholDuty) =>
          TableRowViewModel(
            cells = Seq(
              TableRow(
                content =
                  Text(messages("dutyDueForThisReturn.table.dutyDue", messages(s"return.regime.$alcoholRegime"))),
                classes = Constants.boldFontCssClass
              ),
              TableRow(Text(Money.format(alcoholDuty.totalDuty)))
            ),
            actions = Seq(
              TableRowActionViewModel(
                label = "Change",
                href = controllers.returns.routes.CheckYourAnswersController.onPageLoad(alcoholRegime)
              )
            )
          )
        }.toSeq)
      case (_, _)                            =>
        Left("Failed to create duty due table view model")
    }
}
