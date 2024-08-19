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
import uk.gov.hmrc.http.{HttpClient, HttpResponse, UpstreamErrorResponse}

import scala.concurrent.Future

class AlcoholDutyAccountConnectorSpec extends SpecBase with ScalaFutures {
  val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val connector                     = new AlcoholDutyAccountConnector(config = mockConfig, httpClient = mock[HttpClient])
  val mockUrl                       = s"http://alcohol-duty-account/producers/$appaId/payments/open"

  "open payments" - {
    "successfully retrieve open payments" in {
      val openPaymentsResponse = openPaymentsData
      val jsonResponse = Json.toJson(openPaymentsResponse).toString()
      val httpResponse           = Future.successful(Right(HttpResponse(OK, jsonResponse)))

      when(mockConfig.adrGetOutstandingPaymentsUrl(eqTo(appaId))).thenReturn(mockUrl)

      when {
        connector.httpClient
          .GET[Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(any(), any(), any())
      } thenReturn httpResponse

      whenReady(connector.outstandingPayments(appaId)) { result =>
        result mustBe openPaymentsResponse
        verify(connector.httpClient, atLeastOnce)
          .GET[Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(any(), any(), any())
      }
    }

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

}
