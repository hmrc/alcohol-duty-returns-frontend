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

import base.SpecBase
import cats.data.EitherT
import config.Constants.adrReturnCreatedDetails
import connectors.PayApiConnector
import models.checkAndSubmit.AdrReturnCreatedDetails
import models.payments.StartPaymentResponse
import org.mockito.ArgumentMatchers.any
import play.api.http.Status.SEE_OTHER
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.Helpers._

import java.time.Instant

class StartPaymentControllerSpec extends SpecBase {

  val startPaymentResponse = StartPaymentResponse("journey-id", "/next-url")

  val returnDetails = AdrReturnCreatedDetails(
    processingDate = Instant.now(clock),
    amount = BigDecimal(10.45),
    Some(chargeReference),
    paymentDueDate
  )

  "StartPayment Controller" - {

    "must redirect to the nextUrl if startPaymentResponse is successful" in {
      val payApiConnector = mock[PayApiConnector]

      when(payApiConnector.startPayment(any())(any())).thenReturn(
        EitherT.rightT(startPaymentResponse)
      )

      val application = applicationBuilder()
        .overrides(bind[PayApiConnector].toInstance(payApiConnector))
        .build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.routes.StartPaymentController.initiateAndRedirect().url).withSession(
            adrReturnCreatedDetails -> Json.toJson(returnDetails).toString()
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe "/next-url"
      }
    }

    "must redirect to the Journey Recovery controller if startPayment fails" in {
      val payApiConnector = mock[PayApiConnector]

      when(payApiConnector.startPayment(any())(any())).thenReturn(
        EitherT.leftT("Error message")
      )

      val application = applicationBuilder()
        .overrides(bind[PayApiConnector].toInstance(payApiConnector))
        .build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.routes.StartPaymentController.initiateAndRedirect().url)
            .withSession(adrReturnCreatedDetails -> Json.toJson(returnDetails).toString())

        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the Journey Recovery controller if no return details are present" in {
      val payApiConnector = mock[PayApiConnector]

      when(payApiConnector.startPayment(any())(any())).thenReturn(
        EitherT.rightT(startPaymentResponse)
      )

      val application = applicationBuilder()
        .overrides(bind[PayApiConnector].toInstance(payApiConnector))
        .build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.routes.StartPaymentController.initiateAndRedirect().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
