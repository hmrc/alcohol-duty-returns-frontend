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
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.{NormalMode, UserAnswers}
import pages.adjustment.{AdjustmentTotalPage, DeclareAdjustmentQuestionPage}
import pages.returns.{AlcoholDutyPage, DeclareAlcoholDutyQuestionPage}
import play.api.Logging
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import viewmodels.{Money, TableRowActionViewModel, TableRowViewModel, TableViewModel}

object DutyDueForThisReturnHelper extends Logging {

  val dutyDueOrder = Seq(Beer, Cider, Wine, Spirits, OtherFermentedProduct)

  def dutyDueByRegime(userAnswers: UserAnswers)(implicit
    messages: Messages
  ): Either[String, TableViewModel] =
    for {
      rows          <- createReturnRows(userAnswers)
      adjustmentRow <- createAdjustmentRow(userAnswers)
    } yield TableViewModel(
      head = Seq(),
      rows = rows :+ adjustmentRow
    )

  private def createReturnRows(
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
        Right(
          alcoholDuties.toSeq.sortBy(entry => dutyDueOrder.indexOf(entry._1)).map { case (alcoholRegime, alcoholDuty) =>
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
          }
        )
      case (_, _)                            =>
        Left("Failed to create duty due table view model")
    }

  private def createAdjustmentRow(
    userAnswers: UserAnswers
  )(implicit messages: Messages): Either[String, TableRowViewModel] =
    (userAnswers.get(DeclareAdjustmentQuestionPage), userAnswers.get(AdjustmentTotalPage)) match {
      case (Some(false), _)                   =>
        Right(
          TableRowViewModel(
            cells = Seq(
              TableRow(
                content = Text(messages("dutyDueForThisReturn.table.adjustmentDue")),
                classes = Constants.boldFontCssClass
              ),
              TableRow(Text(messages("dutyDueForThisReturn.table.nil.value")))
            ),
            actions = Seq(
              TableRowActionViewModel(
                label = "Change",
                href = controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(NormalMode)
              )
            )
          )
        )
      case (Some(true), Some(adjustmentDuty)) =>
        Right(
          TableRowViewModel(
            cells = Seq(
              TableRow(
                content = Text(messages("dutyDueForThisReturn.table.adjustmentDue")),
                classes = Constants.boldFontCssClass
              ),
              TableRow(Text(Money.format(adjustmentDuty)))
            ),
            actions = Seq(
              TableRowActionViewModel(
                label = "Change",
                href = controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
              )
            )
          )
        )
      case (_, _)                             =>
        Left("Failed to create duty due table view model")
    }
}
