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

import connectors.{AlcoholDutyCalculatorConnector, TotalDutyCalculationRequest, UserAnswersConnector}
import controllers.actions._
import models.declareDuty.VolumeAndRateByTaxType
import models.{AlcoholRegime, UserAnswers}
import pages.QuestionPage
import pages.declareDuty.{AlcoholDutyPage, DoYouHaveMultipleSPRDutyRatesPage, DutyCalculationPage, HowMuchDoYouNeedToDeclarePage, MultipleSPRListPage, TellUsAboutSingleSPRRatePage}
import play.api.Logging

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.declareDuty.DutyCalculationHelper
import views.html.declareDuty.DutyCalculationView

import scala.concurrent.{ExecutionContext, Future}

class DutyCalculationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  userAnswersConnector: UserAnswersConnector,
  calculatorConnector: AlcoholDutyCalculatorConnector,
  val controllerComponents: MessagesControllerComponents,
  view: DutyCalculationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(regime: AlcoholRegime): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val totalDutyCalculatorRequest = createTotalDutyRequest(regime, request.userAnswers)
      for {
        totalDuty          <- calculatorConnector.calculateTotalDuty(totalDutyCalculatorRequest)
        updatedUserAnswers <- Future.fromTry(request.userAnswers.setByKey(DutyCalculationPage, regime, totalDuty))
        _                  <- userAnswersConnector.set(updatedUserAnswers)
      } yield DutyCalculationHelper.dutyDueTableViewModel(totalDuty, request.userAnswers, regime) match {
        case Left(errorMessage)      =>
          logger.warn(s"Failed to create duty due table view model: $errorMessage")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        case Right(dutyDueViewModel) => Ok(view(regime, dutyDueViewModel, totalDuty.totalDuty))
      }
  }

  def onSubmit(regime: AlcoholRegime): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.getByKey(DutyCalculationPage, regime) match {
        case None            =>
          logger.warn(s"Failed to get DutyCalculationPage from user answers for regime: $regime")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        case Some(totalDuty) =>
          for {
            updatedUserAnswers <- Future.fromTry(request.userAnswers.setByKey(AlcoholDutyPage, regime, totalDuty))
            _                  <- userAnswersConnector.set(updatedUserAnswers)
          } yield Redirect(controllers.routes.TaskListController.onPageLoad)
      }
  }

  private def createTotalDutyRequest(
    regime: AlcoholRegime,
    userAnswers: UserAnswers
  ): TotalDutyCalculationRequest = {
    val sprPage: QuestionPage[Map[AlcoholRegime, Seq[VolumeAndRateByTaxType]]] =
      userAnswers.getByKey(DoYouHaveMultipleSPRDutyRatesPage, regime) match {
        case Some(true) => MultipleSPRListPage
        case _          => TellUsAboutSingleSPRRatePage
      }

    val coreAndDraughtDuty = userAnswers.getByKey(HowMuchDoYouNeedToDeclarePage, regime).getOrElse(Seq.empty)
    val dutyByTaxType      = userAnswers.getByKey(sprPage, regime).getOrElse(Seq.empty)
    TotalDutyCalculationRequest(coreAndDraughtDuty ++ dutyByTaxType)
  }
}
