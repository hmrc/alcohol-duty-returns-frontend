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
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HttpClient, HttpResponse, StringContextOps, UpstreamErrorResponse}

import scala.concurrent.Future

class AlcoholDutyAccountConnectorSpec extends SpecBase with ScalaFutures {
  val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val connector                     = new AlcoholDutyAccountConnector(config = mockConfig, httpClient = mock[HttpClientV2])

  "open payments" - {
    val mockUrl = s"http://alcohol-duty-account/producers/$appaId/payments/open"
    "successfully retrieve open payments" in {
      val openPaymentsResponse = openPaymentsData
      val jsonResponse         = Json.toJson(openPaymentsResponse).toString()
      val httpResponse         = HttpResponse(OK, jsonResponse)

      when(mockConfig.adrGetOutstandingPaymentsUrl(eqTo(appaId))).thenReturn(mockUrl)
      val requestBuilder: RequestBuilder = mock[RequestBuilder]

//        when(.get(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)
      when {
        connector.httpClient
          .get(any())(any())
      } thenReturn requestBuilder
      when(requestBuilder.execute).thenReturn(Future.successful(httpResponse))
      whenReady(connector.outstandingPayments(appaId)) { result =>
        result mustBe openPaymentsResponse
        verify(connector.httpClient, atLeastOnce)
          .get(url"${eqTo(mockUrl)}")(any())
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }
  }
  /*
    "fail when invalid JSON is returned" in {
      val invalidJsonResponse = Future.successful(Right(HttpResponse(OK, """{ "invalid": "json" }""")))
      when(mockConfig.adrGetOutstandingPaymentsUrl(appaId)).thenReturn(mockUrl)
      when(
        connector.httpClient
          .GET[Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(any(), any(), any())
      )
        .thenReturn(invalidJsonResponse)
      recoverToExceptionIf[String] {
        connector.outstandingPayments(appaId)
      } map { ex =>
        ex must include("Invalid JSON format")
        verify(connector.httpClient, atLeastOnce)
          .GET[Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(any(), any(), any())
      }
    }

    "fail when unexpected status code returned" in {
      val invalidStatusCodeResponse = Future.successful(Right(HttpResponse(BAD_REQUEST, "")))
      when(mockConfig.adrGetOutstandingPaymentsUrl(appaId)).thenReturn(mockUrl)
      when(
        connector.httpClient
          .GET[Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(any(), any(), any())
      )
        .thenReturn(invalidStatusCodeResponse)
      recoverToExceptionIf[String] {
        connector.outstandingPayments(appaId)
      } map { ex =>
        ex must include("Unexpected status code: 400")
        verify(connector.httpClient, atLeastOnce)
          .GET[Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(any(), any(), any())
      }
    }
  }
  "historic payments" - {
    val year    = 2024
    val mockUrl = s"http://alcohol-duty-account/producers/$appaId/payments/historic/$year"
    "successfully retrieve historic payments" in {
      val historicPaymentsResponse = historicPayments
      val jsonResponse             = Json.toJson(historicPaymentsResponse).toString()
      val httpResponse             = Future.successful(Right(HttpResponse(OK, jsonResponse)))

      when(mockConfig.adrGetHistoricPaymentsUrl(eqTo(appaId), eqTo(year))).thenReturn(mockUrl)

      when {
        connector.httpClient
          .GET[Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(any(), any(), any())
      } thenReturn httpResponse

      whenReady(connector.historicPayments(appaId, year)) { result =>
        result mustBe historicPaymentsResponse
        verify(connector.httpClient, atLeastOnce)
          .GET[Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(any(), any(), any())
      }
    }

    "fail when invalid JSON is returned" in {
      val invalidJsonResponse = Future.successful(Right(HttpResponse(OK, """{ "invalid": "json" }""")))
      when(mockConfig.adrGetHistoricPaymentsUrl(appaId, year)).thenReturn(mockUrl)
      when(
        connector.httpClient
          .GET[Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(any(), any(), any())
      )
        .thenReturn(invalidJsonResponse)
      recoverToExceptionIf[String] {
        connector.historicPayments(appaId, year)
      } map { ex =>
        ex must include("Invalid JSON format")
        verify(connector.httpClient, atLeastOnce)
          .GET[Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(any(), any(), any())
      }
    }

    "fail when unexpected status code returned" in {
      val invalidStatusCodeResponse = Future.successful(Right(HttpResponse(BAD_REQUEST, "")))
      when(mockConfig.adrGetHistoricPaymentsUrl(appaId, year)).thenReturn(mockUrl)
      when(
        connector.httpClient
          .GET[Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(any(), any(), any())
      )
        .thenReturn(invalidStatusCodeResponse)
      recoverToExceptionIf[String] {
        connector.historicPayments(appaId, year)
      } map { ex =>
        ex must include("Unexpected status code: 400")
        verify(connector.httpClient, atLeastOnce)
          .GET[Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(any(), any(), any())
      }
    }
  }*/
}
