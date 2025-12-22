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

package controllers.declareDuty

import connectors.UserAnswersConnector
import controllers.actions._
import forms.declareDuty.MultipleSPRMissingDetailsConfirmationFormProvider
import models.{AlcoholRegime, NormalMode}
import navigation.ReturnsNavigator
import pages.declareDuty.MultipleSPRMissingDetailsConfirmationPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.declareDuty.MissingSPRRateBandHelper
import views.html.declareDuty.MultipleSPRMissingDetailsConfirmationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MultipleSPRMissingDetailsConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  navigator: ReturnsNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  missingSPRRateBandHelper: MissingSPRRateBandHelper,
  formProvider: MultipleSPRMissingDetailsConfirmationFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: MultipleSPRMissingDetailsConfirmationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      missingSPRRateBandHelper.findMissingSPRRateBands(regime, request.userAnswers) match {
        case Some(missingRateBands) if missingRateBands.nonEmpty =>
          val missingRateBandDescriptions =
            missingSPRRateBandHelper.getMissingRateBandDescriptions(regime, missingRateBands)
          val hasMultipleRateBands        = missingRateBands.size > 1
          val form                        = formProvider(hasMultipleRateBands)
          Ok(view(form, regime, missingRateBandDescriptions, hasMultipleRateBands))
        case _                                                   =>
          logger.warn(
            "[MultipleSPRMissingDetailsConfirmationController] [onPageLoad] User answers do not contain the required data"
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  def onSubmit(regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      missingSPRRateBandHelper.findMissingSPRRateBands(regime, request.userAnswers) match {
        case Some(missingRateBands) if missingRateBands.nonEmpty =>
          val hasMultipleRateBands = missingRateBands.size > 1
          val form                 = formProvider(hasMultipleRateBands)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => {
                val missingRateBandDescriptions =
                  missingSPRRateBandHelper.getMissingRateBandDescriptions(regime, missingRateBands)
                Future.successful(
                  BadRequest(view(formWithErrors, regime, missingRateBandDescriptions, hasMultipleRateBands))
                )
              },
              value =>
                for {
                  updatedAnswers                 <-
                    Future
                      .fromTry(request.userAnswers.setByKey(MultipleSPRMissingDetailsConfirmationPage, regime, value))
                  answersWithRemovedDeclarations <-
                    Future.fromTry(
                      missingSPRRateBandHelper
                        .removeMissingRateBandsIfConfirmed(value, regime, updatedAnswers, missingRateBands)
                    )
                  _                              <- userAnswersConnector.set(answersWithRemovedDeclarations)
                } yield Redirect(
                  navigator.nextPageWithRegime(
                    MultipleSPRMissingDetailsConfirmationPage,
                    NormalMode,
                    answersWithRemovedDeclarations,
                    regime
                  )
                )
            )
        case _                                                   =>
          logger.warn(
            "[MultipleSPRMissingDetailsConfirmationController] [onSubmit] User answers do not contain the required data"
          )
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }
}
