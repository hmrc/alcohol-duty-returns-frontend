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
import forms.declareDuty.MultipleSPRMissingDetailsFormProvider
import models.AlcoholRegime
import navigation.ReturnsNavigator
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.declareDuty.MissingSPRRateBandHelper
import views.html.declareDuty.MultipleSPRMissingDetailsView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class MultipleSPRMissingDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  navigator: ReturnsNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: MultipleSPRMissingDetailsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: MultipleSPRMissingDetailsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      MissingSPRRateBandHelper.findMissingSPRRateBands(regime, request.userAnswers) match {
        case Some(missingRateBands) if missingRateBands.nonEmpty =>
          val missingRateBandDescriptions =
            MissingSPRRateBandHelper.getMissingRateBandDescriptions(regime, missingRateBands)
          Ok(view(form, regime, missingRateBandDescriptions))
        case _                                                   =>
          logger.warn("User has not selected multiple SPR rates or does not have missing SPR rate bands")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  def onSubmit(regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      Ok("submitted")
    }

//  def onSubmit(mode: Mode, regime: AlcoholRegime): Action[AnyContent] =
//    (identify andThen getData andThen requireData).async { implicit request =>
//      form
//        .bindFromRequest()
//        .fold(
//          formWithErrors => Future.successful(BadRequest(view(formWithErrors, regime, mode))),
//          value => {
//            val hasChanged   = hasValueChanged(value, regime)
//            val pagesToClear = if (hasChanged) nextPages(currentPage) else Seq.empty
//            for {
//              updatedAnswers     <-
//                Future.fromTry(request.userAnswers.setByKey(currentPage, regime, value))
//              clearedUserAnswers <- Future.fromTry(updatedAnswers.removePagesByKey(pagesToClear, regime))
//              _                  <- userAnswersConnector.set(clearedUserAnswers)
//            } yield Redirect(
//              navigator.nextPageWithRegime(currentPage, mode, updatedAnswers, regime, pagesToClear.nonEmpty)
//            )
//          }
//        )
//    }
}
