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

package controllers

import config.Constants.{adrReturnCreatedDetails, pastPaymentsSessionKey}
import config.FrontendAppConfig
import connectors.PayApiConnector
import models.OutstandingPayment
import controllers.actions.IdentifyWithEnrolmentAction
import models.audit.AuditPaymentStarted
import models.checkAndSubmit.AdrReturnCreatedDetails
import models.payments.StartPaymentRequest
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result, Session}
import services.AuditService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StartPaymentController @Inject() (
  identify: IdentifyWithEnrolmentAction,
  appConfig: FrontendAppConfig,
  payApiConnector: PayApiConnector,
  auditService: AuditService,
  clock: Clock,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def initiateAndRedirect(): Action[AnyContent] = identify.async { implicit request =>
    val returnUrl           = appConfig.businessTaxAccountUrl
    val backUrl             = appConfig.host + controllers.checkAndSubmit.routes.ReturnSubmittedController.onPageLoad().url
    val appaId              = request.appaId
    val governmentGatewayId = request.userId
    getReturnDetails(request.session) match {
      case Some(returnDetails) =>
        val startPaymentRequest = StartPaymentRequest(
          returnDetails,
          appaId,
          returnUrl,
          backUrl
        )
        startPayment(startPaymentRequest, appaId, governmentGatewayId)
      case _                   =>
        logger.warn(
          "Return details couldn't be read from the session. Start payment failed. Redirecting user to Journey Recovery"
        )
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }

  def initiateAndRedirectFromPastPayments(index: Int): Action[AnyContent] = identify.async { implicit request =>
    val url                 = appConfig.host + controllers.returns.routes.ViewPastPaymentsController.onPageLoad.url
    val appaId              = request.appaId
    val governmentGatewayId = request.userId
    getPaymentDetails(request.session, index, appaId, url) match {
      case Some(startPaymentRequest) => startPayment(startPaymentRequest, appaId, governmentGatewayId)
      case _                         =>
        logger.warn(
          "OutstandingPayment details couldn't be read from the session. Start payment failed. Redirecting user to Journey Recovery"
        )
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }

  }

  private def getReturnDetails(session: Session): Option[AdrReturnCreatedDetails] =
    session
      .get(adrReturnCreatedDetails)
      .flatMap(returnDetailsString => Json.parse(returnDetailsString).asOpt[AdrReturnCreatedDetails])

  private def startPayment(startPaymentRequest: StartPaymentRequest, appaId: String, governmentGatewayId: String)(
    implicit hc: HeaderCarrier
  ): Future[Result] =
    payApiConnector
      .startPayment(startPaymentRequest)
      .foldF(
        _ => {
          logger.warn("Start payment failed. Redirecting user to Journey Recovery")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        },
        startPaymentResponse => {
          auditPaymentStarted(
            appaId,
            governmentGatewayId,
            startPaymentResponse.journeyId,
            startPaymentRequest.chargeReferenceNumber,
            startPaymentRequest.amountInPence
          )
          Future.successful(Redirect(startPaymentResponse.nextUrl))
        }
      )

  private def getPaymentDetails(
    session: Session,
    index: Int,
    appaId: String,
    url: String
  ): Option[StartPaymentRequest] =
    session
      .get(pastPaymentsSessionKey)
      .flatMap { paymentDetails =>
        Json.parse(paymentDetails).asOpt[Seq[OutstandingPayment]] match {
          case Some(outstandingPayments) =>
            outstandingPayments.lift(index) match {
              case Some(outstandingPayment) => Some(StartPaymentRequest(outstandingPayment, appaId, url))
              case None                     =>
                logger.warn(s"outstandingPayment details at index $index not found.")
                None
            }
          case _                         => None
        }
      }

  private def auditPaymentStarted(
    appaId: String,
    governmentGatewayId: String,
    journeyId: String,
    chargeReference: String,
    amount: BigInt
  )(implicit
    hc: HeaderCarrier
  ): Unit = {
    val eventDetail = AuditPaymentStarted(
      appaId = appaId,
      governmentGatewayId = governmentGatewayId,
      paymentStartedTime = Instant.now(clock),
      journeyId = journeyId,
      chargeReference = chargeReference,
      amount = amount
    )
    auditService.audit(eventDetail)
  }
}
