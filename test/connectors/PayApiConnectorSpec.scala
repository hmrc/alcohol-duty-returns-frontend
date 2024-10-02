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
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status.{BAD_GATEWAY, BAD_REQUEST, CREATED, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HttpResponse, StringContextOps, UpstreamErrorResponse}

import scala.concurrent.Future

class PayApiConnectorSpec extends SpecBase with ScalaFutures {

  "startPayment" - {
    "successfully retrieve a start payment response" in new SetUp {
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

    "fail when an invalid JSON format is returned" in new SetUp {
      val invalidJsonResponse = HttpResponse(CREATED, """{ "invalid": "json" }""")

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(invalidJsonResponse)))

      whenReady(connector.startPayment(startPaymentRequest).value) { result =>
        result.swap.toOption.get must include("Invalid JSON format")
        verify(connector.httpClient, times(1))
          .post(eqTo(url"$mockUrl"))(any())
      }
    }

    "fail when Start Payment returns an error" in new SetUp {
      val upstreamErrorResponse = Future.successful(
        Left[UpstreamErrorResponse, HttpResponse](UpstreamErrorResponse("", BAD_GATEWAY, BAD_GATEWAY, Map.empty))
      )

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(upstreamErrorResponse)

      whenReady(connector.startPayment(startPaymentRequest).value) { result =>
        result.swap.toOption.get must include("Start Payment failed")
        verify(connector.httpClient, times(1))
          .post(eqTo(url"$mockUrl"))(any())
      }
    }

    "fail when an unexpected status code is returned" in new SetUp {
      val invalidStatusCodeResponse = HttpResponse(OK, "")

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(invalidStatusCodeResponse)))

      whenReady(connector.startPayment(startPaymentRequest).value) { result =>
        result.swap.toOption.get mustBe s"Unexpected status code when starting payment: $OK"

        verify(connector.httpClient, times(1))
          .post(eqTo(url"$mockUrl"))(any())
      }
    }
  }

  class SetUp {
    val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]
    val mockUrl                       = "http://pay-api/alcohol-duty/journey/start"
    when(mockConfig.startPaymentUrl).thenReturn(mockUrl)
    val connector                     = new PayApiConnector(config = mockConfig, httpClient = mock[HttpClientV2])
    val startPaymentRequest           =
      StartPaymentRequest("referenceNumber", BigInt(1000), "chargeReferenceNumber", "/return/url", "/back/url")

    val requestBuilder: RequestBuilder = mock[RequestBuilder]

    when(
      requestBuilder.withBody(
        eqTo(Json.toJson(startPaymentRequest))
      )(any(), any(), any())
    )
      .thenReturn(requestBuilder)

    when(connector.httpClient.post(any())(any())).thenReturn(requestBuilder)
  }
}
