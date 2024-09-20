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
  /* protected val endpointName: String = "pay-api"
  val mockConfig: FrontendAppConfig  = mock[FrontendAppConfig]
  val mockUrl                        = "/mock-url"
  val connector                      = new PayApiConnector(config = mockConfig, httpClient = mock[HttpClientV2])
  val startPaymentRequest            =
    StartPaymentRequest("referenceNumber", BigInt(1000), "chargeReferenceNumber", "/return/url", "/back/url")

  "startPayment" - {
    "successfully retrieve a start payment response" {
      val startPaymentResponse = StartPaymentResponse("journey-id", "/next-url")
      val jsonResponse         = Json.toJson(startPaymentResponse).toString()
      val httpResponse         = HttpResponse(CREATED, jsonResponse)

      when(mockConfig.startPaymentUrl).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(httpResponse)))

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

      when {
        connector.httpClient
          .post(eqTo(url"$mockUrl"))(any())
      } thenReturn requestBuilder

      whenReady(connector.startPayment(startPaymentRequest).value) { result =>
        result mustBe startPaymentResponse

        verify(connector.httpClient, times(1))
          .post(eqTo(url"$mockUrl"))(any())
//
//        verify(requestBuilder, atLeastOnce)
//          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }
  }*/
}
/*
    "fail when an invalid JSON format is returned" in {
      val invalidJsonResponse = Future.successful(Right(HttpResponse(OK, """{ "invalid": "json" }""")))
      when(mockConfig.startPaymentUrl).thenReturn(mockUrl)
      when(
        connector.httpClient
          .POST[StartPaymentRequest, Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(
            any(),
            any(),
            any(),
            any()
          )
      )
        .thenReturn(invalidJsonResponse)
      recoverToExceptionIf[Exception] {
        connector.startPayment(startPaymentRequest).value
      } map { ex =>
        ex.getMessage must include("Invalid JSON format")
        verify(connector.httpClient, atLeastOnce)
          .POST[StartPaymentRequest, Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(
            any(),
            any(),
            any(),
            any()
          )
      }
    }

    "fail when Start Payment returns an error" in {
      val upstreamErrorResponse = Future.successful(Right(HttpResponse(BAD_GATEWAY, "")))
      when(mockConfig.startPaymentUrl).thenReturn(mockUrl)
      when(
        connector.httpClient
          .POST[StartPaymentRequest, Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(
            any(),
            any(),
            any(),
            any()
          )
      )
        .thenReturn(upstreamErrorResponse)
      recoverToExceptionIf[Exception] {
        connector.startPayment(startPaymentRequest).value
      } map { ex =>
        ex.getMessage must include("Start Payment failed")
        verify(connector.httpClient, atLeastOnce)
          .POST[StartPaymentRequest, Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(
            any(),
            any(),
            any(),
            any()
          )
      }
    }
    "fail when an unexpected status code is returned" in {
      //  val mockHttpClient: HttpClientV2   = mock[HttpClientV2]
      //  val requestBuilder: RequestBuilder = mock[RequestBuilder]
      //  when(mockHttpClient.get(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)
      //  when(mockHttpClient.post(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)
      val invalidStatusCodeResponse = Future.successful(Right(HttpResponse(BAD_REQUEST, "")))
      when(mockConfig.startPaymentUrl).thenReturn(mockUrl)
      // Assuming httpClientV2 uses a similar structure for POST but with an updated signature
      when(
        connector.httpClient
          .post(url"$mockUrl") // Update to use v2's method structure
          .withBody[StartPaymentRequest](any())( ec) // Use withBody to send the request body in v2
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()) // Correct method for execution
      ).thenReturn(invalidStatusCodeResponse)
      recoverToExceptionIf[Exception] {
        connector.startPayment(startPaymentRequest).value
      } map { ex =>
        ex.getMessage must include("Unexpected status code: 400")
        // Verify the httpClientV2 POST method was called correctly
        verify(connector.httpClient, atLeastOnce)
          .post(url"$mockUrl") // Use the `post` method as per httpClientV2
          .withBody[StartPaymentRequest](any())( ec) // Correctly mock the body in v2
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()) // Verify execution
      }
    }
//    "fail when an unexpected status code is returned" in {
//      val invalidStatusCodeResponse = Future.successful(Right(HttpResponse(BAD_REQUEST, "")))
//      when(mockConfig.startPaymentUrl).thenReturn(mockUrl)
//      // Assuming httpClientV2 uses a similar structure for POST but with an updated signature
//      when(
//        connector.httpClient
//          .post(url"$mockUrl") // Update to use v2's method structure
//          .withBody[StartPaymentRequest](any())(ec) // Use withBody to send the request body in v2
//          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()) // Correct method for execution
//      ).thenReturn(invalidStatusCodeResponse)
//      recoverToExceptionIf[Exception] {
//        connector.startPayment(startPaymentRequest).value
//      } map { ex =>
//        ex.getMessage must include("Unexpected status code: 400")
//        // Verify the httpClientV2 POST method was called correctly
//        verify(connector.httpClient, atLeastOnce)
//          .post(url"$mockUrl") // Use the `post` method as per httpClientV2
//          .withBody[StartPaymentRequest](any())(ec) // Correctly mock the body in v2
//          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()) // Verify execution
//      }
//    }
//    "fail when an unexpected status code is returned" in {
//      val invalidStatusCodeResponse = Future.successful(Right(HttpResponse(BAD_REQUEST, "")))
//      when(mockConfig.startPaymentUrl).thenReturn(mockUrl)
//      when(
//        connector.httpClient
//          .POST[StartPaymentRequest, Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(
//            any(),
//            any(),
//            any(),
//            any()
//          )
//      )
//        .thenReturn(invalidStatusCodeResponse)
//      recoverToExceptionIf[Exception] {
//        connector.startPayment(startPaymentRequest).value
//      } map { ex =>
//        ex.getMessage must include("Unexpected status code: 400")
//        verify(connector.httpClient, atLeastOnce)
//          .POST[StartPaymentRequest, Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(
//            any(),
//            any(),
//            any(),
//            any()
//          )
//      }
//    }
  }
 */
// }

//  class SetUp extends ConnectorFixture {
//    wireMockServer.start()
//  }
