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
import forms.declareDuty.{DoYouHaveMultipleSPRDutyRatesFormProvider, MultipleSPRMissingDetailsFormProvider}
import models.{AlcoholRegime, Mode}
import navigation.ReturnsNavigator
import pages.declareDuty.{DoYouHaveMultipleSPRDutyRatesPage, nextPages}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.declareDuty.{DoYouHaveMultipleSPRDutyRatesView, MultipleSPRMissingDetailsView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

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
      Ok(view(form, regime))
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
