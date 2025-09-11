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

import config.Constants.pastPaymentsSessionKey
import controllers.actions._
import forms.payments.ManageCentralAssessmentFormProvider
import models.TransactionType.CA
import models.{NormalMode, OutstandingPayment}
import pages.declareDuty.MultipleSPRMissingDetailsPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.DateTimeHelper
import viewmodels.payments.ManageCentralAssessmentHelper
import views.html.payments.ManageCentralAssessmentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ManageCentralAssessmentController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifyWithEnrolmentAction,
  formProvider: ManageCentralAssessmentFormProvider,
  helper: ManageCentralAssessmentHelper,
  val controllerComponents: MessagesControllerComponents,
  view: ManageCentralAssessmentView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(chargeRef: String): Action[AnyContent] = identify { implicit request =>
    request.session.get(pastPaymentsSessionKey) match {
      case None                 =>
        logger.warn("Outstanding payment details not present in session")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      case Some(paymentDetails) =>
        Json.parse(paymentDetails).asOpt[Seq[OutstandingPayment]] match {
          case Some(outstandingPayments) =>
            outstandingPayments.find(p => p.chargeReference.contains(chargeRef) && p.transactionType == CA) match {
              case Some(charge) =>
                val viewModel = helper.getCentralAssessmentViewModel(charge)
                Ok(view(form, viewModel))
              case None         =>
                logger.warn("Could not find required Central Assessment charge in session")
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
          case None                      =>
            logger.warn("Could not parse outstanding payment details in session")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }
  }

  def onSubmit(chargeRef: String): Action[AnyContent] = identify { implicit request =>
    request.session.get(pastPaymentsSessionKey) match {
      case None                 =>
        logger.warn("Outstanding payment details not present in session")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      case Some(paymentDetails) =>
        Json.parse(paymentDetails).asOpt[Seq[OutstandingPayment]] match {
          case Some(outstandingPayments) =>
            outstandingPayments.find(p => p.chargeReference.contains(chargeRef) && p.transactionType == CA) match {
              case Some(charge) =>
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
                      Ok("Going to 'Pay central assessment charge'")
                    }
                  )
              case None         =>
                logger.warn("Could not find required Central Assessment charge in session")
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
          case None                      =>
            logger.warn("Could not parse outstanding payment details in session")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }
  }
}
