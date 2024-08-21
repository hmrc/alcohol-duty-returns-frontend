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

package controllers.returns

import config.Constants.pastPaymentsSessionKey
import config.FrontendAppConfig
import connectors.PayApiConnector
import controllers.actions.IdentifierAction
import models.OutstandingPayment
import models.payments.StartPaymentRequest
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Session}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PayNowController @Inject() (
                                   identify: IdentifierAction,
                                   appConfig: FrontendAppConfig,
                                   payApiConnector: PayApiConnector,
                                   val controllerComponents: MessagesControllerComponents
                                 )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Logging {

  def initiateAndRedirect(index: Int): Action[AnyContent] = identify.async { implicit request =>
    val url = appConfig.host + controllers.returns.routes.ViewPastPaymentsController.onPageLoad.url
    getPaymentDetails(request.session, index) match {
      case (Some(chargeReference), Some(amount)) =>
        val amountInPence = (amount * 100).toBigInt
        val startPaymentRequest = StartPaymentRequest(request.appaId, amountInPence, chargeReference, url, url)
        payApiConnector
          .startPayment(startPaymentRequest)
          .foldF(
            _ => {
              logger.warn("View past payments Pay now payment failed. Redirecting user to Journey Recovery")
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            },
            startPaymentResponse => Future.successful(Redirect(startPaymentResponse.nextUrl))
          )
      case (_, _) => logger.warn("View past payments Pay now payment failed. Redirecting user to Journey Recovery")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }

  private def getPaymentDetails(session: Session, index: Int): (Option[String],Option[BigDecimal]) = {
    session.get(pastPaymentsSessionKey).flatMap(paymentDetails => Json.parse(paymentDetails).asOpt[Seq[OutstandingPayment]]) match {
      case Some(outstandingPayments) => getChargeRefAndAmount(outstandingPayments.lift(index))//startpaymentrequest a new apply method
      case _ => (None, None)
    }
  }

  private def getChargeRefAndAmount(outstandingPayment: Option[OutstandingPayment]):(Option[String],Option[BigDecimal]) =
    outstandingPayment match {
      case Some(outstandingPayment) => (outstandingPayment.chargeReference, Some(outstandingPayment.remainingAmount))
      case _ => (None, None)
    }

}
