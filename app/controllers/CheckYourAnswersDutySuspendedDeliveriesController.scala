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

package controllers

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.{CheckDutySuspendedDeliveriesMode, CheckMode, Mode, UserAnswers}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.{DeclareDutySuspendedDeliveriesOutsideUkSummary, DeclareDutySuspendedDeliveriesQuestionSummary}
import viewmodels.govuk.summarylist._
import views.html.{CheckYourAnswersDutySuspendedDeliveriesView, CheckYourAnswersView}

class CheckYourAnswersDutySuspendedDeliveriesController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersDutySuspendedDeliveriesView
) extends FrontendBaseController
    with I18nSupport {

  // This is to validate that the answers are ready to be checked, if they are it returns their summary rows, else None.
  // For example, if the user somehow misses a question, this should be handled appropriately.
  // A simple case, here, is that we want to validate all questions in our subjourney have been answered.
  private def getSummaryListIfAnswersAreValid(
    userAnswers: UserAnswers
  )(implicit messages: Messages): Option[SummaryList] = {
    val rows = Seq(
      DeclareDutySuspendedDeliveriesQuestionSummary.row(userAnswers, CheckDutySuspendedDeliveriesMode),
      DeclareDutySuspendedDeliveriesOutsideUkSummary.row(userAnswers, CheckDutySuspendedDeliveriesMode),
      DeclareDutySuspendedDeliveriesQuestionSummary.row(userAnswers, CheckDutySuspendedDeliveriesMode)
    ).flatten

    if (rows.contains(None) || rows.isEmpty) {
      None
    } else {
      Some(SummaryListViewModel(
        rows = rows
      ))
    }
  }

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    getSummaryListIfAnswersAreValid(request.userAnswers) match {
      case Some(list) =>
        // Show the view if the answers are valid
        Ok(view(list))
      case None       =>
        // The appropriate handling might be to redirect them to the question. This is up for debate.
        // For now, I'm just going to give a bad request instead.
        BadRequest
    }
  }

}
