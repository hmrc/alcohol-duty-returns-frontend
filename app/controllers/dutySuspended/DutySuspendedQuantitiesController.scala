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

import connectors.{AlcoholDutyCalculatorConnector, UserAnswersConnector}
import controllers.actions._
import forms.dutySuspended.DutySuspendedQuantitiesFormProvider
import models.{AlcoholRegime, Mode}
import navigation.DutySuspendedNavigator
import pages.dutySuspended.{DutySuspendedAlcoholTypePage, DutySuspendedFinalVolumesPage, DutySuspendedQuantitiesPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.dutySuspended.DutySuspendedQuantitiesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DutySuspendedQuantitiesController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  calculatorConnector: AlcoholDutyCalculatorConnector,
  navigator: DutySuspendedNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: DutySuspendedQuantitiesFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DutySuspendedQuantitiesView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode, regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      if (request.userAnswers.get(DutySuspendedAlcoholTypePage).exists(_.contains(regime))) {
        val form         = formProvider(regime)
        val preparedForm = request.userAnswers.getByKey(DutySuspendedQuantitiesPage, regime) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Ok(view(preparedForm, regime, mode))
      } else {
        logger.warn(s"User has not selected regime $regime for duty suspense")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  def onSubmit(mode: Mode, regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      if (request.userAnswers.get(DutySuspendedAlcoholTypePage).exists(_.contains(regime))) {
        val form = formProvider(regime)
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, regime, mode))),
            value => {
              val result =
                for {
                  updatedAnswersWithQuantities   <-
                    Future.fromTry(request.userAnswers.setByKey(DutySuspendedQuantitiesPage, regime, value))
                  calculatedVolumes              <- calculatorConnector.calculateDutySuspendedVolumes(value)
                  updatedAnswersWithFinalVolumes <-
                    Future.fromTry(
                      updatedAnswersWithQuantities.setByKey(DutySuspendedFinalVolumesPage, regime, calculatedVolumes)
                    )
                  _                              <- userAnswersConnector.set(updatedAnswersWithFinalVolumes)
                } yield Redirect(
                  navigator
                    .nextPageWithRegime(DutySuspendedQuantitiesPage, mode, updatedAnswersWithFinalVolumes, regime)
                )
              result.recover { case e =>
                logger.warn(s"Error: ${e.getMessage}")
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
              }
            }
          )
      } else {
        logger.warn(s"User has not selected regime $regime for duty suspense")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }
}
