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

package controllers.payments

import base.SpecBase
import cats.data.EitherT
import config.Constants.{adrReturnCreatedDetails, pastPaymentsSessionKey}
import connectors.PayApiConnector
import models.OutstandingPayment
import models.TransactionType.Return
import models.audit.AuditPaymentStarted
import models.checkAndSubmit.AdrReturnCreatedDetails
import models.payments.StartPaymentResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.http.Status.SEE_OTHER
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.Helpers._
import services.AuditService

import java.time.{Clock, Instant, LocalDate}

class StartPaymentControllerSpec extends SpecBase {

  val startPaymentResponse = StartPaymentResponse("journey-id", "/next-url")

  val returnDetails = AdrReturnCreatedDetails(
    processingDate = Instant.now(clock),
    amount = BigDecimal(10.45),
    Some(chargeReference),
    Some(paymentDueDate)
  )

  val outstandingPayment = OutstandingPayment(
    Return,
    LocalDate.of(9999, 6, 25),
    Some(chargeReference),
    BigDecimal(10.45)
  )

  val expectedAuditEvent = AuditPaymentStarted(
    appaId = appaId,
    credentialID = internalId,
    paymentStartedTime = Instant.now(clock),
    journeyId = startPaymentResponse.journeyId,
    chargeReference = chargeReference,
    amountInPence = BigInt(1045)
  )

  "StartPayment Controller return payment" - {

    "must redirect to the nextUrl if startPaymentResponse is successful with audit event" in {
      val payApiConnector                = mock[PayApiConnector]
      val mockAuditService: AuditService = mock[AuditService]

      when(payApiConnector.startPayment(any())(any())).thenReturn(
        EitherT.rightT(startPaymentResponse)
      )

      val application = applicationBuilder()
        .overrides(
          bind[PayApiConnector].toInstance(payApiConnector),
          bind[AuditService].toInstance(mockAuditService),
          bind(classOf[Clock]).toInstance(clock)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.payments.routes.StartPaymentController.initiateAndRedirect().url).withSession(
            adrReturnCreatedDetails -> Json.toJson(returnDetails).toString()
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockAuditService).audit(eqTo(expectedAuditEvent))(any(), any())
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
          FakeRequest(GET, controllers.payments.routes.StartPaymentController.initiateAndRedirect().url)
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
          FakeRequest(GET, controllers.payments.routes.StartPaymentController.initiateAndRedirect().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }

  "StartPayment Controller past payment" - {

    "must redirect to the nextUrl if startPaymentResponse is successful with audit event" in {
      val payApiConnector                = mock[PayApiConnector]
      val mockAuditService: AuditService = mock[AuditService]
      when(payApiConnector.startPayment(any())(any())).thenReturn(
        EitherT.rightT(startPaymentResponse)
      )

      val application = applicationBuilder()
        .overrides(
          bind[PayApiConnector].toInstance(payApiConnector),
          bind[AuditService].toInstance(mockAuditService),
          bind(classOf[Clock]).toInstance(clock)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            controllers.payments.routes.StartPaymentController.initiateAndRedirectFromPastPayments(0).url
          )
            .withSession(
              pastPaymentsSessionKey -> Json.toJson(Seq(outstandingPayment)).toString()
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockAuditService).audit(eqTo(expectedAuditEvent))(any(), any())
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
          FakeRequest(
            GET,
            controllers.payments.routes.StartPaymentController.initiateAndRedirectFromPastPayments(0).url
          )
            .withSession(pastPaymentsSessionKey -> Json.toJson(Seq(outstandingPayment)).toString())

        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the Journey Recovery controller if no outstanding payment is found at given index" in {
      val payApiConnector = mock[PayApiConnector]

      val application = applicationBuilder()
        .overrides(bind[PayApiConnector].toInstance(payApiConnector))
        .build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            controllers.payments.routes.StartPaymentController.initiateAndRedirectFromPastPayments(3).url
          )
            .withSession(
              pastPaymentsSessionKey -> Json.toJson(Seq(outstandingPayment)).toString()
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the Journey Recovery controller if session contains valid but incorrect JSON format for OutstandingPayment" in {
      val payApiConnector = mock[PayApiConnector]

      val application = applicationBuilder()
        .overrides(bind[PayApiConnector].toInstance(payApiConnector))
        .build()
      running(application) {

        val request =
          FakeRequest(
            GET,
            controllers.payments.routes.StartPaymentController.initiateAndRedirectFromPastPayments(0).url
          )
            .withSession(
              pastPaymentsSessionKey -> Json.toJson("malformed JSON").toString()
            )
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
