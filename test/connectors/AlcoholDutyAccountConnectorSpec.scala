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
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.scalatest.RecoverMethods.recoverToExceptionIf
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HttpResponse, StringContextOps, UpstreamErrorResponse}

import scala.concurrent.Future

class AlcoholDutyAccountConnectorSpec extends SpecBase with ScalaFutures {

  "open payments" - {
    val mockUrl = s"http://alcohol-duty-account/producers/$appaId/payments/open"
    "successfully retrieve open payments" in new SetUp {
      val openPaymentsResponse = openPaymentsData
      val jsonResponse         = Json.toJson(openPaymentsResponse).toString()
      val httpResponse         = HttpResponse(OK, jsonResponse)

      when(mockConfig.adrGetOutstandingPaymentsUrl(eqTo(appaId))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(httpResponse)))

      when {
        connector.httpClient
          .get(eqTo(url"$mockUrl"))(any())
      } thenReturn requestBuilder

      whenReady(connector.outstandingPayments(appaId)) { result =>
        result mustBe openPaymentsResponse
        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when invalid JSON is returned" in new SetUp {
      val invalidJsonResponse = Right(HttpResponse(OK, """{ "invalid": "json" }"""))
      when(mockConfig.adrGetOutstandingPaymentsUrl(appaId)).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(invalidJsonResponse))

      when {
        connector.httpClient
          .get(eqTo(url"$mockUrl"))(any())
      } thenReturn requestBuilder

      recoverToExceptionIf[String] {
        connector.outstandingPayments(appaId)
      } map { ex =>
        ex must include("Invalid JSON format")

        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when unexpected status code returned" in new SetUp {
      val invalidStatusCodeResponse = Right(HttpResponse(BAD_REQUEST, ""))

      when(mockConfig.adrGetOutstandingPaymentsUrl(appaId)).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(invalidStatusCodeResponse))

      when {
        connector.httpClient
          .get(eqTo(url"$mockUrl"))(any())
      } thenReturn requestBuilder

      recoverToExceptionIf[String] {
        connector.outstandingPayments(appaId)
      } map { ex =>
        ex must include("Unexpected status code: 400")

        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }
  }

  "historic payments" - {
    val year    = 2024
    val mockUrl = s"http://alcohol-duty-account/producers/$appaId/payments/historic/$year"
    "successfully retrieve historic payments" in new SetUp {
      val historicPaymentsResponse = historicPayments
      val jsonResponse             = Json.toJson(historicPaymentsResponse).toString()
      val httpResponse             = Right(HttpResponse(OK, jsonResponse))

      when(mockConfig.adrGetHistoricPaymentsUrl(eqTo(appaId), eqTo(year))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(httpResponse))

      when {
        connector.httpClient
          .get(eqTo(url"$mockUrl"))(any())
      } thenReturn requestBuilder

      whenReady(connector.historicPayments(appaId, year)) { result =>
        result mustBe historicPaymentsResponse

        verify(connector.httpClient, atLeastOnce)
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, atLeastOnce)
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when invalid JSON is returned" in new SetUp {
      val invalidJsonResponse = Right(HttpResponse(OK, """{ "invalid": "json" }"""))

      when(mockConfig.adrGetHistoricPaymentsUrl(appaId, year)).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(invalidJsonResponse))

      when {
        connector.httpClient
          .get(eqTo(url"$mockUrl"))(any())
      } thenReturn requestBuilder

      recoverToExceptionIf[String] {
        connector.historicPayments(appaId, year)
      } map { ex =>
        ex must include("Invalid JSON format")

        verify(connector.httpClient, atLeastOnce)
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, atLeastOnce)
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when unexpected status code returned" in new SetUp {
      val invalidStatusCodeResponse = Right(HttpResponse(BAD_REQUEST, ""))

      when(mockConfig.adrGetHistoricPaymentsUrl(appaId, year)).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(invalidStatusCodeResponse))

      when {
        connector.httpClient
          .get(eqTo(url"$mockUrl"))(any())
      } thenReturn requestBuilder

      recoverToExceptionIf[String] {
        connector.historicPayments(appaId, year)
      } map { ex =>
        ex must include("Unexpected status code: 400")

        verify(connector.httpClient, atLeastOnce)
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, atLeastOnce)
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }
  }

  class SetUp {
    val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]
    val connector                     = new AlcoholDutyAccountConnector(config = mockConfig, httpClient = mock[HttpClientV2])
  }
}
