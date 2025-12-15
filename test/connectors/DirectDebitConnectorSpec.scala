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
import models.payments.{StartDirectDebitRequest, StartDirectDebitResponse}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{atLeastOnce, verify, when}
import play.api.http.Status.{BAD_GATEWAY, CREATED, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HttpResponse, StringContextOps, UpstreamErrorResponse}

import scala.concurrent.Future

class DirectDebitConnectorSpec extends SpecBase {

  "startDirectDebit" - {
    "successfully retrieve a start direct debit response" in new SetUp {
      val startDirectDebitResponse = StartDirectDebitResponse("/next-url")
      val jsonResponse             = Json.toJson(startDirectDebitResponse).toString()
      val httpResponse             = Future.successful(Right(HttpResponse(CREATED, jsonResponse)))

      when(mockConfig.startDirectDebitUrl).thenReturn(mockUrl)

      when(connector.httpClient.post(any())(any())).thenReturn(requestBuilder)

      when(requestBuilder.withBody(any())(any(), any(), any()))
        .thenReturn(requestBuilder)

      when(requestBuilder.setHeader("Csrf-Token" -> "nocheck"))
        .thenReturn(requestBuilder)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(httpResponse)

      whenReady(connector.startDirectDebit(startDirectDebitRequest).value) { result =>
        result mustBe Right(startDirectDebitResponse)

        verify(connector.httpClient, atLeastOnce)
          .post(eqTo(url"$mockUrl"))(any())
      }
    }

    "fail when an invalid JSON format is returned" in new SetUp {
      val invalidJsonResponse = Future.successful(Right(HttpResponse(CREATED, """{ "invalid": "json" }""")))

      when(mockConfig.startDirectDebitUrl).thenReturn(mockUrl)

      when(connector.httpClient.post(any())(any())).thenReturn(requestBuilder)

      when(requestBuilder.withBody(any())(any(), any(), any()))
        .thenReturn(requestBuilder)

      when(requestBuilder.setHeader("Csrf-Token" -> "nocheck"))
        .thenReturn(requestBuilder)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(invalidJsonResponse)

      whenReady(connector.startDirectDebit(startDirectDebitRequest).value) { result =>
        result.swap.toOption.get must include("Invalid JSON format")

        verify(connector.httpClient, atLeastOnce)
          .post(eqTo(url"$mockUrl"))(any())
      }
    }

    "fail when Start Direct Debit returns an error" in new SetUp {
      val upstreamErrorResponse = Future.successful(
        Left[UpstreamErrorResponse, HttpResponse](UpstreamErrorResponse("", BAD_GATEWAY, BAD_GATEWAY, Map.empty))
      )

      when(mockConfig.startDirectDebitUrl).thenReturn(mockUrl)

      when(connector.httpClient.post(any())(any())).thenReturn(requestBuilder)

      when(requestBuilder.withBody(any())(any(), any(), any()))
        .thenReturn(requestBuilder)

      when(requestBuilder.setHeader("Csrf-Token" -> "nocheck"))
        .thenReturn(requestBuilder)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(upstreamErrorResponse)

      whenReady(connector.startDirectDebit(startDirectDebitRequest).value) { result =>
        result.swap.toOption.get must include("Start Direct Debit failed")

        verify(connector.httpClient, atLeastOnce)
          .post(eqTo(url"$mockUrl"))(any())
      }
    }

    "fail when an unexpected status code is returned" in new SetUp {
      val invalidStatusCodeResponse = Future.successful(Right(HttpResponse(OK, "")))

      when(mockConfig.startDirectDebitUrl).thenReturn(mockUrl)

      when(connector.httpClient.post(any())(any())).thenReturn(requestBuilder)

      when(requestBuilder.withBody(any())(any(), any(), any()))
        .thenReturn(requestBuilder)

      when(requestBuilder.setHeader("Csrf-Token" -> "nocheck"))
        .thenReturn(requestBuilder)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(invalidStatusCodeResponse)

      whenReady(connector.startDirectDebit(startDirectDebitRequest).value) { result =>
        result.swap.toOption.get mustBe s"Unexpected status code received when starting direct debit: $OK"

        verify(connector.httpClient, atLeastOnce)
          .post(eqTo(url"$mockUrl"))(any())
      }
    }
  }

  class SetUp {
    val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]
    val connector                     = new DirectDebitConnector(config = mockConfig, httpClient = mock[HttpClientV2])
    val mockUrl                       = "http://mock-url"

    val startDirectDebitRequest =
      StartDirectDebitRequest("/return/url", "/back/url")

    val requestBuilder: RequestBuilder = mock[RequestBuilder]
  }
}
