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

package controllers.payments

import controllers.actions._
import forms.payments.ManageCentralAssessmentFormProvider
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.payments.CentralAssessmentHelper
import views.html.payments.ManageCentralAssessmentView

import javax.inject.Inject

class ManageCentralAssessmentController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifyWithEnrolmentAction,
  formProvider: ManageCentralAssessmentFormProvider,
  helper: CentralAssessmentHelper,
  val controllerComponents: MessagesControllerComponents,
  view: ManageCentralAssessmentView
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(chargeRef: String): Action[AnyContent] = identify { implicit request =>
    helper.getCentralAssessmentChargeFromSession(request.session, chargeRef) match {
      case Some((charge, _)) =>
        val viewModel = helper.getCentralAssessmentViewModel(charge)
        Ok(view(form, viewModel))
      case None              =>
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(chargeRef: String): Action[AnyContent] = identify { implicit request =>
    helper.getCentralAssessmentChargeFromSession(request.session, chargeRef) match {
      case Some((charge, _)) =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              val viewModel = helper.getCentralAssessmentViewModel(charge)
              BadRequest(view(formWithErrors, viewModel))
            },
            if (_) {
              Redirect(controllers.returns.routes.ViewPastReturnsController.onPageLoad)
            } else {
              Redirect(controllers.payments.routes.PayCentralAssessmentController.onPageLoad(chargeRef: String))
            }
          )
      case None              =>
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }
}
