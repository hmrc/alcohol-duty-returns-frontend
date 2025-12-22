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

package controllers.declareDuty

import controllers.actions._
import models.AlcoholRegime
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.declareDuty.CheckYourAnswersSummaryListHelper
import views.html.declareDuty.CheckYourAnswersView

import javax.inject.Inject

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

  def onPageLoad(regime: AlcoholRegime): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      checkYourAnswersSummaryListHelper.createSummaryList(regime, request.userAnswers) match {
        case Right(summaryList) => Ok(view(regime, summaryList))
        case Left(error)        =>
          val (appaId, periodKey) = (request.userAnswers.returnId.appaId, request.userAnswers.returnId.periodKey)
          logger.warn(
            s"[declareDuty/CheckYourAnswersController] [onPageLoad] Error on declare duty CYA for appa id $appaId, period key $periodKey: ${error.message}"
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }
}
