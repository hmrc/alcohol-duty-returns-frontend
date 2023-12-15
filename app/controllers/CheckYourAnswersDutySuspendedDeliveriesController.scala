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
import models.UserAnswers
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.{DeclareDutySuspendedDeliveriesOutsideUkSummary, DeclareDutySuspendedReceivedSummary, DutySuspendedDeliveriesSummary}
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersDutySuspendedDeliveriesView

class CheckYourAnswersDutySuspendedDeliveriesController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersDutySuspendedDeliveriesView
) extends FrontendBaseController
    with I18nSupport {

  private def getSummaryListIfAnswersAreValid(
    userAnswers: UserAnswers
  )(implicit messages: Messages): Option[SummaryList] = {
    val rows = Seq(
      DeclareDutySuspendedDeliveriesOutsideUkSummary.row(userAnswers),
      DutySuspendedDeliveriesSummary.row(userAnswers),
      DeclareDutySuspendedReceivedSummary.row(userAnswers)
    ).flatten

    if (rows.contains(None) || rows.isEmpty) {
      None
    } else {
      Some(
        SummaryListViewModel(
          rows = rows
        )
      )
    }
  }

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    getSummaryListIfAnswersAreValid(request.userAnswers) match {
      case Some(list) =>
        Ok(view(list))
      case None       =>
        Redirect(routes.JourneyRecoveryController.onPageLoad())
    }
  }

}
