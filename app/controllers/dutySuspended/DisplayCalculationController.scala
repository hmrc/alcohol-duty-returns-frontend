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

import controllers.actions._
import models.{AlcoholRegime, NormalMode}
import navigation.DutySuspendedNavigator
import pages.dutySuspended.{DisplayCalculationPage, DutySuspendedFinalVolumesPage, DutySuspendedQuantitiesPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.dutySuspendedNew.DisplayCalculationView

import javax.inject.Inject

class DisplayCalculationController @Inject() (
  override val messagesApi: MessagesApi,
  navigator: DutySuspendedNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  checkDSDNewJourneyToggle: CheckDSDNewJourneyToggleAction,
  val controllerComponents: MessagesControllerComponents,
  view: DisplayCalculationView
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen checkDSDNewJourneyToggle) { implicit request =>
      (
        request.userAnswers.getByKey(DutySuspendedQuantitiesPage, regime),
        request.userAnswers.getByKey(DutySuspendedFinalVolumesPage, regime)
      ) match {
        case (Some(enteredQuantities), Some(calculatedVolumes)) =>
          Ok(view(regime, enteredQuantities, calculatedVolumes))
        case _                                                  =>
          logger.warn(s"Entered quantities or calculated volumes are not present for regime $regime")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  def onSubmit(regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen checkDSDNewJourneyToggle) { implicit request =>
      (
        request.userAnswers.getByKey(DutySuspendedQuantitiesPage, regime),
        request.userAnswers.getByKey(DutySuspendedFinalVolumesPage, regime)
      ) match {
        case (Some(_), Some(_)) =>
          Redirect(navigator.nextPageWithRegime(DisplayCalculationPage, NormalMode, request.userAnswers, regime))
        case _                  =>
          logger.warn(s"Entered quantities or calculated volumes are not present for regime $regime")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }
}
