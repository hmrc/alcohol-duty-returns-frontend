/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.dutySuspended

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifyWithEnrolmentAction}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.dutySuspended.CheckYourAnswersSummaryListHelper
import views.html.dutySuspended.CheckYourAnswersView

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  checkYourAnswersSummaryListHelper: CheckYourAnswersSummaryListHelper,
  view: CheckYourAnswersView
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val maybeAlcoholTypeSummary = checkYourAnswersSummaryListHelper.alcoholTypeSummaryList(request.userAnswers)
    val maybeAmountSummary      = checkYourAnswersSummaryListHelper.dutySuspendedAmountsSummaryList(request.userAnswers)
    (maybeAlcoholTypeSummary, maybeAmountSummary) match {
      case (Some(alcoholTypeSummary), Some(amountSummary)) =>
        if (request.userAnswers.regimes.regimes.size > 1) {
          Ok(view(Some(alcoholTypeSummary), amountSummary))
        } else {
          Ok(view(None, amountSummary))
        }
      case _                                               =>
        logger.warn("Alcohol types or duty suspended volumes missing from user answers")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }
}
