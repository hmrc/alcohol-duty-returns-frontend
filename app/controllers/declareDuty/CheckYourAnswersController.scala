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

import connectors.UserAnswersConnector
import controllers.actions._
import models.AlcoholRegime
import pages.QuestionPage
import pages.declareDuty.{MissingRateBandsToDeletePage, MultipleSPRMissingDetailsConfirmationPage, MultipleSPRMissingDetailsPage}
import play.api.Logging

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.declareDuty.{CheckYourAnswersSummaryListHelper, MissingSPRRateBandHelper}
import views.html.declareDuty.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  userAnswersConnector: UserAnswersConnector,
  checkYourAnswersSummaryListHelper: CheckYourAnswersSummaryListHelper,
  missingSPRRateBandHelper: MissingSPRRateBandHelper,
  view: CheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(regime: AlcoholRegime): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val removedRateBands = request.userAnswers.getByKey(MissingRateBandsToDeletePage, regime).map { rateBands =>
        missingSPRRateBandHelper.getMissingRateBandDescriptions(regime, rateBands)
      }

      val pagesToRemove: Seq[QuestionPage[Map[AlcoholRegime, _]]] = Seq(
        MultipleSPRMissingDetailsPage,
        MultipleSPRMissingDetailsConfirmationPage,
        MissingRateBandsToDeletePage
      ).map(_.asInstanceOf[QuestionPage[Map[AlcoholRegime, _]]])

      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.removePagesByKey(pagesToRemove, regime))
        _              <- userAnswersConnector.set(updatedAnswers)
      } yield checkYourAnswersSummaryListHelper.createSummaryList(regime, request.userAnswers) match {
        case Some(summaryList) => Ok(view(regime, summaryList, removedRateBands))
        case None              =>
          val (appaId, periodKey) = (request.userAnswers.returnId.appaId, request.userAnswers.returnId.periodKey)
          logger.warn(
            s"Unable to retrieve summary list rows during declare duty CYA onPageLoad appa id: $appaId, period key: $periodKey"
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }
}
