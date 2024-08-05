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

import config.Constants.adrReturnCreatedDetails
import config.FrontendAppConfig
import connectors.PayApiConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.checkAndSubmit.AdrReturnCreatedDetails
import models.payments.StartPaymentRequest
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{MessagesControllerComponents, Session}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StartPaymentController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  appConfig: FrontendAppConfig,
  payApiConnector: PayApiConnector,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def initiateAndRedirect() = (identify andThen getData).async { implicit request =>
    val returnUrl = appConfig.businessTaxAccountUrl
    val backUrl   = appConfig.host + controllers.checkAndSubmit.routes.ReturnSubmittedController.onPageLoad().url

    getReturnDetails(request.session) match {
      case Some(returnDetails) =>
        val startPaymentRequest = StartPaymentRequest.createPaymentStart(
          returnDetails,
          request.appaId,
          returnUrl,
          backUrl
        )

        payApiConnector
          .startPayment(startPaymentRequest)
          .foldF(
            _ => {
              logger.warn("Start payment failed. Redirecting user to Journey Recovery")
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            },
            startPaymentResponse => Future.successful(Redirect(startPaymentResponse.nextUrl))
          )

      case _ => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }

  def getReturnDetails(session: Session): Option[AdrReturnCreatedDetails] =
    session
      .get(adrReturnCreatedDetails)
      .flatMap(returnDetailsString => Json.parse(returnDetailsString).asOpt[AdrReturnCreatedDetails])
}
