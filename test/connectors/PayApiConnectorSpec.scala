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

package connectors

import base.SpecBase
import config.FrontendAppConfig
import models.payments.{StartPaymentRequest, StartPaymentResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.scalatest.RecoverMethods.recoverToExceptionIf
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import play.api.http.Status.{BAD_GATEWAY, BAD_REQUEST, CREATED, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HttpResponse, StringContextOps, UpstreamErrorResponse}

import scala.concurrent.Future

class PayApiConnectorSpec extends SpecBase with ScalaFutures {
  val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val mockUrl                       = "http://pay-api/alcohol-duty/journey/start"
  when(mockConfig.startPaymentUrl).thenReturn(mockUrl)
  val connector                     = new PayApiConnector(config = mockConfig, httpClient = mock[HttpClientV2])
  val startPaymentRequest           =
    StartPaymentRequest("referenceNumber", BigInt(1000), "chargeReferenceNumber", "/return/url", "/back/url")

  when(
    requestBuilder.withBody(
      eqTo(Json.toJson(startPaymentRequest))
    )(any(), any(), any())
  )
    .thenReturn(requestBuilder)

  when {
    connector.httpClient
      .post(eqTo(url"$mockUrl"))(any())
  } thenReturn requestBuilder

  "startPayment" - {
    "successfully retrieve a start payment response" {
      val startPaymentResponse = StartPaymentResponse("journey-id", "/next-url")
      val jsonResponse         = Json.toJson(startPaymentResponse).toString()
      val httpResponse         = HttpResponse(CREATED, jsonResponse)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(httpResponse)))

      whenReady(connector.startPayment(startPaymentRequest).value) { result =>
        result mustBe Right(startPaymentResponse)
        verify(connector.httpClient, times(1))
          .post(eqTo(url"$mockUrl"))(any())
      }
    }
  }

  "fail when an invalid JSON format is returned" in {
    val invalidJsonResponse = HttpResponse(OK, """{ "invalid": "json" }""")

    when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
      .thenReturn(Future.successful(Right(invalidJsonResponse)))

    recoverToExceptionIf[Exception] {
      connector.startPayment(startPaymentRequest).value
    } map { ex =>
      ex.getMessage must include("Invalid JSON format")
      verify(connector.httpClient, times(1))
        .post(eqTo(url"$mockUrl"))(any())
    }
  }

  "fail when Start Payment returns an error" in {
    val upstreamErrorResponse = HttpResponse(BAD_GATEWAY, "")

    when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
      .thenReturn(Future.successful(Right(upstreamErrorResponse)))

    recoverToExceptionIf[Exception] {
      connector.startPayment(startPaymentRequest).value
    } map { ex =>
      ex.getMessage must include("Start Payment failed")
      verify(connector.httpClient, times(1))
        .post(eqTo(url"$mockUrl"))(any())
    }
  }

  "fail when an unexpected status code is returned" in {
    val invalidStatusCodeResponse = HttpResponse(BAD_REQUEST, "")

    when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
      .thenReturn(Future.successful(Right(invalidStatusCodeResponse)))

    recoverToExceptionIf[Exception] {
      connector.startPayment(startPaymentRequest).value
    } map { ex =>
      ex.getMessage must include("Unexpected status code: 400")

      verify(connector.httpClient, times(1))
        .post(url"$mockUrl")(any())
    }
  }

}
